package de.wits.pdf.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({FileSystemPathConfig.class})
public class PdfGenerationConfiguration {
}
