package com.medicalstore.backup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "backup.enabled", havingValue = "true")
public class BackupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BackupScheduler.class);

    @Autowired
    private DatabaseBackupService backupService;

    @Scheduled(cron = "${backup.cron:0 0 2 * * *}")
    public void scheduledBackup() {
        logger.info("Starting scheduled daily backup...");
        String result = backupService.backup();
        if (result != null) {
            logger.info("Scheduled backup completed: {}", result);
        } else {
            logger.error("Scheduled backup FAILED");
        }
    }
}
