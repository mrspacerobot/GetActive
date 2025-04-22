package com.sportsync.activity;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.NonNull;

@Builder
@JsonTypeName("RUNNING")
public record RunningAttributes(
    @NonNull @Positive Double pace,
    @NonNull @Positive Double distance,
    @NonNull RunningTerrain terrain)
    implements SportAttributes {
  @Override
  public Sport getSportType() {
    return Sport.RUNNING;
  }
}
