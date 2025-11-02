package com.sportsync.activity.filter;

import com.sportsync.activity.RunningTerrain;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ActivityFilterService {

    private final FilterPredicateProcessor filterPredicateProcessor;

    public ActivityFilterService(FilterPredicateProcessor filterPredicateProcessor) {
        this.filterPredicateProcessor = filterPredicateProcessor;
    }

    public List<String> generateFilterPredicates(JpqlFilterable filter, Map<String, Object> params) {
        return filterPredicateProcessor.generatePredicates(filter, params);
    }

    // Create filters and process them
    public List<String> createRunningFilterPredicates(Map<String, Object> filterData) {
        RunningFilter filter = RunningFilter.builder()
                .minPace(extractDouble(filterData, "minPace"))
                .maxPace(extractDouble(filterData, "maxPace"))
                .minDistance(extractDouble(filterData, "minDistance"))
                .maxDistance(extractDouble(filterData, "maxDistance"))
                .terrain(extractEnum(filterData, "terrain", RunningTerrain.class))
                .build();

        Map<String, Object> params = new HashMap<>();
        return generateFilterPredicates(filter, params);
    }

    private Double extractDouble(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value instanceof Number ? ((Number) value).doubleValue() : null;
    }

    private <E extends Enum<E>> E extractEnum(Map<String, Object> data, String key, Class<E> enumClass) {
        Object value = data.get(key);
        return value != null ? Enum.valueOf(enumClass, value.toString()) : null;
    }
}
