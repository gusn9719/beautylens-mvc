package kr.ac.kopo.interaction.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import kr.ac.kopo.common.vo.ApiResponse;
import kr.ac.kopo.interaction.service.ProductInteractionService;
import kr.ac.kopo.interaction.vo.ProductRatingVO;
import kr.ac.kopo.interaction.vo.RecommendationFeedbackVO;
import kr.ac.kopo.interaction.vo.UserProductEventVO;
import kr.ac.kopo.member.vo.MemberVO;
import kr.ac.kopo.product.vo.ProductVO;

@RestController
public class ProductInteractionController {

    @Autowired
    private ProductInteractionService productInteractionService;

    @PostMapping("/api/products/{productId}/favorite")
    public ResponseEntity<ApiResponse<Map<String, Object>>> favorite(@PathVariable int productId, HttpSession session) {
        MemberVO member = requireLogin(session);
        if (member == null) return unauthorized();
        if (!productInteractionService.productExists(productId)) return notFound("product not found");

        productInteractionService.favorite(member, productId);
        return ResponseEntity.ok(new ApiResponse<>(true, "favorite saved", Map.of("favorite", true)));
    }

    @DeleteMapping("/api/products/{productId}/favorite")
    public ResponseEntity<ApiResponse<Map<String, Object>>> unfavorite(@PathVariable int productId, HttpSession session) {
        MemberVO member = requireLogin(session);
        if (member == null) return unauthorized();
        if (!productInteractionService.productExists(productId)) return notFound("product not found");

        productInteractionService.unfavorite(member, productId);
        return ResponseEntity.ok(new ApiResponse<>(true, "favorite removed", Map.of("favorite", false)));
    }

    @GetMapping("/api/members/me/favorites")
    public ResponseEntity<ApiResponse<List<ProductVO>>> myFavorites(HttpSession session) {
        MemberVO member = requireLogin(session);
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "not logged in", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "favorites found",
                productInteractionService.getFavorites(member.getMemberId())));
    }

    @PostMapping("/api/products/{productId}/rating")
    public ResponseEntity<ApiResponse<ProductRatingVO>> saveRating(
            @PathVariable int productId,
            @RequestBody ProductRatingVO rating,
            HttpSession session) {
        MemberVO member = requireLogin(session);
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "not logged in", null));
        }
        if (!productInteractionService.productExists(productId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "product not found", null));
        }
        if (rating.getRating() == null || rating.getRating() < 1 || rating.getRating() > 5) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "rating must be between 1 and 5", null));
        }
        rating.setProductId(productId);
        rating.setIrritationYn(normalizeYn(rating.getIrritationYn()));
        rating.setRepurchaseYn(normalizeYn(rating.getRepurchaseYn()));
        productInteractionService.saveRating(member, rating);
        return ResponseEntity.ok(new ApiResponse<>(true, "rating saved",
                productInteractionService.getRating(member.getMemberId(), productId)));
    }

    @GetMapping("/api/products/{productId}/rating")
    public ResponseEntity<ApiResponse<ProductRatingVO>> myRating(@PathVariable int productId, HttpSession session) {
        MemberVO member = requireLogin(session);
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "not logged in", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "rating found",
                productInteractionService.getRating(member.getMemberId(), productId)));
    }

    @DeleteMapping("/api/products/{productId}/rating")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteRating(@PathVariable int productId, HttpSession session) {
        MemberVO member = requireLogin(session);
        if (member == null) return unauthorized();
        if (!productInteractionService.productExists(productId)) return notFound("product not found");

        productInteractionService.deleteRating(member, productId);
        return ResponseEntity.ok(new ApiResponse<>(true, "rating removed", Map.of("removed", true)));
    }

    @GetMapping("/api/members/me/ratings")
    public ResponseEntity<ApiResponse<List<ProductRatingVO>>> myRatings(HttpSession session) {
        MemberVO member = requireLogin(session);
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "not logged in", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "ratings found",
                productInteractionService.getRatings(member.getMemberId())));
    }

    @PostMapping("/api/products/{productId}/recommendation-feedback")
    public ResponseEntity<ApiResponse<Map<String, Object>>> recommendationFeedback(
            @PathVariable int productId,
            @RequestBody Map<String, String> body,
            HttpSession session) {
        MemberVO member = requireLogin(session);
        if (member == null) return unauthorized();
        if (!productInteractionService.productExists(productId)) return notFound("product not found");
        try {
            productInteractionService.saveRecommendationFeedback(member, productId, body.get("feedbackType"));
            return ResponseEntity.ok(new ApiResponse<>(true, "feedback saved",
                    Map.of("feedbackType", body.get("feedbackType"))));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/api/members/me/recommendation-feedback")
    public ResponseEntity<ApiResponse<List<RecommendationFeedbackVO>>> myRecommendationFeedback(HttpSession session) {
        MemberVO member = requireLogin(session);
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "not logged in", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "recommendation feedback found",
                productInteractionService.getRecommendationFeedback(member.getMemberId())));
    }

    @DeleteMapping("/api/members/me/recommendation-feedback/{productId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteRecommendationFeedback(
            @PathVariable int productId,
            HttpSession session) {
        MemberVO member = requireLogin(session);
        if (member == null) return unauthorized();
        productInteractionService.deleteRecommendationFeedback(member, productId);
        return ResponseEntity.ok(new ApiResponse<>(true, "recommendation feedback removed",
                Map.of("removed", true)));
    }

    @PostMapping("/api/products/{productId}/events")
    public ResponseEntity<ApiResponse<Map<String, Object>>> recordEvent(
            @PathVariable int productId,
            @RequestBody Map<String, String> body,
            HttpSession session) {
        MemberVO member = requireLogin(session);
        if (member == null) return unauthorized();
        if (!productInteractionService.productExists(productId)) return notFound("product not found");

        productInteractionService.recordEvent(member, productId, body.get("eventType"), body.get("eventValue"));
        return ResponseEntity.ok(new ApiResponse<>(true, "event recorded", Map.of("recorded", true)));
    }

    @GetMapping("/api/members/me/recent-products")
    public ResponseEntity<ApiResponse<List<UserProductEventVO>>> recentProducts(HttpSession session) {
        MemberVO member = requireLogin(session);
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "not logged in", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "recent products found",
                productInteractionService.getRecentProducts(member.getMemberId())));
    }

    private MemberVO requireLogin(HttpSession session) {
        return (MemberVO) session.getAttribute("loginMember");
    }

    private <T> ResponseEntity<ApiResponse<T>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(false, "not logged in", null));
    }

    private <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, message, null));
    }

    private String normalizeYn(String value) {
        if (value == null || value.isBlank()) return null;
        return "Y".equalsIgnoreCase(value) ? "Y" : "N";
    }
}
