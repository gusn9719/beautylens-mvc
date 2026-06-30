package kr.ac.kopo.admin.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import kr.ac.kopo.comment.service.CommentReportService;
import kr.ac.kopo.comment.service.CommentService;
import kr.ac.kopo.admin.service.AdminAuditLogService;
import kr.ac.kopo.comment.vo.CommentReportVO;
import kr.ac.kopo.common.vo.ApiResponse;
import kr.ac.kopo.member.vo.MemberVO;

@RestController
@RequestMapping("/api/admin")
public class AdminCommentReportApiController {

    @Autowired
    private CommentReportService commentReportService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private AdminAuditLogService adminAuditLogService;

    @GetMapping("/comment-reports")
    public ResponseEntity<ApiResponse<List<CommentReportVO>>> reports(
            @RequestParam(required = false, defaultValue = "all") String status,
            @RequestParam(defaultValue = "100") int size,
            HttpSession session) {
        MemberVO admin = admin(session);
        if (admin == null) return adminError(session);
        return ResponseEntity.ok(new ApiResponse<>(true, "comment reports found",
                commentReportService.getReports(status, size)));
    }

    @PostMapping("/comment-reports/{reportId}/resolve")
    public ResponseEntity<ApiResponse<Void>> resolve(
            @PathVariable int reportId,
            @RequestBody(required = false) Map<String, String> body,
            HttpSession session) {
        MemberVO admin = admin(session);
        if (admin == null) return adminError(session);
        String status = body == null ? "RESOLVED" : body.get("status");
        commentReportService.resolve(admin, reportId, status);
        adminAuditLogService.log(admin, "COMMENT_REPORT_RESOLVE", "COMMENT_REPORT", reportId, null, status);
        return ResponseEntity.ok(new ApiResponse<>(true, "report resolved", null));
    }

    @PostMapping("/comments/{commentId}/restore")
    public ResponseEntity<ApiResponse<Void>> restore(@PathVariable int commentId, HttpSession session) {
        MemberVO admin = admin(session);
        if (admin == null) return adminError(session);
        commentService.restoreComment(commentId, admin.getMemberId());
        adminAuditLogService.log(admin, "COMMENT_RESTORE", "COMMENT", commentId, "DELETED", "ACTIVE");
        return ResponseEntity.ok(new ApiResponse<>(true, "comment restored", null));
    }

    private MemberVO admin(HttpSession session) {
        MemberVO member = (MemberVO) session.getAttribute("loginMember");
        return member != null && "ADMIN".equals(member.getRole()) ? member : null;
    }

    private <T> ResponseEntity<ApiResponse<T>> adminError(HttpSession session) {
        MemberVO member = (MemberVO) session.getAttribute("loginMember");
        HttpStatus status = member == null ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
        String message = member == null ? "not logged in" : "admin only";
        return ResponseEntity.status(status).body(new ApiResponse<>(false, message, null));
    }
}
