package com.sportsync.activity;

import static org.junit.jupiter.api.Assertions.*;

import com.sportsync.BaseTestWithDatabase;
import com.sportsync.user.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class ActivityRepositoryTest extends BaseTestWithDatabase {

    @Autowired private ActivityRepository activityRepository;

    @Autowired private GeometryFactory geometryFactory;

    @Nested
    class LocationFiltering {

        @Test
        void shouldFindNearbyActivities() {
            User user = persistUser("test@example.com");

            Activity nearby = persistActivity(user, -74.0060, 40.7128, Sport.RUNNING);
            persistActivity(user, 34.0522, -118.2437, Sport.RUNNING); // Far away

            List<Activity> results =
                    activityRepository.findNearby(
                            -74.0060,
                            40.7128,
                            10000,
                            LocalDateTime.now().minusDays(1),
                            LocalDateTime.now().plusDays(1),
                            10);

            assertEquals(1, results.size());
            assertEquals(nearby.getId(), results.getFirst().getId());
        }
    }

    @Nested
    class SportFiltering {
        @Test
        void shouldFilterActivitiesByActivityType() {
            User user = persistUser("filtertest@example.com");

            persistActivity(user, 0, 0, Sport.RUNNING);
            persistActivity(user, 0, 0, Sport.TENNIS);

            ActivityFilter filter = ActivityFilter.builder().sportType(Sport.RUNNING).build();

            // Use findWithFilter instead of specifications
            Page<Activity> result =
                    activityRepository.findWithFilter(filter, PageRequest.of(0, 10));

            assertEquals(1, result.getTotalElements());
            assertEquals(Sport.RUNNING, result.getContent().getFirst().getActivityType());
        }
    }

    @Nested
    class DateFiltering {
        @Test
        void shouldFilterActivitiesByStartDate() {
            User user = persistUser("startdate@example.com");

            persistActivity(user, 0, 0, Sport.RUNNING, LocalDateTime.now().plusDays(1));
            persistActivity(user, 0, 0, Sport.RUNNING, LocalDateTime.now().minusDays(5));

            ActivityFilter filter = ActivityFilter.builder().startDate(LocalDateTime.now()).build();

            // Use findWithFilter instead of specifications
            Page<Activity> result =
                    activityRepository.findWithFilter(filter, PageRequest.of(0, 10));

            assertEquals(1, result.getTotalElements());
            assertTrue(
                    result.getContent()
                            .getFirst()
                            .getStartTime()
                            .isAfter(LocalDateTime.now().minusSeconds(1)));
        }

        @Test
        void shouldFilterActivitiesByEndDate() {
            User user = persistUser("enddate@example.com");

            persistActivity(user, 0, 0, Sport.RUNNING, LocalDateTime.now().plusDays(5));
            persistActivity(user, 0, 0, Sport.RUNNING, LocalDateTime.now().minusDays(1));

            ActivityFilter filter = ActivityFilter.builder().endDate(LocalDateTime.now()).build();

            // Use findWithFilter instead of specifications
            Page<Activity> result =
                    activityRepository.findWithFilter(filter, PageRequest.of(0, 10));

            assertEquals(1, result.getTotalElements());
            assertTrue(
                    result.getContent()
                            .getFirst()
                            .getStartTime()
                            .isBefore(LocalDateTime.now().plusSeconds(1)));
        }
    }

    @Nested
    class AttributeFiltering {
        @Test
        void shouldFilterActivitiesBySportSpecificAttributes() {
            User user = persistUser("sportattr@example.com");

            RunningAttributes trailAttrs =
                    RunningAttributes.builder()
                            .terrain(RunningTerrain.TRAIL)
                            .distance(10.0)
                            .pace(5.0)
                            .build();
            RunningAttributes roadAttrs =
                    RunningAttributes.builder()
                            .terrain(RunningTerrain.GRAVEL)
                            .distance(10.0)
                            .pace(5.0)
                            .build();

            persistActivity(user, 0, 0, Sport.RUNNING, trailAttrs);
            persistActivity(user, 0, 0, Sport.RUNNING, roadAttrs);

            RunningFilter runningFilter =
                    RunningFilter.builder().terrain(RunningTerrain.TRAIL).build();

            ActivityFilter filter = ActivityFilter.builder().sportFilter(runningFilter).build();

            // Use findWithFilter instead of specifications
            Page<Activity> result =
                    activityRepository.findWithFilter(filter, PageRequest.of(0, 10));

            assertEquals(1, result.getTotalElements());

            RunningAttributes attrs =
                    result.getContent()
                            .getFirst()
                            .getAttributesAs(RunningAttributes.class)
                            .orElseThrow(() -> new AssertionError("Expected RunningAttributes"));

            assertEquals(RunningTerrain.TRAIL, attrs.terrain());
        }

        @Test
        void shouldFilterActivitiesByMaxDistance() {
            User user = persistUser("maxdistance@example.com");

            persistActivity(user, 0, 0, Sport.RUNNING, 5.0);
            persistActivity(user, 0, 0, Sport.RUNNING, 15.0);

            RunningFilter runningFilter = RunningFilter.builder().maxDistance(10.0).build();

            ActivityFilter filter = ActivityFilter.builder().sportFilter(runningFilter).build();

            // Use findWithFilter instead of specifications
            Page<Activity> result =
                    activityRepository.findWithFilter(filter, PageRequest.of(0, 10));

            assertEquals(1, result.getTotalElements());

            RunningAttributes attrs =
                    result.getContent()
                            .getFirst()
                            .getAttributesAs(RunningAttributes.class)
                            .orElseThrow(() -> new AssertionError("Expected RunningAttributes"));

            assertTrue(attrs.distance() <= 10.0);
        }

        @Test
        void shouldFilterActivitiesByCombinedRunningAttributes() {
            User user = persistUser("combinedfilter@example.com");

            RunningAttributes matchingAttrs =
                    RunningAttributes.builder()
                            .terrain(RunningTerrain.TRAIL)
                            .distance(8.0)
                            .pace(5.0)
                            .build();
            RunningAttributes nonMatchingAttrs =
                    RunningAttributes.builder()
                            .terrain(RunningTerrain.GRAVEL)
                            .distance(15.0)
                            .pace(7.0)
                            .build();

            persistActivity(user, 0, 0, Sport.RUNNING, matchingAttrs);
            persistActivity(user, 0, 0, Sport.RUNNING, nonMatchingAttrs);

            RunningFilter runningFilter =
                    RunningFilter.builder()
                            .terrain(RunningTerrain.TRAIL)
                            .maxDistance(10.0)
                            .maxPace(6.0)
                            .build();

            ActivityFilter filter = ActivityFilter.builder().sportFilter(runningFilter).build();

            Page<Activity> result =
                    activityRepository.findWithFilter(filter, PageRequest.of(0, 10));

            assertEquals(1, result.getTotalElements());

            RunningAttributes attrs =
                    result.getContent()
                            .getFirst()
                            .getAttributesAs(RunningAttributes.class)
                            .orElseThrow(() -> new AssertionError("Expected RunningAttributes"));

            assertEquals(RunningTerrain.TRAIL, attrs.terrain());
            assertTrue(attrs.distance() <= 10.0);
            assertTrue(attrs.pace() <= 6.0);
        }
    }

    @Nested
    @DisplayName("Running Filter Tests")
    class RunningFilterTests {
        @ParameterizedTest(
                name = "{index} => pace: {0}, distance: {2}, terrain: {3} -> expected {4}")
        @MethodSource("runningFilterTestCases")
        void shouldFilterActivitiesByCombinedRunningAttributes(RunningFilterTestCase testCase) {
            User user = persistUser("methodsource@example.com");

            // Prepare activities
            persistActivity(
                    user,
                    0,
                    0,
                    Sport.RUNNING,
                    RunningAttributes.builder()
                            .pace(5.0)
                            .distance(12.0)
                            .terrain(RunningTerrain.TRAIL)
                            .build());

            persistActivity(
                    user,
                    0,
                    0,
                    Sport.RUNNING,
                    RunningAttributes.builder()
                            .pace(5.0)
                            .distance(12.0)
                            .terrain(RunningTerrain.GRAVEL)
                            .build());

            persistActivity(
                    user,
                    0,
                    0,
                    Sport.RUNNING,
                    RunningAttributes.builder()
                            .pace(7.0)
                            .distance(8.0)
                            .terrain(RunningTerrain.TRAIL)
                            .build());

            RunningFilter runningFilter =
                    RunningFilter.builder()
                            .minPace(testCase.minPace())
                            .maxPace(testCase.maxPace())
                            .minDistance(testCase.minDistance())
                            .terrain(testCase.terrain())
                            .build();

            ActivityFilter filter = ActivityFilter.builder().sportFilter(runningFilter).build();

            // Use findWithFilter instead of specifications
            Page<Activity> result =
                    activityRepository.findWithFilter(filter, PageRequest.of(0, 10));

            assertEquals(testCase.expectedMatches(), result.getTotalElements());
        }

        static Stream<RunningFilterTestCase> runningFilterTestCases() {
            return Stream.of(
                    new RunningFilterTestCase(4.0, 6.0, 10.0, RunningTerrain.TRAIL, 1),
                    new RunningFilterTestCase(4.0, 6.0, 10.0, RunningTerrain.GRAVEL, 1),
                    new RunningFilterTestCase(4.0, 6.0, 15.0, RunningTerrain.TRAIL, 0),
                    new RunningFilterTestCase(6.0, 8.0, 10.0, RunningTerrain.TRAIL, 0));
        }
    }

    @Nested
    @DisplayName("Combined Filtering Tests")
    class CombinedFilteringTest {

        @Test
        @DisplayName("Should filter by location, date range, and running attributes")
        void shouldCombineLocationDateAndAttributeFilters() {
            // Create a user
            var user = persistUser("combined@example.com");

            // Create activities with different attributes and locations

            // Activity 1: Matching - nearby, within date range, matching running attributes
            RunningAttributes matchingAttrs =
                    RunningAttributes.builder()
                            .terrain(RunningTerrain.TRAIL)
                            .distance(8.0)
                            .pace(5.0)
                            .build();

            var activity1 =
                    persistActivity(
                            user,
                            40.7128, // NYC coordinates (nearby)
                            -74.0060,
                            Sport.RUNNING,
                            matchingAttrs,
                            LocalDateTime.now().plusDays(1) // within date range
                            );

            // Activity 2: Non-matching - nearby but wrong terrain
            RunningAttributes wrongTerrainAttrs =
                    RunningAttributes.builder()
                            .terrain(RunningTerrain.ASPHALT)
                            .distance(8.0)
                            .pace(5.0)
                            .build();

            persistActivity(
                    user,
                    40.7130, // NYC coordinates (nearby)
                    -74.0065,
                    Sport.RUNNING,
                    wrongTerrainAttrs,
                    LocalDateTime.now().plusDays(1));

            // Activity 3: Non-matching - right attributes but outside date range
            persistActivity(
                    user,
                    40.7135,
                    -74.0070,
                    Sport.RUNNING,
                    matchingAttrs,
                    LocalDateTime.now().plusDays(10) // outside date range
                    );

            // Activity 4: Non-matching - right attributes but too far away
            persistActivity(
                    user,
                    34.0522, // Los Angeles coordinates (far away)
                    -118.2437,
                    Sport.RUNNING,
                    matchingAttrs,
                    LocalDateTime.now().plusDays(1));

            // Create the filter combining location, date and running attributes
            RunningFilter runningFilter =
                    RunningFilter.builder()
                            .terrain(RunningTerrain.TRAIL)
                            .maxDistance(10.0)
                            .maxPace(6.0)
                            .build();

            ActivityFilter filter =
                    ActivityFilter.builder()
                            .lat(40.7128) // NYC
                            .lon(-74.0060)
                            .radius(10000.0) // 10km radius
                            .startDate(LocalDateTime.now())
                            .endDate(LocalDateTime.now().plusDays(5))
                            .sportType(Sport.RUNNING)
                            .sportFilter(runningFilter)
                            .build();

            // Execute the filter
            Page<Activity> result =
                    activityRepository.findWithFilter(filter, PageRequest.of(0, 10));

            // Verify results
            assertEquals(1, result.getTotalElements(), "Should find exactly one matching activity");
            assertEquals(
                    activity1.getId(),
                    result.getContent().getFirst().getId(),
                    "The matching activity should be the one with the correct attributes, location and date");

            // Verify activity properties
            var foundActivity = result.getContent().getFirst();
            assertTrue(foundActivity.getStartTime().isAfter(LocalDateTime.now()));
            assertTrue(foundActivity.getStartTime().isBefore(LocalDateTime.now().plusDays(5)));

            RunningAttributes attrs =
                    foundActivity
                            .getAttributesAs(RunningAttributes.class)
                            .orElseThrow(() -> new AssertionError("Expected RunningAttributes"));

            assertEquals(RunningTerrain.TRAIL, attrs.terrain());
            assertTrue(attrs.distance() <= 10.0);
            assertTrue(attrs.pace() <= 6.0);
        }
    }

    // --- Helpers ---

    private User persistUser(String email) {
        User user = User.builder().email(email).password("password").name("Test User").build();
        entityManager.persist(user);
        return user;
    }

    private Activity persistActivity(
            User creator,
            double lat,
            double lon,
            Sport sport,
            RunningAttributes attrs,
            LocalDateTime startTime) {
        Activity activity =
                Activity.builder()
                        .title("Test Activity")
                        .activityType(sport)
                        .startTime(startTime)
                        .endTime(startTime.plusHours(2))
                        .maxParticipants(10)
                        .location(geometryFactory.createPoint(new Coordinate(lon, lat)))
                        .attributes(attrs)
                        .creator(creator)
                        .build();
        entityManager.persist(activity);
        return activity;
    }

    private Activity persistActivity(User creator, double lat, double lon, Sport sport) {
        return persistActivity(creator, lat, lon, sport, 10.0);
    }

    private Activity persistActivity(
            User creator, double lat, double lon, Sport sport, double distance) {
        return persistActivity(creator, lat, lon, sport, distance, LocalDateTime.now().plusDays(1));
    }

    private Activity persistActivity(
            User creator, double lat, double lon, Sport sport, LocalDateTime startTime) {
        return persistActivity(creator, lat, lon, sport, 10.0, startTime);
    }

    private Activity persistActivity(
            User creator,
            double lat,
            double lon,
            Sport sport,
            double distance,
            LocalDateTime startTime) {
        Activity activity =
                Activity.builder()
                        .title("Test Activity")
                        .activityType(sport)
                        .startTime(startTime)
                        .endTime(startTime.plusHours(2))
                        .maxParticipants(10)
                        .location(geometryFactory.createPoint(new Coordinate(lon, lat)))
                        .attributes(
                                RunningAttributes.builder()
                                        .distance(distance)
                                        .pace(5.0)
                                        .terrain(RunningTerrain.TRAIL)
                                        .build())
                        .creator(creator)
                        .build();
        entityManager.persist(activity);
        return activity;
    }

    private Activity persistActivity(
            User creator, double lat, double lon, Sport sport, RunningAttributes attrs) {
        Activity activity =
                Activity.builder()
                        .title("Test Activity")
                        .activityType(sport)
                        .startTime(LocalDateTime.now().plusHours(1))
                        .endTime(LocalDateTime.now().plusHours(3))
                        .maxParticipants(10)
                        .location(geometryFactory.createPoint(new Coordinate(lon, lat)))
                        .attributes(attrs)
                        .creator(creator)
                        .build();
        entityManager.persist(activity);
        return activity;
    }

    record RunningFilterTestCase(
            double minPace,
            double maxPace,
            double minDistance,
            RunningTerrain terrain,
            int expectedMatches) {}
}
