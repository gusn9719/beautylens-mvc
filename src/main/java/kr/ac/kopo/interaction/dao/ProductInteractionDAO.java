package kr.ac.kopo.interaction.dao;

import java.util.List;
import java.util.Map;

import kr.ac.kopo.interaction.vo.ProductRatingVO;
import kr.ac.kopo.interaction.vo.RecommendationFeedbackVO;
import kr.ac.kopo.interaction.vo.UserProductEventVO;
import kr.ac.kopo.product.vo.ProductVO;

public interface ProductInteractionDAO {
    int existsProduct(int productId);
    int existsFavorite(Map<String, Object> param);
    void insertFavorite(Map<String, Object> param);
    void deleteFavorite(Map<String, Object> param);
    List<ProductVO> selectFavorites(int memberId);
    void mergeRating(ProductRatingVO rating);
    void deleteRating(Map<String, Object> param);
    ProductRatingVO selectRating(Map<String, Object> param);
    List<ProductRatingVO> selectRatingsByMember(int memberId);
    void deleteRecommendationFeedback(Map<String, Object> param);
    void insertRecommendationFeedback(RecommendationFeedbackVO feedback);
    List<RecommendationFeedbackVO> selectFeedbackByMember(int memberId);
    void insertEvent(UserProductEventVO event);
    List<UserProductEventVO> selectRecentProducts(int memberId);
}
