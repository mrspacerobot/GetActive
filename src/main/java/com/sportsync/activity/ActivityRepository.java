package com.sportsync.activity;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityRepository
        extends JpaRepository<Activity, Long>, ActivityRepositoryCustom {

    /**
     * @Query( "SELECT a FROM Activity a WHERE " + "to_tsvector('english', a.title || ' ' ||
     * a.description) @@ to_tsquery('english', :query)") Page<Activity> search(@Param("query")
     * String query, Pageable pageable);
     */
    // Location-based search
    @Query(
            value =
                    "SELECT * FROM activities WHERE "
                            + "ST_DWithin(location, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326), :distance * 0.00001) AND "
                            + "start_time >= :start AND start_time <= :end "
                            + "ORDER BY ST_Distance(location, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)) "
                            + "LIMIT :limit",
            nativeQuery = true)
    List<Activity> findNearby(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("distance") double distance, // in meters
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("limit") int limit);

    Page<Activity> findByActivityType(String activityType, Pageable pageable);
}
