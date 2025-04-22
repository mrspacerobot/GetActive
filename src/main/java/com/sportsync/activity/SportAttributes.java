package com.sportsync.activity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@type" // Using @type to clearly indicate this is a type discriminator
    )
@JsonSubTypes({
  @JsonSubTypes.Type(value = RunningAttributes.class, name = "RUNNING"),
  // Add other sport types as needed
})
public interface SportAttributes {
  @JsonIgnore
  Sport getSportType();
}
