package kr.ac.kopo.admin.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import kr.ac.kopo.admin.service.AdminProductService;
import kr.ac.kopo.admin.vo.ProductAdminFlagVO;
import kr.ac.kopo.common.vo.ApiResponse;
import kr.ac.kopo.member.vo.MemberVO;
import kr.ac.kopo.product.vo.ProductVO;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductApiController {

    @Autowired
    private AdminProductService adminProductService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductVO>>> products(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "all") String platform,
            @RequestParam(required = false, defaultValue = "all") String visible,
            @RequestParam(required = false, defaultValue = "all") String excludeRecommendation,
            @RequestParam(required = false, defaultValue = "all") String qualityStatus,
            @RequestParam(required = false, defaultValue = "false") boolean imageMissing,
            @RequestParam(defaultValue = "100") int size,
            HttpSession session) {
        MemberVO admin = admin(session);
        if (admin == null) return adminError(session);

        Map<String, Object> param = new HashMap<>();
        param.put("keyword", keyword);
        param.put("platform", platform);
        param.put("visible", visible);
        param.put("excludeRecommendation", excludeRecommendation);
        param.put("qualityStatus", qualityStatus);
        param.put("imageMissing", imageMissing);
        param.put("size", size);
        return ResponseEntity.ok(new ApiResponse<>(true, "admin products found",
                adminProductService.getAdminProducts(param)));
    }

    @GetMapping("/{productId}/flags")
    public ResponseEntity<ApiResponse<ProductAdminFlagVO>> flags(@PathVariable int productId, HttpSession session) {
        MemberVO admin = admin(session);
        if (admin == null) return adminError(session);
        return ResponseEntity.ok(new ApiResponse<>(true, "flags found", adminProductService.getFlags(productId)));
    }

    @PutMapping("/{productId}/flags")
    public ResponseEntity<ApiResponse<ProductAdminFlagVO>> saveFlags(
            @PathVariable int productId,
            @RequestBody ProductAdminFlagVO flags,
            HttpSession session) {
        MemberVO admin = admin(session);
        if (admin == null) return adminError(session);
        flags.setProductId(productId);
        adminProductService.saveFlags(admin, flags);
        return ResponseEntity.ok(new ApiResponse<>(true, "flags saved", adminProductService.getFlags(productId)));
    }

    @PostMapping("/{productId}/hide")
    public ResponseEntity<ApiResponse<Map<String, Object>>> hide(
            @PathVariable int productId, @RequestBody(required = false) Map<String, String> body, HttpSession session) {
        MemberVO admin = admin(session);
        if (admin == null) return adminError(session);
        adminProductService.hide(admin, productId, body == null ? null : body.get("reason"));
        return okState("hidden", true);
    }

    @PostMapping("/{productId}/restore")
    public ResponseEntity<ApiResponse<Map<String, Object>>> restore(@PathVariable int productId, HttpSession session) {
        MemberVO admin = admin(session);
        if (admin == null) return adminError(session);
        adminProductService.restore(admin, productId);
        return okState("visible", true);
    }

    @PostMapping("/{productId}/exclude-recommendation")
    public ResponseEntity<ApiResponse<Map<String, Object>>> exclude(@PathVariable int productId, HttpSession session) {
        MemberVO admin = admin(session);
        if (admin == null) return adminError(session);
        adminProductService.excludeRecommendation(admin, productId);
        return okState("excludeRecommendation", true);
    }

    @PostMapping("/{productId}/include-recommendation")
    public ResponseEntity<ApiResponse<Map<String, Object>>> include(@PathVariable int productId, HttpSession session) {
        MemberVO admin = admin(session);
        if (admin == null) return adminError(session);
        adminProductService.includeRecommendation(admin, productId);
        return okState("excludeRecommendation", false);
    }

    @PostMapping("/{productId}/feature")
    public ResponseEntity<ApiResponse<Map<String, Object>>> feature(@PathVariable int productId, HttpSession session) {
        MemberVO admin = admin(session);
        if (admin == null) return adminError(session);
        adminProductService.feature(admin, productId);
        return okState("featured", true);
    }

    @PostMapping("/{productId}/unfeature")
    public ResponseEntity<ApiResponse<Map<String, Object>>> unfeature(@PathVariable int productId, HttpSession session) {
        MemberVO admin = admin(session);
        if (admin == null) return adminError(session);
        adminProductService.unfeature(admin, productId);
        return okState("featured", false);
    }

    private ResponseEntity<ApiResponse<Map<String, Object>>> okState(String key, boolean value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "state changed", Map.of(key, value)));
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
