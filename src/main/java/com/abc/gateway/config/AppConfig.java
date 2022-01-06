package com.abc.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppConfig {

    @NotNull
    private boolean gckPhaseActive;
    @NotNull
    private boolean detPhaseActive;
    @NotNull
    private boolean forwardingMode;
    private String forwardingURL;

    @Getter
    @Setter
    public static class EntityModel {
        @NotNull
        private String model;

        @NotNull
        private boolean adjustmentApplicable;

        @NotNull
        private boolean requireTrigger;

        @NotNull
        private String trueModel;

        @NotNull
        private String dbOperation;

        @NotNull
        private String dbOperationForAdjustment;
    }

    @Getter
    @Setter
    public static class Kafka {
        @NotNull
        private KafkaClusterConfigs kafkaClusterConfigs;
        @NotNull
        private OutProcessFlowTopicConfigs outProcessFlowTopicConfigs;
        @NotNull
        private SchemaRegistry schemaRegistry;
        @NotNull
        private int secondsBetweenClusterChecks;
    }

    @Getter
    @Setter
    public static class KafkaClusterConfigs {
        @NotNull
        private String bootstrapServers;
        @NotNull
        private String kafkaSchemaRegistryUrlKey;
        @NotNull
        private String kafkaSchemaRegistryKeyConverterUrlKey;
        @NotNull
        private String kafkaSchemaRegistryValueConverterUrlKey;
    }

    @Getter
    @Setter
    public static class OutProcessFlowTopicConfigs {
        @NotNull
        private String processFlowTopicToProduce;
        @NotNull
        private int numPartitions;
        @NotNull
        private short replicationFactor;
        @NotNull
        private String kafkaKeySerializer;
        @NotNull
        private String kafkaValueSerializer;
        @NotNull
        private int producerRetries;
        @NotNull
        private String acks;
        @NotNull
        private int batchSize;
    }

    @Getter
    @Setter
    public static class SchemaRegistry {
        @NotNull
        private String servers;
    }
}
