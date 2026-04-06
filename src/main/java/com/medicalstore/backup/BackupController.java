package com.medicalstore.backup;

import com.medicalstore.util.Constants;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/backup")
@ConditionalOnProperty(name = "backup.enabled", havingValue = "true")
public class BackupController {

    @Autowired
    private DatabaseBackupService backupService;

    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> runBackup(HttpSession session) {
        if (session.getAttribute(Constants.USER_SESSION_KEY) == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        String result = backupService.backup();
        if (result != null) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Backup completed", "file", result));
        }
        return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Backup failed. Check if pg_dump is installed."));
    }

    @GetMapping("/list")
    public ResponseEntity<List<DatabaseBackupService.BackupInfo>> listBackups(HttpSession session) {
        if (session.getAttribute(Constants.USER_SESSION_KEY) == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(backupService.listBackups());
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadBackup(@PathVariable String fileName, HttpSession session) {
        if (session.getAttribute(Constants.USER_SESSION_KEY) == null) {
            return ResponseEntity.status(401).build();
        }
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }
        var backup = backupService.listBackups().stream().filter(b -> b.fileName().equals(fileName)).findFirst();
        if (backup.isEmpty()) return ResponseEntity.notFound().build();

        File file = new File(backup.get().filePath());
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
            .body(new FileSystemResource(file));
    }

    @PostMapping("/restore/{fileName}")
    public ResponseEntity<Map<String, Object>> restoreBackup(@PathVariable String fileName, HttpSession session) {
        if (session.getAttribute(Constants.USER_SESSION_KEY) == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid file name"));
        }
        var backup = backupService.listBackups().stream().filter(b -> b.fileName().equals(fileName)).findFirst();
        if (backup.isEmpty()) return ResponseEntity.notFound().build();

        boolean success = backupService.restore(backup.get().filePath());
        if (success) return ResponseEntity.ok(Map.of("success", true, "message", "Restore completed"));
        return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Restore failed. Check logs."));
    }
}
