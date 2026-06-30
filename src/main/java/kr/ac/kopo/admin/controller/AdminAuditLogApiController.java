package kr.ac.kopo.admin.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import kr.ac.kopo.admin.service.AdminAuditLogService;
import kr.ac.kopo.admin.vo.AdminAuditLogVO;
import kr.ac.kopo.common.vo.ApiResponse;
import kr.ac.kopo.member.vo.MemberVO;

@RestController
@RequestMapping("/api/admin/logs")
public class AdminAuditLogApiController {

    @Autowired
    private AdminAuditLogService adminAuditLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminAuditLogVO>>> logs(
            @RequestParam(required = false, defaultValue = "all") String actionType,
            @RequestParam(required = false, defaultValue = "all") String targetType,
            @RequestParam(required = false) Integer adminId,
            @RequestParam(defaultValue = "100") int size,
            HttpSession session) {
        MemberVO admin = (MemberVO) session.getAttribute("loginMember");
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "not logged in", null));
        }
        if (!"ADMIN".equals(admin.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "admin only", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "admin logs found",
                adminAuditLogService.getLogs(actionType, targetType, adminId, size)));
    }
}
