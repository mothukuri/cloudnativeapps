package com.aacloud.servicebrokermongo.config;


import org.springframework.cloud.servicebroker.model.catalog.Catalog;
import org.springframework.cloud.servicebroker.model.catalog.Plan;
import org.springframework.cloud.servicebroker.model.catalog.ServiceDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
public class CatalogConfig {

    @Bean
    public Catalog catalog() {
        return Catalog.builder().serviceDefinitions(Collections.singletonList(
                ServiceDefinition.builder().id(getEnvOrDefault("SERVICE_ID", "mongodb-service-broker")).
                        name(getEnvOrDefault("SERVICE_NAME", "MongoDB")).
                        description("A simple MongoDB service broker implementation").
                        bindable(true).
                        planUpdateable(false).
                        plans(Collections.singletonList(
                            Plan.builder().id(getEnvOrDefault("PLAN_ID", "mongo-plan")).
                                    name("standard").
                                    description("This is a default mongo plan.  All services are created equally.").
                                    free(true).
                                    metadata(getPlanMetadata()).build())).
                        tags(Arrays.asList("mongodb", "document")).
                        metadata(getServiceDefinitionMetadata()).
                        bindable(true).
                        planUpdateable(true).build())).build();
    }

    /* Used by Pivotal CF console */

    private Map<String, Object> getServiceDefinitionMetadata() {
        Map<String, Object> sdMetadata = new HashMap<>();
        sdMetadata.put("displayName", "MongoDB");
        sdMetadata.put("imageUrl", "http://info.mongodb.com/rs/mongodb/images/MongoDB_Logo_Full.png");
        sdMetadata.put("longDescription", "MongoDB Service");
        sdMetadata.put("providerDisplayName", "Pivotal");
        sdMetadata.put("documentationUrl", "https://github.com/spring-cloud-samples/cloudfoundry-mongodb-service-broker");
        sdMetadata.put("supportUrl", "https://github.com/spring-cloud-samples/cloudfoundry-mongodb-service-broker");
        return sdMetadata;
    }

    private Map<String, Object> getPlanMetadata() {
        Map<String, Object> planMetadata = new HashMap<>();
        planMetadata.put("bullets", getBullets());
        return planMetadata;
    }


    private List<String> getBullets() {
        return Arrays.asList("Shared MongoDB server",
                "100 MB Storage (not enforced)",
                "40 concurrent connections (not enforced)");
    }

    private String getEnvOrDefault(final String variable, final String defaultValue) {
        String value = System.getenv(variable);
        if (value != null) {
            return value;
        } else {
            return defaultValue;
        }
    }

}
