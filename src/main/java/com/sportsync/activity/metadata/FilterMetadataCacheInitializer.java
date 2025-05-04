package com.sportsync.activity.metadata;

import com.sportsync.activity.Sport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilterMetadataCacheInitializer {

  private final FilterMetadataService metadataService;

  @EventListener(ApplicationReadyEvent.class)
  public void preloadFilterMetadata() {
    for (Sport sport : Sport.values()) {
      try {
        metadataService.getFilterMetadata(sport);
        log.info("Preloaded metadata cache for sport: {}", sport);
      } catch (IllegalArgumentException ignored) {
        log.info("No metadata to cache for sport: {}", sport);
      }
    }
  }
}
