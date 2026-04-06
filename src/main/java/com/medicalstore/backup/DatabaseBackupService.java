package com.medicalstore.backup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "backup.enabled", havingValue = "true")
public class DatabaseBackupService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseBackupService.class);
    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    @Value("${backup.dir:backups}")
    private String backupDir;

    @Value("${backup.max-files:7}")
    private int maxBackupFiles;

    @Value("${backup.pg-dump-path:pg_dump}")
    private String pgDumpPath;

    @Value("${backup.pg-restore-path:psql}")
    private String pgRestorePath;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    public String backup() {
        try {
            Path dir = Paths.get(backupDir);
            Files.createDirectories(dir);

            String timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);
            String filename = "dhruvit_backup_" + timestamp + ".sql";
            Path backupFile = dir.resolve(filename);

            String dbName = extractDbName(dbUrl);
            String host = extractHost(dbUrl);
            String port = extractPort(dbUrl);

            ProcessBuilder pb = new ProcessBuilder(
                pgDumpPath, "-h", host, "-p", port, "-U", dbUsername, "-d", dbName, "-F", "p", "-f", backupFile.toString()
            );
            pb.environment().put("PGPASSWORD", dbPassword);
            pb.redirectErrorStream(true);

            logger.info("Starting database backup: {}", filename);
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                long sizeKB = Files.size(backupFile) / 1024;
                logger.info("Backup completed: {} ({}KB)", filename, sizeKB);
                cleanOldBackups();
                return backupFile.toString();
            } else {
                logger.error("Backup failed (exit code {}): {}", exitCode, output);
                Files.deleteIfExists(backupFile);
                return null;
            }
        } catch (Exception e) {
            logger.error("Backup error", e);
            return null;
        }
    }

    public boolean restore(String backupFilePath) {
        try {
            Path file = Paths.get(backupFilePath);
            if (!Files.exists(file)) {
                logger.error("Backup file not found: {}", backupFilePath);
                return false;
            }

            String dbName = extractDbName(dbUrl);
            String host = extractHost(dbUrl);
            String port = extractPort(dbUrl);

            ProcessBuilder pb = new ProcessBuilder(
                pgRestorePath, "-h", host, "-p", port, "-U", dbUsername, "-d", dbName, "-f", backupFilePath
            );
            pb.environment().put("PGPASSWORD", dbPassword);
            pb.redirectErrorStream(true);

            logger.info("Starting database restore from: {}", backupFilePath);
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                logger.info("Restore completed successfully");
                return true;
            } else {
                logger.error("Restore failed (exit code {}): {}", exitCode, output);
                return false;
            }
        } catch (Exception e) {
            logger.error("Restore error", e);
            return false;
        }
    }

    public List<BackupInfo> listBackups() {
        try {
            Path dir = Paths.get(backupDir);
            if (!Files.exists(dir)) return List.of();

            File[] files = dir.toFile().listFiles((d, name) -> name.startsWith("dhruvit_backup_") && name.endsWith(".sql"));
            if (files == null) return List.of();

            return Arrays.stream(files)
                .sorted(Comparator.comparingLong(File::lastModified).reversed())
                .map(f -> new BackupInfo(
                    f.getName(), f.getAbsolutePath(), f.length() / 1024,
                    LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(f.lastModified()), java.time.ZoneId.systemDefault())
                ))
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error listing backups", e);
            return List.of();
        }
    }

    private void cleanOldBackups() {
        try {
            List<BackupInfo> backups = listBackups();
            if (backups.size() <= maxBackupFiles) return;
            for (BackupInfo b : backups.subList(maxBackupFiles, backups.size())) {
                Files.deleteIfExists(Paths.get(b.filePath()));
                logger.info("Deleted old backup: {}", b.fileName());
            }
        } catch (IOException e) {
            logger.warn("Error cleaning old backups", e);
        }
    }

    private String extractDbName(String url) { return url.substring(url.lastIndexOf('/') + 1).split("\\?")[0]; }
    private String extractHost(String url) { return url.substring(url.indexOf("//") + 2).split(":")[0]; }
    private String extractPort(String url) { return url.substring(url.indexOf("//") + 2).split(":")[1].split("/")[0]; }

    public record BackupInfo(String fileName, String filePath, long sizeKB, LocalDateTime createdAt) {}
}
