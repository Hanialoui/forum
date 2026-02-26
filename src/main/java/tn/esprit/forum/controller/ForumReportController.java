package tn.esprit.forum.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.forum.entity.ForumReport;
import tn.esprit.forum.services.ForumReportService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forums")
public class ForumReportController {

    @Autowired
    private ForumReportService reportService;

    // ── CREATE REPORT ──
    @PostMapping("/create-report")
    public ResponseEntity<ForumReport> createReport(@RequestBody ForumReport report) {
        return ResponseEntity.ok(reportService.createReport(report));
    }

    // ── GET ALL REPORTS ──
    @GetMapping("/get-all-reports")
    public ResponseEntity<List<ForumReport>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    // ── GET REPORT BY ID ──
    @GetMapping("/get-report-by-id/{id}")
    public ResponseEntity<ForumReport> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    // ── GET REPORTS BY POST ──
    @GetMapping("/get-reports-by-post/{postId}")
    public ResponseEntity<List<ForumReport>> getReportsByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(reportService.getReportsByPostId(postId));
    }

    // ── GET REPORTS BY STATUS ──
    @GetMapping("/get-reports-by-status/{status}")
    public ResponseEntity<List<ForumReport>> getReportsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(reportService.getReportsByStatus(status));
    }

    // ── UPDATE REPORT STATUS ──
    @PutMapping("/update-report-status/{id}")
    public ResponseEntity<ForumReport> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String status = body.get("status");
        String adminNote = body.get("adminNote");
        return ResponseEntity.ok(reportService.updateReportStatus(id, status, adminNote));
    }

    // ── DELETE REPORT ──
    @DeleteMapping("/delete-report/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }

    // ── DELETE REPORTED POST ──
    @DeleteMapping("/delete-reported-post/{postId}")
    public ResponseEntity<Void> deleteReportedPost(@PathVariable Long postId) {
        reportService.deleteReportedPost(postId);
        return ResponseEntity.noContent().build();
    }
}
