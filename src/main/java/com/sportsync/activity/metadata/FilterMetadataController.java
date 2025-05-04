package com.sportsync.activity.metadata;

import com.sportsync.activity.Sport;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/v1/activity/filterMetadata")
@RequiredArgsConstructor
public class FilterMetadataController {
  private FilterMetadataService filterMetadataService;

  @GetMapping
  public List<FilterMetadataField> getFilterMetadataField(
      @RequestParam(required = true) Sport sport) {
    return filterMetadataService.getFilterMetadata(sport);
  }
}
