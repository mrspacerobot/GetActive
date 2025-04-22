package com.sportsync.activity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ActivityRepositoryCustom {
    Page<Activity> findWithFilter(ActivityFilter filter, Pageable pageable);
}
