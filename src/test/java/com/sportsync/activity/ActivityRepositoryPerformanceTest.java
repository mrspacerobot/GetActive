package com.sportsync.activity;

import com.sportsync.BaseTestWithDatabase;
import com.sportsync.user.User;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Disabled
public class ActivityRepositoryPerformanceTest extends BaseTestWithDatabase {

    @Autowired private ActivityRepository activityRepository;

    @Autowired private GeometryFactory geometryFactory;

    @Autowired private JdbcTemplate jdbcTemplate;

    private User testUser;

    @BeforeEach
    void setup() {
        // Create test user
        testUser =
                User.builder()
                        .email("performance_test@example.com")
                        .password("password")
                        .name("Performance Test User")
                        .build();
        entityManager.persist(testUser);
        entityManager.flush();
    }

    @Test
    @DisplayName("Generate test data and measure query performance")
    @Commit // Commit the transaction so we can see the data
    void generateTestDataAndMeasurePerformance() throws Exception {
        // Step 1: Create SQL functions for data generation
        createDataGenerationFunctions();

        // Step 2: Generate test data in batches to avoid memory issues
        int totalRecords = 1000000; // Total number of records to generate
        int batchSize = 10000; // Number of records per batch
        int batches = totalRecords / batchSize;

        for (int batch = 0; batch < batches; batch++) {
            System.out.println("Generating batch " + (batch + 1) + " of " + batches);
            generateTestActivitiesBatch(testUser.getId(), batchSize);
        }

        System.out.println("Generated " + totalRecords + " test activities");

        // Step 3: Set up indexes for testing
        setupIndexes();

        // Step 4: Run performance test
        runPerformanceTests();
    }

