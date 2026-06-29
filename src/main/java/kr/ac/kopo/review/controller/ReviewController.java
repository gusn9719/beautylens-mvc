package kr.ac.kopo.review.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.kopo.common.vo.ApiResponse;
import kr.ac.kopo.review.service.ReviewService;
import kr.ac.kopo.review.vo.ReviewVO;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewVO>>> list(@PathVariable int productId) {
        List<ReviewVO> list = reviewService.getReviews(productId);
        return ResponseEntity.ok(new ApiResponse<>(true, "reviews found", list));
    }

    @GetMapping("/negative")
    public ResponseEntity<ApiResponse<List<ReviewVO>>> negative(@PathVariable int productId) {
        List<ReviewVO> list = reviewService.getReviewsBySentiment(productId, "negative");
        return ResponseEntity.ok(new ApiResponse<>(true, "negative reviews found", list));
    }

    @GetMapping("/positive")
    public ResponseEntity<ApiResponse<List<ReviewVO>>> positive(@PathVariable int productId) {
        List<ReviewVO> list = reviewService.getReviewsBySentiment(productId, "positive");
        return ResponseEntity.ok(new ApiResponse<>(true, "positive reviews found", list));
    }
}
