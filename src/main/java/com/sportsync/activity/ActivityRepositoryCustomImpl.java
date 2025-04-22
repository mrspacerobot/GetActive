package com.sportsync.activity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Custom implementation of ActivityRepositoryCustom Provides filtering capabilities using native
 * SQL queries
 */
@Component
public class ActivityRepositoryCustomImpl implements ActivityRepositoryCustom {

    @PersistenceContext private EntityManager entityManager;

    @Override
    public Page<Activity> findWithFilter(ActivityFilter filter, Pageable pageable) {
        Map<String, Object> params = new HashMap<>();
        String whereClause = buildWhereClause(filter, params);

        // Execute count query first
        long total = executeCountQuery(whereClause, params);

        // Then execute main query with results
        List<Activity> activities = executeMainQuery(whereClause, params, filter, pageable);

        return new PageImpl<>(activities, pageable, total);
    }

    /** Builds WHERE clause from filter predicates */
    private String buildWhereClause(ActivityFilter filter, Map<String, Object> params) {
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1");

        // Add sport type predicate if present
        if (filter.getSportType() != null) {
            whereClause.append(" AND a.activity_type = :activityType");
            params.put("activityType", filter.getSportType().ordinal());
        }

        // Add all predicates from the filter
        for (String predicate : filter.toJpqlPredicates(params)) {
            whereClause.append(" AND ").append(predicate);
        }

        return whereClause.toString();
    }

    /** Executes count query to get total number of matching records */
    private long executeCountQuery(String whereClause, Map<String, Object> params) {
        String countSql = "SELECT COUNT(*) FROM activities a" + whereClause;
        Query countQuery = entityManager.createNativeQuery(countSql);
        setQueryParams(countQuery, params);
        Number countResult = (Number) countQuery.getSingleResult();
        return countResult.longValue();
    }

    /** Executes main query to get actual activity records */
    private List<Activity> executeMainQuery(
            String whereClause,
            Map<String, Object> params,
            ActivityFilter filter,
            Pageable pageable) {
        String mainSql =
                "SELECT * FROM activities a"
                        + whereClause
                        + buildOrderClause(filter, pageable)
                        + " LIMIT :limit OFFSET :offset";

        Query query = entityManager.createNativeQuery(mainSql, Activity.class);
        setQueryParams(query, params);
        query.setParameter("limit", pageable.getPageSize());
        query.setParameter("offset", pageable.getOffset());

        @SuppressWarnings("unchecked")
        List<Activity> activities = query.getResultList();

        return activities;
    }

    /**
     * Builds ORDER BY clause from pageable sort information and location-based sorting if
     * applicable
     */
    private String buildOrderClause(ActivityFilter filter, Pageable pageable) {
        // If location-based filtering is being used, prioritize distance-based sorting
        if (filter.lat() != null && filter.lon() != null) {
            return " ORDER BY ST_Distance(a.location, ST_SetSRID(ST_MakePoint("
                    + filter.lon()
                    + ", "
                    + filter.lat()
                    + "), 4326))";
        }

        // Otherwise use pageable sorting
        if (!pageable.getSort().isSorted()) {
            return " ORDER BY a.created_at DESC";
        }

        StringJoiner sortJoiner = new StringJoiner(", ");
        pageable.getSort()
                .forEach(
                        order -> {
                            String property = mapPropertyToColumnName(order.getProperty());
                            String direction = order.getDirection().name();
                            sortJoiner.add(property + " " + direction);
                        });

        return " ORDER BY " + sortJoiner;
    }

    /** Sets all parameters on a query from a parameter map */
    private void setQueryParams(Query query, Map<String, Object> params) {
        params.forEach(query::setParameter);
    }

    /**
     * Maps Java property names to database column names Converts camelCase to snake_case for column
     * names
     */
    private String mapPropertyToColumnName(String property) {
        return property.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
