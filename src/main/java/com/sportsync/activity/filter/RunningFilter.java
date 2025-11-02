package com.sportsync.activity.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sportsync.activity.PredicateBuilder;
import com.sportsync.activity.RunningTerrain;
import com.sportsync.activity.Sport;
import lombok.Builder;

/** Filter for running activities that generates its own predicates */
@Builder
public record RunningFilter(
        @FilterField(attribute = "pace", operator = ">=", paramName = "minPace", type = PredicateType.DOUBLE)
        Double minPace,

        @FilterField(attribute = "pace", operator = "<=", paramName = "maxPace", type = PredicateType.DOUBLE)
        Double maxPace,

        @FilterField(attribute = "distance", operator = ">=", paramName = "minDistance", type = PredicateType.DOUBLE)
        Double minDistance,

        @FilterField(attribute = "distance", operator = "<=", paramName = "maxDistance", type = PredicateType.DOUBLE)
        Double maxDistance,

        @FilterField(attribute = "terrain", operator = "=", paramName = "terrain", type = PredicateType.ENUM)
        RunningTerrain terrain
) implements JpqlFilterable{

  private static final String PACE_ATTRIBUTE = "pace";
  private static final String DISTANCE_ATTRIBUTE = "distance";
  private static final String TERRAIN_ATTRIBUTE = "terrain";

  @Override
  public List<String> toJpqlPredicates(Map<String, Object> params) {

  }

  @Override
  public Sport getSportType() {
    return Sport.RUNNING;
  }
}