    private void createDataGenerationFunctions() throws Exception {
        // Use raw JDBC for DDL operations to avoid transaction issues
        try (Connection conn = jdbcTemplate.getDataSource().getConnection();
                Statement stmt = conn.createStatement()) {

            // Enable UUID extension if not already enabled
            stmt.execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";");

            // Function to generate random points - using standard quotes instead of dollar quoting
            stmt.execute(
                    "CREATE OR REPLACE FUNCTION random_point() RETURNS geometry AS '\n"
                            + "BEGIN\n"
                            + "  RETURN ST_SetSRID(ST_MakePoint(\n"
                            + "    -115.0 + random() * 40.0,  -- Longitude range\n"
                            + "    30.0 + random() * 20.0     -- Latitude range\n"
                            + "  ), 4326);\n"
                            + "END;\n"
                            + "' LANGUAGE plpgsql;");

            // Function to generate random running attributes - using standard quotes
            stmt.execute(
                    "CREATE OR REPLACE FUNCTION random_running_attributes() RETURNS jsonb AS '\n"
                            + "DECLARE\n"
                            + "  terrains text[] := array[''TRAIL'', ''ASPHALT'', ''GRAVEL''];\n"
                            + "BEGIN\n"
                            + "  RETURN jsonb_build_object(\n"
                            + "    ''@type'', ''RUNNING'',\n"
                            + "    ''distance'', 1.0 + random() * 30.0,\n"
                            + "    ''pace'', 4.0 + random() * 6.0,\n"
                            + "    ''terrain'', terrains[1 + floor(random() * array_length(terrains, 1))::int]\n"
                            + "  );\n"
                            + "END;\n"
                            + "' LANGUAGE plpgsql;");
        }
    }

    private void generateTestActivitiesBatch(UUID creatorId, int batchSize) {
        StringBuilder sql = new StringBuilder();

        for (int i = 0; i < batchSize; i++) {
            int activityTypeIndex = (int) (Math.random() * 4);
            int daysOffset = -30 + (int) (Math.random() * 60);

            sql.append(
                            "INSERT INTO activities (id, title, activity_type, start_time, end_time, max_participants, location, attributes, creator_id) VALUES (")
                    .append("uuid_generate_v4(), ")
                    .append("'Test Activity ")
                    .append(i)
                    .append("', ")
                    .append(activityTypeIndex)
                    .append(", ")
                    .append("NOW() + INTERVAL '")
                    .append(daysOffset)
                    .append(" days', ")
                    .append("NOW() + INTERVAL '")
                    .append(daysOffset)
                    .append(" days' + INTERVAL '2 hours', ")
                    .append("10, ")
                    .append("random_point(), ")
                    .append(activityTypeIndex == 0 ? "random_running_attributes()" : "'{}'::jsonb")
                    .append(", ")
                    .append("'")
                    .append(creatorId)
                    .append("');\n");
        }

        jdbcTemplate.execute(sql.toString());
    }

    private void setupIndexes() {
        // First, drop any existing indexes we want to test
        jdbcTemplate.execute("DROP INDEX IF EXISTS idx_activities_location");
        jdbcTemplate.execute("DROP INDEX IF EXISTS idx_activities_location_time");
        jdbcTemplate.execute("DROP INDEX IF EXISTS idx_activity_type_start_time");
        jdbcTemplate.execute("DROP INDEX IF EXISTS idx_activities_attributes");

        // Create essential spatial index
        jdbcTemplate.execute(
                "CREATE INDEX idx_activities_location ON activities USING GIST(location)");

        // Create JSON index
        jdbcTemplate.execute(
                "CREATE INDEX idx_activities_attributes ON activities USING GIN(attributes jsonb_path_ops)");

        // Create specialized running index
        jdbcTemplate.execute(
                "CREATE INDEX idx_running_distance ON activities (((attributes->>'distance')::double precision)) "
                        + "WHERE activity_type = 0" // RUNNING = 0
                );

        // Create combined index for activity type and terrain
        jdbcTemplate.execute(
                "CREATE INDEX idx_activities_combined ON activities(activity_type, ((attributes->>'terrain')::text)) "
                        + "WHERE activity_type = 0" // RUNNING = 0
                );

        // Create index for date filtering
        jdbcTemplate.execute("CREATE INDEX idx_activities_date_range ON activities(start_time)");

        // Update database statistics
        jdbcTemplate.execute("ANALYZE activities");
    }

    private void runPerformanceTests() {
        System.out.println("\n===== Running Performance Tests =====");

        // Create test parameters
        double lat = 40.7128; // NYC
        double lon = -74.0060;
        double radius = 10000.0; // 10km

        // Test different filter combinations
        testLocationFilter(lat, lon, radius);
        testCombinedFilter(lat, lon, radius);
        testComplexFilter(lat, lon, radius);

        // Test with different index configurations
        System.out.println("\n===== Testing with Different Index Configurations =====");
        testWithNoIndexes(lat, lon, radius);
        testWithOnlySpatialIndex(lat, lon, radius);
        testWithAllIndexes(lat, lon, radius);
    }

    private void testLocationFilter(double lat, double lon, double radius) {
        System.out.println("\n----- Testing Location Filter -----");

        ActivityFilter filter = ActivityFilter.builder().lat(lat).lon(lon).radius(radius).build();

        measureQueryPerformance("Location Only", filter, 5);
    }

    private void testCombinedFilter(double lat, double lon, double radius) {
        System.out.println("\n----- Testing Combined Date + Sport Filter -----");

        ActivityFilter filter =
                ActivityFilter.builder()
                        .startDate(LocalDateTime.now().minusDays(7))
                        .endDate(LocalDateTime.now().plusDays(7))
                        .sportType(Sport.RUNNING)
                        .build();

        measureQueryPerformance("Date + Sport", filter, 5);
    }

    private void testComplexFilter(double lat, double lon, double radius) {
        System.out.println("\n----- Testing Complex Filter -----");

        RunningFilter runningFilter =
                RunningFilter.builder()
                        .terrain(RunningTerrain.TRAIL)
                        .minDistance(5.0)
                        .maxDistance(15.0)
                        .minPace(4.0)
                        .maxPace(6.0)
                        .build();

        ActivityFilter filter =
                ActivityFilter.builder()
                        .lat(lat)
                        .lon(lon)
                        .radius(radius)
                        .startDate(LocalDateTime.now().minusDays(7))
                        .endDate(LocalDateTime.now().plusDays(7))
                        .sportType(Sport.RUNNING)
                        .sportFilter(runningFilter)
                        .build();

        measureQueryPerformance("Complex Combined Filter", filter, 5);
    }

    private void testWithNoIndexes(double lat, double lon, double radius) {
        System.out.println("\n----- Testing Complex Filter with No Indexes -----");

        // Drop all indexes
        jdbcTemplate.execute("DROP INDEX IF EXISTS idx_activities_location");
        jdbcTemplate.execute("DROP INDEX IF EXISTS idx_activities_attributes");
        jdbcTemplate.execute("DROP INDEX IF EXISTS idx_running_distance");
        jdbcTemplate.execute("DROP INDEX IF EXISTS idx_activities_combined");
        jdbcTemplate.execute("DROP INDEX IF EXISTS idx_activities_date_range");
        jdbcTemplate.execute("DROP INDEX IF EXISTS idx_activities_location_time_combined");

        RunningFilter runningFilter =
                RunningFilter.builder()
                        .terrain(RunningTerrain.TRAIL)
                        .minDistance(5.0)
                        .maxDistance(15.0)
                        .build();

        ActivityFilter filter =
                ActivityFilter.builder()
                        .lat(lat)
                        .lon(lon)
                        .radius(radius)
                        .startDate(LocalDateTime.now().minusDays(7))
                        .endDate(LocalDateTime.now().plusDays(7))
                        .sportType(Sport.RUNNING)
                        .sportFilter(runningFilter)
                        .build();

        measureQueryPerformance("No Indexes", filter, 3);
    }

    private void testWithOnlySpatialIndex(double lat, double lon, double radius) {
        System.out.println("\n----- Testing Complex Filter with Only Spatial Index -----");

        // Drop all other indexes and create only spatial index
        jdbcTemplate.execute("DROP INDEX IF EXISTS idx_activities_attributes");
        jdbcTemplate.execute("DROP INDEX IF EXISTS idx_running_distance");
        jdbcTemplate.execute("DROP INDEX IF EXISTS idx_activities_combined");
        jdbcTemplate.execute("DROP INDEX IF EXISTS idx_activities_date_range");
        jdbcTemplate.execute("DROP INDEX IF EXISTS idx_activities_location_time_combined");

        // Create spatial index
        jdbcTemplate.execute(
                "CREATE INDEX idx_activities_location ON activities USING GIST(location)");

        RunningFilter runningFilter =
                RunningFilter.builder()
                        .terrain(RunningTerrain.TRAIL)
                        .minDistance(5.0)
                        .maxDistance(15.0)
                        .build();

        ActivityFilter filter =
                ActivityFilter.builder()
                        .lat(lat)
                        .lon(lon)
                        .radius(radius)
                        .startDate(LocalDateTime.now().minusDays(7))
                        .endDate(LocalDateTime.now().plusDays(7))
                        .sportType(Sport.RUNNING)
                        .sportFilter(runningFilter)
                        .build();

        measureQueryPerformance("Only Spatial Index", filter, 3);
    }

    private void testWithAllIndexes(double lat, double lon, double radius) {
        System.out.println("\n----- Testing Complex Filter with All Indexes -----");

        // Create all indexes
        setupIndexes();

        RunningFilter runningFilter =
                RunningFilter.builder()
                        .terrain(RunningTerrain.TRAIL)
                        .minDistance(5.0)
                        .maxDistance(15.0)
                        .build();

        ActivityFilter filter =
                ActivityFilter.builder()
                        .lat(lat)
                        .lon(lon)
                        .radius(radius)
                        .startDate(LocalDateTime.now().minusDays(7))
                        .endDate(LocalDateTime.now().plusDays(7))
                        .sportType(Sport.RUNNING)
                        .sportFilter(runningFilter)
                        .build();

        measureQueryPerformance("All Indexes", filter, 3);
    }

    private void measureQueryPerformance(String testName, ActivityFilter filter, int iterations) {
        // Warm up
        for (int i = 0; i < 3; i++) {
            activityRepository.findWithFilter(filter, PageRequest.of(0, 20));
        }

        // Run the actual performance test
        List<Long> executionTimes = new ArrayList<>();

        for (int i = 0; i < iterations; i++) {
            Instant start = Instant.now();
            var result = activityRepository.findWithFilter(filter, PageRequest.of(0, 20));
            Instant end = Instant.now();

            long executionTime = Duration.between(start, end).toMillis();
            executionTimes.add(executionTime);

            System.out.println(
                    "Run "
                            + (i + 1)
                            + ": "
                            + executionTime
                            + "ms, Found "
                            + result.getTotalElements()
                            + " results");
        }

        // Calculate and print statistics
        double avg = executionTimes.stream().mapToLong(l -> l).average().orElse(0);
        long min = executionTimes.stream().mapToLong(l -> l).min().orElse(0);
        long max = executionTimes.stream().mapToLong(l -> l).max().orElse(0);

        System.out.println("\nResults for " + testName);
        System.out.println("Average: " + avg + "ms");
        System.out.println("Min: " + min + "ms");
        System.out.println("Max: " + max + "ms");
    }
}
