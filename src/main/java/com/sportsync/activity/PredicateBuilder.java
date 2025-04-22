package com.sportsync.activity;

import java.util.Map;

/**
 * Utility class for building SQL predicates for JSONB attributes This class provides helper methods
 * for all sport filters to use
 */
public class PredicateBuilder {

    /**
     * Creates a parameter name with a sport-specific prefix to avoid conflicts
     *
     * @param sportType The sport type
     * @param paramName Base parameter name
     * @return Prefixed parameter name
     */
    public static String prefixedParam(Sport sportType, String paramName) {
        return sportType.name().toLowerCase() + "_" + paramName;
    }

    /**
     * Creates a JPQL predicate for a double precision JSONB attribute
     *
     * @param attributeName Name of the attribute in the JSONB
     * @param value Value to compare against
     * @param operator Comparison operator (e.g., ">=", "<=", "=")
     * @param paramName Name for the parameter (will be prefixed)
     * @param sportType The sport type for parameter prefix
     * @param params Parameter map to populate
     * @return JPQL predicate string
     */
    public static String createDoublePredicate(
            String attributeName,
            Double value,
            String operator,
            String paramName,
            Sport sportType,
            Map<String, Object> params) {
        String prefixedParamName = prefixedParam(sportType, paramName);
        params.put(prefixedParamName, value);
        return String.format(
                "(a.attributes->>'%s')::double precision %s :%s",
                attributeName, operator, prefixedParamName);
    }

    /**
     * Creates a JPQL predicate for a string JSONB attribute
     *
     * @param attributeName Name of the attribute in the JSONB
     * @param value Value to compare against
     * @param paramName Name for the parameter (will be prefixed)
     * @param sportType The sport type for parameter prefix
     * @param params Parameter map to populate
     * @return JPQL predicate string
     */
    public static String createStringPredicate(
            String attributeName,
            Object value,
            String paramName,
            Sport sportType,
            Map<String, Object> params) {
        String prefixedParamName = prefixedParam(sportType, paramName);
        params.put(prefixedParamName, value.toString());
        return String.format("a.attributes->>'%s' = :%s", attributeName, prefixedParamName);
    }
}
