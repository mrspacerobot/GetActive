package com.sportsync.activity;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ActivityService {
    /** Creates a new activity */
    Activity createActivity(ActivityCreateDto dto, UUID creatorId);

    /** Gets all activities with pagination */
    Page<Activity> getAllActivities(Pageable pageable);

    /** Filters activities based on provided criteria */
    Page<Activity> filterActivities(ActivityFilter filterDto, Pageable pageable);

    /** Finds activities near a geographical location */
    Page<Activity> findNearbyActivities(
            Double lat,
            Double lon,
            Double distance,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable);

    /** Allows a user to join an existing activity */
    void joinActivity(Long activityId, UUID userId);
}
