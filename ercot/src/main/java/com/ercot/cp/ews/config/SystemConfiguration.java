package com.ercot.cp.ews.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@Configuration
@ConfigurationProperties(prefix = "config")
public class SystemConfiguration {

    private String folderPath;

    private String trustStoreType;
    private String trustStorePassword;
    private String trustStore;

    private String keyStoreType;
    private String keyStore;
    private String keyStorePassword;

    private String privateKeyPassword;

    private String certificateFileName;
    private String securityPolicyFileName;

    private String userID;

    private List<ReportConfig> reports;
}
