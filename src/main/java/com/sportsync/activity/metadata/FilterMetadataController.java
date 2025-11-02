package com.sportsync.activity.metadata;

import com.sportsync.activity.Sport;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/activity/filterMetadata")
@RequiredArgsConstructor
public class FilterMetadataController {
  private final FilterMetadataService filterMetadataService;

  @GetMapping
  public List<FilterMetadataField> getFilterMetadataField(
      @RequestParam(required = true) Sport sport) {
    return filterMetadataService.getFilterMetadata(sport);
  }
}
