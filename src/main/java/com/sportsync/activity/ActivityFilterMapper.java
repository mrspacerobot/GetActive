package com.sportsync.activity;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ActivityFilterMapper {

    private final ObjectMapper objectMapper;

    public ActivityFilterMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** Maps the API DTO to the internal domain filter */
    public ActivityFilter toActivityFilter(ActivityFilterDto dto) {
        ActivityFilter.ActivityFilterBuilder builder =
                ActivityFilter.builder()
                        .startDate(dto.getStartDate())
                        .endDate(dto.getEndDate())
                        .lat(dto.getLat())
                        .lon(dto.getLon());

        // Handle sport specific filters
        if (dto.getSportType() != null && dto.getSportSpecificFilter() != null) {
            try {
                // Add the @type property for Jackson to determine the concrete type
                Map<String, Object> filterMap = dto.getSportSpecificFilter();

                JpqlFilterable sportFilter = createSportFilter(dto.getSportType(), filterMap);
                builder.sportFilter(sportFilter);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid sport-specific filter data", e);
            }
        }

        return builder.build();
    }

    private JpqlFilterable createSportFilter(Sport sportType, Map<String, Object> filterMap) {
        switch (sportType) {
            case RUNNING:
                RunningFilter.RunningFilterBuilder runningBuilder = RunningFilter.builder();

                if (filterMap.containsKey("minPace")) {
                    runningBuilder.minPace(convertToDouble(filterMap.get("minPace")));
                }
                if (filterMap.containsKey("maxPace")) {
                    runningBuilder.maxPace(convertToDouble(filterMap.get("maxPace")));
                }
                if (filterMap.containsKey("minDistance")) {
                    runningBuilder.minDistance(convertToDouble(filterMap.get("minDistance")));
                }
                if (filterMap.containsKey("maxDistance")) {
                    runningBuilder.maxDistance(convertToDouble(filterMap.get("maxDistance")));
                }
                if (filterMap.containsKey("terrain")) {
                    runningBuilder.terrain(
                            RunningTerrain.valueOf(filterMap.get("terrain").toString()));
                }

                return runningBuilder.build();

                // Add other sport types as needed
            default:
                throw new IllegalArgumentException("Unsupported sport type: " + sportType);
        }
    }

    private Double convertToDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(value.toString());
    }
}
