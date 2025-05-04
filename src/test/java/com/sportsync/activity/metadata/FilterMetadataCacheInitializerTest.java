package com.sportsync.activity.metadata;

import static org.junit.jupiter.api.Assertions.*;

import com.sportsync.BaseTestWithDatabase;
import com.sportsync.activity.Sport;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

class FilterMetadataCacheInitializerTest extends BaseTestWithDatabase {

  @Autowired private FilterMetadataService metadataService;

  @Autowired private CacheManager cacheManager;

  @Test
  void cacheShouldBePreloadedForSupportedSports() {
    Cache cache = cacheManager.getCache("filterMetadata");
    assertNotNull(cache);

    for (Sport sport : Sport.values()) {
      try {
        List<FilterMetadataField> expected = metadataService.getFilterMetadata(sport);
        Cache.ValueWrapper value = cache.get(sport.name());

        if (expected != null && !expected.isEmpty()) {
          assertNotNull(value, "Expected metadata to be cached for: " + sport);
        }
      } catch (IllegalArgumentException ignored) {
        // Skip unsupported sports
      }
    }
  }
}
