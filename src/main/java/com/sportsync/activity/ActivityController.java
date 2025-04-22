package com.sportsync.activity;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityService activityService;
    private final ActivityFilterMapper filterMapper;

    @PostMapping
    public ResponseEntity<Activity> createActivity(
            @Valid @RequestBody ActivityCreateDto dto, @RequestHeader("X-User-ID") UUID userId) {
        return ResponseEntity.ok(activityService.createActivity(dto, userId));
    }

    /** GET endpoint for simple filtering with query parameters */
    @GetMapping
    public ResponseEntity<Page<Activity>> getActivities(
            @RequestParam(required = false) Sport sportType,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @PageableDefault Pageable pageable) {

        // For simple queries, build a basic filter
        ActivityFilter filter =
                ActivityFilter.builder().startDate(startDate).endDate(endDate).build();

        return ResponseEntity.ok(activityService.filterActivities(filter, pageable));
    }

    @PostMapping("/filter")
    public ResponseEntity<Page<Activity>> filterActivities(
            @Valid @RequestBody ActivityFilterDto filterDto, @PageableDefault Pageable pageable) {

        ActivityFilter filter = filterMapper.toActivityFilter(filterDto);

        return ResponseEntity.ok(activityService.filterActivities(filter, pageable));
    }

    @PostMapping("/{activityId}/join")
    public ResponseEntity<Void> joinActivity(
            @PathVariable Long activityId, @RequestHeader("X-User-ID") UUID userId) {
        activityService.joinActivity(activityId, userId);
        return ResponseEntity.ok().build();
    }
}
