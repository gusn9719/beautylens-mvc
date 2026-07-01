package kr.ac.kopo.interaction.service;

import java.util.List;

import kr.ac.kopo.interaction.vo.ProductRatingVO;
import kr.ac.kopo.interaction.vo.RecommendationFeedbackVO;
import kr.ac.kopo.interaction.vo.UserProductEventVO;
import kr.ac.kopo.member.vo.MemberVO;
import kr.ac.kopo.product.vo.ProductVO;

public interface ProductInteractionService {
    boolean productExists(int productId);
    void favorite(MemberVO member, int productId);
    void unfavorite(MemberVO member, int productId);
    List<ProductVO> getFavorites(int memberId);
    void saveRating(MemberVO member, ProductRatingVO rating);
    void deleteRating(MemberVO member, int productId);
    ProductRatingVO getRating(int memberId, int productId);
    List<ProductRatingVO> getRatings(int memberId);
    void saveRecommendationFeedback(MemberVO member, int productId, String feedbackType);
    void deleteRecommendationFeedback(MemberVO member, int productId);
    List<RecommendationFeedbackVO> getRecommendationFeedback(int memberId);
    void recordEvent(MemberVO member, int productId, String eventType, String eventValue);
    List<UserProductEventVO> getRecentProducts(int memberId);
}
