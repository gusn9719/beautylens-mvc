package kr.ac.kopo.admin.controller;

import kr.ac.kopo.admin.service.AdminService;
import kr.ac.kopo.admin.service.AdminAuditLogService;
import kr.ac.kopo.admin.vo.AdminSummaryVO;
import kr.ac.kopo.comment.service.CommentService;
import kr.ac.kopo.comment.vo.CommentVO;
import kr.ac.kopo.common.vo.ApiResponse;
import kr.ac.kopo.member.vo.MemberVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private AdminAuditLogService adminAuditLogService;

    private ResponseEntity<ApiResponse<Void>> checkAdmin(HttpSession session) {
        MemberVO m = (MemberVO) session.getAttribute("loginMember");
        if (m == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "not logged in", null));
        }
        if (!"ADMIN".equals(m.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "admin only", null));
        }
        return null;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AdminSummaryVO>> summary(HttpSession session) {
        ResponseEntity<ApiResponse<Void>> err = checkAdmin(session);
        if (err != null) return ResponseEntity.status(err.getStatusCode())
                .body(new ApiResponse<>(false, err.getBody().getMessage(), null));

        AdminSummaryVO summary = adminService.getSummary();
        return ResponseEntity.ok(new ApiResponse<>(true, "summary ok", summary));
    }

    @GetMapping("/comments")
    public ResponseEntity<ApiResponse<List<CommentVO>>> comments(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "50") int size,
            HttpSession session) {

        ResponseEntity<ApiResponse<Void>> err = checkAdmin(session);
        if (err != null) return ResponseEntity.status(err.getStatusCode())
                .body(new ApiResponse<>(false, err.getBody().getMessage(), null));

        List<CommentVO> list = commentService.getAllComments(status, size);
        return ResponseEntity.ok(new ApiResponse<>(true, "admin comments found", list));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable int commentId,
            HttpSession session) {

        ResponseEntity<ApiResponse<Void>> err = checkAdmin(session);
        if (err != null) return err;

        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");

        CommentVO comment = commentService.getComment(commentId);
        if (comment == null || "DELETED".equals(comment.getStatus())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "comment not found", null));
        }

        commentService.deleteComment(commentId, loginMember.getMemberId(), "ADMIN_DELETE");
        adminAuditLogService.log(loginMember, "COMMENT_DELETE", "COMMENT", commentId,
                comment.getStatus(), "DELETED");
        return ResponseEntity.ok(new ApiResponse<>(true, "comment deleted", null));
    }
}
