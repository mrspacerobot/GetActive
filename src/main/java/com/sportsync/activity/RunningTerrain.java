package com.sportsync.activity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RunningTerrain {
  ASPHALT,
  GRAVEL,
  TRAIL;

  @JsonCreator
  public static RunningTerrain fromString(String value) {
    for (RunningTerrain terrain : RunningTerrain.values()) {
      if (terrain.name().equalsIgnoreCase(value)) {
        return terrain;
      }
    }
    throw new IllegalArgumentException("Unknown terrain type: " + value);
  }

  @JsonValue
  public String toJson() {
    return name().toLowerCase();
  }

  @Override
  public String toString() {
    return toJson();
  }
}
