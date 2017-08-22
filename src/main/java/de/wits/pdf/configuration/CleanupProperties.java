package de.wits.pdf.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cleanup")
public class CleanupProperties {

    private String cronexpression;

    public String getCronexpression() {
        return cronexpression;
    }

    public void setCronexpression(String cronexpression) {
        this.cronexpression = cronexpression;
    }
}
