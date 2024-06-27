package com.mikaelfrancoeur.springpropertymerging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

class MergingTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(DataSourceConfig.class)
            .withPropertyValues(
                    "spring.datasource.url=common-url",
                    "spring.datasource.username=common-username",
                    "spring.datasource.password=common-password",
                    "custom.datasources.writer.username=writer-username",
                    "custom.datasources.reader.password=reader-password");

    @Test
    void merging() {
        runner.run(context ->
                assertThat(context)
                        .getBean("writerDataSourceProperties", DataSourceProperties.class)
                        .extracting(
                                DataSourceProperties::getUrl,
                                DataSourceProperties::getUsername,
                                DataSourceProperties::getPassword)
                        .containsExactly(
                                "common-url",
                                "writer-username",
                                "common-password"));

        runner.run(context ->
                assertThat(context)
                        .getBean("readerDataSourceProperties", DataSourceProperties.class)
                        .extracting(
                                DataSourceProperties::getUrl,
                                DataSourceProperties::getUsername,
                                DataSourceProperties::getPassword)
                        .containsExactly(
                                "common-url",
                                "common-username",
                                "reader-password"));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ConfigurationProperties(prefix = "spring.datasource")
    // this class emulates the one from spring data, to keep this project simple
    static class DataSourceProperties {
        private String url;
        private String username;
        private String password;
    }

    @Configuration
    @EnableConfigurationProperties(DataSourceProperties.class)
    static class DataSourceConfig {

        @Bean(autowireCandidate = false)
        @Scope("prototype")
        DataSourceProperties commonDataSourceProperties() {
            return new DataSourceProperties();
        }

        @Bean
        @ConfigurationProperties(prefix = "custom.datasources.writer")
        DataSourceProperties writerDataSourceProperties() {
            return commonDataSourceProperties();
        }

        @Bean
        @ConfigurationProperties(prefix = "custom.datasources.reader")
        DataSourceProperties readerDataSourceProperties() {
            return commonDataSourceProperties();
        }
    }
}
