package com.ercot.cp.ews;

import com.ercot.cp.ews.config.ReportConfig;
import com.ercot.cp.ews.config.SystemConfiguration;
import com.ercot.cp.ews.job.DownloadReportJob;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.security.support.KeyStoreFactoryBean;
import org.springframework.ws.soap.security.xwss.XwsSecurityInterceptor;
import org.springframework.ws.soap.security.xwss.callback.KeyStoreCallbackHandler;

import static org.quartz.TriggerKey.triggerKey;
import javax.annotation.PostConstruct;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.ZoneId;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

@Configuration
@RequiredArgsConstructor
public class EwsConfiguration {

    private final SystemConfiguration systemConfiguration;
    private final Scheduler scheduler;

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.ercot.schema._2007_06.nodal.ews.message");
        return marshaller;
    }

    private void configureSystemProperties() {
        System.setProperty("javax.net.ssl.trustStoreType", systemConfiguration.getTrustStoreType());
        System.setProperty("javax.net.ssl.trustStore", systemConfiguration.getTrustStore());
        System.setProperty("javax.net.ssl.trustStorePassword", systemConfiguration.getTrustStorePassword());

        System.setProperty("javax.net.ssl.keyStoreType", systemConfiguration.getKeyStoreType());
        System.setProperty("javax.net.ssl.keyStore", systemConfiguration.getKeyStore());
        System.setProperty("javax.net.ssl.keyStorePassword", systemConfiguration.getKeyStorePassword());
        
        System.out.println("getPrivateKeyPassword:"+systemConfiguration.getPrivateKeyPassword());
        System.out.println("getKeyStorePassword:"+systemConfiguration.getKeyStorePassword());
    }

    private void verifyKeyStore(KeyStore ks) throws KeyStoreException {
        Enumeration<String> aliases = ks.aliases();
        System.out.println("Keystore contents:");
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            System.out.println("Found alias: " + alias);
            if (ks.isKeyEntry(alias)) {
                System.out.println("  This is a key entry");
            }
        }
    }

    @Bean
    public KeyStoreCallbackHandler keyStoreHandler() {
        configureSystemProperties();
        try {

            KeyStore ks = KeyStore.getInstance(systemConfiguration.getKeyStoreType());
            ks.load(new FileInputStream(systemConfiguration.getKeyStore()),
                    systemConfiguration.getKeyStorePassword().toCharArray());
            verifyKeyStore(ks);
            String policyFile = systemConfiguration.getSecurityPolicyFileName();
            System.out.println("Security policy file: " + policyFile);
            try {
                InputStream is = new ClassPathResource(policyFile).getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                reader.close();
            } catch (IOException e) {
                System.err.println("Error reading security policy file: " + e.getMessage());
            }
            KeyStoreCallbackHandler keyStoreHandler = new KeyStoreCallbackHandler();
            keyStoreHandler.setKeyStore(ks);
            keyStoreHandler.setPrivateKeyPassword(systemConfiguration.getPrivateKeyPassword());
            keyStoreHandler.setDefaultAlias("clientcert");
            return keyStoreHandler;
        } catch (Exception e) {
            System.err.println(e);

            throw new RuntimeException("Failed to create KeyStoreCallbackHandler", e);
        }
    }

    @Bean
    public XwsSecurityInterceptor securityInterceptor() {
        System.out.println("Creating XwsSecurityInterceptor");
        XwsSecurityInterceptor securityInterceptor = new XwsSecurityInterceptor();
        KeyStoreCallbackHandler handler = keyStoreHandler();
        System.out.println("KeyStoreCallbackHandler created successfully");
        securityInterceptor.setCallbackHandler(handler);
        securityInterceptor
                .setPolicyConfiguration(new ClassPathResource(systemConfiguration.getSecurityPolicyFileName()));
        System.out.println("XwsSecurityInterceptor configured successfully");
        return securityInterceptor;
    }

    @Bean
    public EwsClient ewsClient(Jaxb2Marshaller marshaller) {
        EwsClient client = new EwsClient();
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);
        client.setInterceptors(new ClientInterceptor[] { securityInterceptor() });
        return client;
    }

    @PostConstruct
    void scheduleJob() {
        List<ReportConfig> reportConfigList = systemConfiguration.getReports();
        reportConfigList
                .forEach(reportConfig -> {
                    Class<? extends Job> jobClass = DownloadReportJob.class;

                    final var jobId = reportConfig.getId();
                    final var cronSchedule = reportConfig.getCron();

                    JobDataMap jobDataMap = new JobDataMap();
                    jobDataMap.put("reportConfig", reportConfig);

                    final var jobDetail = JobBuilder.newJob(jobClass)
                            .withIdentity(JobKey.jobKey(reportConfig.getName(), jobId))
                            .setJobData(jobDataMap)
                            .storeDurably()
                            .build();

                    CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                            .withIdentity(triggerKey(jobClass.getSimpleName(), jobId))
                            .withSchedule(CronScheduleBuilder.cronSchedule(cronSchedule)
                                    .inTimeZone(TimeZone.getTimeZone("CST")))
                            .build();

                    try {
                        scheduler.scheduleJob(jobDetail, Set.of(cronTrigger), true);
                    } catch (SchedulerException e) {
                        throw new RuntimeException(e);
                    }

                });
    }

}
