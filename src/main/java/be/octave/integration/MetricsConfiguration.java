package be.octave.integration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegrationManagement;
import org.springframework.integration.graph.IntegrationGraphServer;
import org.springframework.integration.http.config.EnableIntegrationGraphController;

@Configuration
@EnableIntegrationManagement (defaultCountsEnabled = "true")
@EnableIntegrationGraphController()
public class MetricsConfiguration {

    @Bean
    public IntegrationGraphServer integrationGraphServer() {
        return new IntegrationGraphServer();
    }

}
