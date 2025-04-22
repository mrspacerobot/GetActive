package com.sportsync.activity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class SportAttributesConverter implements AttributeConverter<SportAttributes, String> {
  private static final ObjectMapper objectMapper =
      new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  static {
    objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
  }

  @Override
  public String convertToDatabaseColumn(SportAttributes attributes) {
    if (attributes == null) {
      return null;
    }

    try {
      return objectMapper.writeValueAsString(attributes);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize sport attributes", e);
    }
  }

  @Override
  public SportAttributes convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isEmpty()) {
      return null;
    }

    try {
      return objectMapper.readValue(dbData, SportAttributes.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to deserialize sport attributes", e);
    }
  }
}
