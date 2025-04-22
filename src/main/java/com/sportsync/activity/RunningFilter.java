package com.sportsync.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Builder;

/** Filter for running activities that generates its own predicates */
@Builder
public record RunningFilter(
        Double minPace,
        Double maxPace,
        Double minDistance,
        Double maxDistance,
        RunningTerrain terrain)
        implements JpqlFilterable {

    private static final String PACE_ATTRIBUTE = "pace";
    private static final String DISTANCE_ATTRIBUTE = "distance";
    private static final String TERRAIN_ATTRIBUTE = "terrain";

    @Override
    public List<String> toJpqlPredicates(Map<String, Object> params) {
        List<String> predicates = new ArrayList<>();

        // Generate predicates for non-null filter values
        if (minPace != null) {
            predicates.add(
                    PredicateBuilder.createDoublePredicate(
                            PACE_ATTRIBUTE, minPace, ">=", "minPace", getSportType(), params));
        }

        if (maxPace != null) {
            predicates.add(
                    PredicateBuilder.createDoublePredicate(
                            PACE_ATTRIBUTE, maxPace, "<=", "maxPace", getSportType(), params));
        }

        if (minDistance != null) {
            predicates.add(
                    PredicateBuilder.createDoublePredicate(
                            DISTANCE_ATTRIBUTE,
                            minDistance,
                            ">=",
                            "minDistance",
                            getSportType(),
                            params));
        }

        if (maxDistance != null) {
            predicates.add(
                    PredicateBuilder.createDoublePredicate(
                            DISTANCE_ATTRIBUTE,
                            maxDistance,
                            "<=",
                            "maxDistance",
                            getSportType(),
                            params));
        }

        if (terrain != null) {
            predicates.add(
                    PredicateBuilder.createStringPredicate(
                            TERRAIN_ATTRIBUTE, terrain, "terrain", getSportType(), params));
        }

        return predicates;
    }

    @Override
    public Sport getSportType() {
        return Sport.RUNNING;
    }
}
