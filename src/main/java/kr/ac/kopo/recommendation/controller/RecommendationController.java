package kr.ac.kopo.recommendation.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import kr.ac.kopo.common.vo.ApiResponse;
import kr.ac.kopo.member.vo.MemberVO;
import kr.ac.kopo.recommendation.service.RecommendationService;
import kr.ac.kopo.recommendation.vo.RecommendationVO;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RecommendationVO>>> recommendBySkinType(
            @RequestParam String skinType,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session) {

        if (skinType == null || skinType.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "skin type is required", null));
        }

        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
        String skinConcern = loginMember != null ? loginMember.getSkinConcern() : null;
        Integer memberId = loginMember != null ? loginMember.getMemberId() : null;
        List<RecommendationVO> list = recommendationService.recommend(skinType, skinConcern, size, memberId);

        return ResponseEntity.ok(new ApiResponse<>(true, "recommendations found", list));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<RecommendationVO>>> recommend(
            @RequestParam(defaultValue = "20") int size,
            HttpSession session) {

        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "not logged in", null));
        }

        String skinType = loginMember.getSkinType();
        if (skinType == null || skinType.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "skin type is required", null));
        }

        List<RecommendationVO> list = recommendationService.recommend(
                skinType, loginMember.getSkinConcern(), size, loginMember.getMemberId());

        return ResponseEntity.ok(new ApiResponse<>(true, "recommendations found", list));
    }
}
