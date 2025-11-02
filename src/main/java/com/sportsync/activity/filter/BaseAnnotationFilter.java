package com.sportsync.activity.filter;
import com.sportsync.activity.PredicateBuilder;
import com.sportsync.activity.filter.JpqlFilterable;

import java.util.ArrayList;
import java.util.Map;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class BaseAnnotationFilter implements JpqlFilterable {

    @Override
    public List<String> toJpqlPredicates(Map<String, Object> params) {
        List<String> predicates = new ArrayList<>();

        Field[] fields = this.getClass().getDeclaredFields();

        for (Field field : fields) {
            FilterField annotation = field.getAnnotation(FilterField.class);
            if (annotation != null) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(this);

                    if (value != null) {
                        String predicate = createPredicateFromAnnotation(annotation, value, params);
                        predicates.add(predicate);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to access field: " + field.getName(), e);
                }
            }
        }

        return predicates;
    }

    protected String createPredicateFromAnnotation(FilterField annotation, Object value, Map<String, Object> params) {
        return switch (annotation.type()) {
            case DOUBLE -> PredicateBuilder.createDoublePredicate(
                    annotation.attribute(), (Double) value, annotation.operator(),
                    annotation.paramName(), getSportType(), params);
            case STRING -> PredicateBuilder.createStringPredicate(
                    annotation.attribute(), value, annotation.paramName(), getSportType(), params);
            case INTEGER -> PredicateBuilder.createIntegerPredicate(
                    annotation.attribute(), (Integer) value, annotation.operator(),
                    annotation.paramName(), getSportType(), params);
            case BOOLEAN -> PredicateBuilder.createBooleanPredicate(
                    annotation.attribute(), (Boolean) value, annotation.paramName(), getSportType(), params);
            case ENUM -> PredicateBuilder.createStringPredicate(
                    annotation.attribute(), value, annotation.paramName(), getSportType(), params);
        };
    }
}