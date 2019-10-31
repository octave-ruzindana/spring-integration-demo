package be.octave.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.config.IntegrationManagementConfigurer;
import org.springframework.integration.support.management.MessageChannelMetrics;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("metrics")
public class MetricsController {

    @Autowired
    IntegrationManagementConfigurer configurer;

    @GetMapping
    String[] channels () {
        return configurer.getChannelNames();
    }

    @GetMapping("/counters")
    Map<String, Integer> counters () {
        Map<String, Integer> channelMetrics = new HashMap<>();

        for (String channelName : configurer.getChannelNames()) {
            channelMetrics.computeIfAbsent(channelName, name -> configurer.getChannelMetrics(name).getSendCount() ) ;

        }
        return channelMetrics;
    }


}
