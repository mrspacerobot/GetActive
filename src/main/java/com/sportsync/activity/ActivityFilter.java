package com.sportsync.activity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Builder;

/** Main filter record for activities that combines common and sport-specific predicates */
@Builder
public record ActivityFilter(
        LocalDateTime startDate,
        LocalDateTime endDate,
        Double lat,
        Double lon,
        Double radius,
        Sport sportType,
        JpqlFilterable sportFilter)
        implements JpqlFilterable {

    @Override
    public List<String> toJpqlPredicates(Map<String, Object> params) {
        List<String> predicates = new ArrayList<>();

        // Add common predicates
        if (startDate != null) {
            predicates.add("a.start_time >= :startDate");
            params.put("startDate", startDate);
        }

        if (endDate != null) {
            predicates.add("a.start_time <= :endDate");
            params.put("endDate", endDate);
        }

        if (lat != null && lon != null && radius != null) {
            // Use PostGIS for spatial querying
            // radius is expected to be in meters, we convert to degrees for ST_DWithin
            String locationPredicate =
                    "ST_DWithin(a.location, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326), :radius * 0.00001)";
            predicates.add(locationPredicate);
            params.put("lat", lat);
            params.put("lon", lon);
            params.put("radius", radius); // Radius in meters
        }

        // Add sport-specific predicates if we have a sport filter
        if (sportFilter != null) {
            predicates.addAll(sportFilter.toJpqlPredicates(params));
        }

        return predicates;
    }

    @Override
    public Sport getSportType() {
        return sportType;
    }
}
