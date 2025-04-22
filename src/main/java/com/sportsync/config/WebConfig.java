package com.sportsync.config;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {
    @Bean
    public GeometryFactory geometryFactory() {
        // SRID 4326 is for WGS84 (standard GPS coordinates)
        return new GeometryFactory(new PrecisionModel(), 4326);
    }
}
