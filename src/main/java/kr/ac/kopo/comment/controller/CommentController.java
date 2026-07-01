package kr.ac.kopo.comment.controller;

import kr.ac.kopo.comment.service.CommentService;
import kr.ac.kopo.comment.service.CommentReportService;
import kr.ac.kopo.comment.vo.CommentVO;
import kr.ac.kopo.common.vo.ApiResponse;
import kr.ac.kopo.member.vo.MemberVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@RestController
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentReportService commentReportService;

    @GetMapping("/api/products/{productId}/comments")
    public ResponseEntity<ApiResponse<List<CommentVO>>> list(@PathVariable int productId) {
        List<CommentVO> list = commentService.getComments(productId);
        return ResponseEntity.ok(new ApiResponse<>(true, "comments found", list));
    }

    @PostMapping("/api/products/{productId}/comments")
    public ResponseEntity<ApiResponse<Void>> post(
            @PathVariable int productId,
            @RequestBody Map<String, String> body,
            HttpSession session) {

        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "not logged in", null));
        }

        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "content is required", null));
        }
        if (content.length() > 1000) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "content must be 1000 characters or less", null));
        }

        if (!commentService.productExists(productId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "product not found", null));
        }

        commentService.postComment(productId, loginMember.getMemberId(), content.trim());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "comment posted", null));
    }

    @PutMapping("/api/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable int commentId,
            @RequestBody Map<String, String> body,
            HttpSession session) {

        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "not logged in", null));
        }

        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "content is required", null));
        }
        if (content.length() > 1000) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "content must be 1000 characters or less", null));
        }

        CommentVO comment = commentService.getComment(commentId);
        if (comment == null || "DELETED".equals(comment.getStatus())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "comment not found", null));
        }
        if (!loginMember.getMemberId().equals(comment.getMemberId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "no permission to edit this comment", null));
        }

        commentService.updateComment(commentId, loginMember.getMemberId(), content.trim());
        return ResponseEntity.ok(new ApiResponse<>(true, "comment updated", null));
    }

    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable int commentId,
            HttpSession session) {

        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "not logged in", null));
        }

        CommentVO comment = commentService.getComment(commentId);
        if (comment == null || "DELETED".equals(comment.getStatus())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "comment not found", null));
        }

        boolean isAuthor = loginMember.getMemberId().equals(comment.getMemberId());
        boolean isAdmin  = "ADMIN".equals(loginMember.getRole());

        if (!isAuthor && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "no permission to delete this comment", null));
        }

        String reason = isAdmin ? "ADMIN_DELETE" : "USER_DELETE";
        commentService.deleteComment(commentId, loginMember.getMemberId(), reason);
        return ResponseEntity.ok(new ApiResponse<>(true, "comment deleted", null));
    }

    @PostMapping("/api/comments/{commentId}/report")
    public ResponseEntity<ApiResponse<Void>> report(
            @PathVariable int commentId,
            @RequestBody Map<String, String> body,
            HttpSession session) {

        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "not logged in", null));
        }

        CommentVO comment = commentService.getComment(commentId);
        if (comment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "comment not found", null));
        }

        commentReportService.report(loginMember, commentId, body.get("reasonType"), body.get("reasonText"));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "comment reported", null));
    }
}
