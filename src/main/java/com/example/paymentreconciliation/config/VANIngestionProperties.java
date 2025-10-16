package com.example.paymentreconciliation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "van")
public class VANIngestionProperties {
    private String baseDir;
    private String inboxDir;
    private String processingDir;
    private String archiveDir;
    private String quarantineDir;

    public String getBaseDir() { return baseDir; }
    public void setBaseDir(String baseDir) { this.baseDir = baseDir; }
    public String getInboxDir() { return inboxDir; }
    public void setInboxDir(String inboxDir) { this.inboxDir = inboxDir; }
    public String getProcessingDir() { return processingDir; }
    public void setProcessingDir(String processingDir) { this.processingDir = processingDir; }
    public String getArchiveDir() { return archiveDir; }
    public void setArchiveDir(String archiveDir) { this.archiveDir = archiveDir; }
    public String getQuarantineDir() { return quarantineDir; }
    public void setQuarantineDir(String quarantineDir) { this.quarantineDir = quarantineDir; }
}
