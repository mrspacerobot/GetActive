package com.sportsync.activity;

import com.sportsync.user.User;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ActivityDtoConverter {
    private final GeometryFactory geometryFactory;

    Activity convertDtoToActivity(ActivityCreateDto dto, User user) {
        return Activity.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .activityType(Sport.valueOf(dto.getActivityType()))
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .maxParticipants(dto.getMaxParticipants())
                .location(
                        geometryFactory.createPoint(
                                new Coordinate(dto.getLongitude(), dto.getLatitude())))
                .creator(user)
                .build();
    }
}
