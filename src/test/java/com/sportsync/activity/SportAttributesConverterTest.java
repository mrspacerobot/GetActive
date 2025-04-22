package com.sportsync.activity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class SportAttributesConverterTest {

    private final SportAttributesConverter converter = new SportAttributesConverter();

    @Nested
    @DisplayName("Serialization and Deserialization Tests")
    class SerializationDeserialization {

        @Test
        @DisplayName("Should correctly serialize and deserialize RunningAttributes")
        void shouldSerializeAndDeserializeRunningAttributes() {
            // Given
            RunningAttributes original =
                    RunningAttributes.builder()
                            .pace(4.8)
                            .distance(12.5)
                            .terrain(RunningTerrain.ASPHALT)
                            .build();

            // When
            String serialized = converter.convertToDatabaseColumn(original);
            SportAttributes deserialized = converter.convertToEntityAttribute(serialized);

            // Then
            assertThat(serialized)
                    .isNotNull()
                    .contains("\"@type\":\"RUNNING\"")
                    .contains("pace", "distance", "terrain");

            assertThat(deserialized).isInstanceOf(RunningAttributes.class);

            RunningAttributes result = (RunningAttributes) deserialized;
            assertThat(result.pace()).isEqualTo(original.pace());
            assertThat(result.distance()).isEqualTo(original.distance());
            assertThat(result.terrain()).isEqualTo(original.terrain());
        }
    }

    @Nested
    @DisplayName("Null and Empty Handling Tests")
    class NullAndEmptyHandling {

        @Test
        @DisplayName("Should return null when serializing null")
        void shouldReturnNullWhenSerializingNull() {
            assertThat(converter.convertToDatabaseColumn(null)).isNull();
        }

        @Test
        @DisplayName("Should return null when deserializing null")
        void shouldReturnNullWhenDeserializingNull() {
            assertThat(converter.convertToEntityAttribute(null)).isNull();
        }

        @Test
        @DisplayName("Should return null when deserializing empty string")
        void shouldReturnNullWhenDeserializingEmptyString() {
            assertThat(converter.convertToEntityAttribute("")).isNull();
        }
    }
}
