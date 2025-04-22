package com.sportsync.activity;

import java.util.List;
import java.util.Map;

/** Base interface for all filters that can provide JPQL predicates */
public interface JpqlFilterable {
    /**
     * Builds JPQL predicate fragments for this filter
     *
     * @param params Parameter map to populate with values
     * @return JPQL string fragments for the WHERE clause
     */
    List<String> toJpqlPredicates(Map<String, Object> params);

    /** Returns the sport type this filter applies to */
    Sport getSportType();
}
