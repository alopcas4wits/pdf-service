package de.wits.pdf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.util.Assert;

import java.nio.charset.Charset;
import java.util.TimeZone;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"de.wits"})
@EnableResourceServer
@EnableOAuth2Sso
@EnableOAuth2Client
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableScheduling
public class Application {


    public static void main(String[] args) {

        // can be set runtime before Spring instantiates any beans
        // TimeZone.setDefault(TimeZone.getTimeZone("GMT+00:00"));
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        // cannot override encoding in Spring at runtime as some strings have already been read
        // however, we can assert and ensure right values are loaded here
        // verify system property is set
        //   Assert.isTrue("UTF-8".equals(System.getProperty("file.encoding")));
        // and actually verify it is being used
        Charset charset = Charset.defaultCharset();
        Assert.isTrue(charset.equals(Charset.forName("UTF-8")));


        SpringApplication.run(Application.class, args);
    }

    @Configuration
    @EnableWebSecurity
    protected static class webSecurityConfig extends ResourceServerConfigurerAdapter {

        @Override
        public void configure(HttpSecurity http) throws Exception {
            // @formatter:off
      http
              .authorizeRequests()
              // allow health check
              .antMatchers(HttpMethod.GET, "/health").permitAll()
              .antMatchers("/**")
              //.authenticated()
              .permitAll()
              .and()
              .csrf().disable();
      // @formatter:on
        }
    }
}
