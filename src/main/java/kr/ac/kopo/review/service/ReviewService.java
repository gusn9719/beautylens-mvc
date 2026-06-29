package kr.ac.kopo.review.service;

import java.util.List;

import kr.ac.kopo.review.vo.ReviewVO;

public interface ReviewService {
    List<ReviewVO> getReviews(int productId);
    List<ReviewVO> getReviewsBySentiment(int productId, String sentimentLabel);
}
