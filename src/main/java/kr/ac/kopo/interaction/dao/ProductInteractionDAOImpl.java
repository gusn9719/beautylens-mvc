package kr.ac.kopo.interaction.dao;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.ac.kopo.interaction.vo.ProductRatingVO;
import kr.ac.kopo.interaction.vo.RecommendationFeedbackVO;
import kr.ac.kopo.interaction.vo.UserProductEventVO;
import kr.ac.kopo.product.vo.ProductVO;

@Repository
public class ProductInteractionDAOImpl implements ProductInteractionDAO {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    @Override
    public int existsProduct(int productId) {
        return sqlSessionTemplate.selectOne("interaction.dao.ProductInteractionDAO.existsProduct", productId);
    }

    @Override
    public int existsFavorite(Map<String, Object> param) {
        return sqlSessionTemplate.selectOne("interaction.dao.ProductInteractionDAO.existsFavorite", param);
    }

    @Override
    public void insertFavorite(Map<String, Object> param) {
        sqlSessionTemplate.insert("interaction.dao.ProductInteractionDAO.insertFavorite", param);
    }

    @Override
    public void deleteFavorite(Map<String, Object> param) {
        sqlSessionTemplate.delete("interaction.dao.ProductInteractionDAO.deleteFavorite", param);
    }

    @Override
    public List<ProductVO> selectFavorites(int memberId) {
        return sqlSessionTemplate.selectList("interaction.dao.ProductInteractionDAO.selectFavorites", memberId);
    }

    @Override
    public void mergeRating(ProductRatingVO rating) {
        sqlSessionTemplate.update("interaction.dao.ProductInteractionDAO.mergeRating", rating);
    }

    @Override
    public void deleteRating(Map<String, Object> param) {
        sqlSessionTemplate.delete("interaction.dao.ProductInteractionDAO.deleteRating", param);
    }

    @Override
    public ProductRatingVO selectRating(Map<String, Object> param) {
        return sqlSessionTemplate.selectOne("interaction.dao.ProductInteractionDAO.selectRating", param);
    }

    @Override
    public List<ProductRatingVO> selectRatingsByMember(int memberId) {
        return sqlSessionTemplate.selectList("interaction.dao.ProductInteractionDAO.selectRatingsByMember", memberId);
    }

    @Override
    public void deleteRecommendationFeedback(Map<String, Object> param) {
        sqlSessionTemplate.delete("interaction.dao.ProductInteractionDAO.deleteRecommendationFeedback", param);
    }

    @Override
    public void insertRecommendationFeedback(RecommendationFeedbackVO feedback) {
        sqlSessionTemplate.insert("interaction.dao.ProductInteractionDAO.insertRecommendationFeedback", feedback);
    }

    @Override
    public List<RecommendationFeedbackVO> selectFeedbackByMember(int memberId) {
        return sqlSessionTemplate.selectList("interaction.dao.ProductInteractionDAO.selectFeedbackByMember", memberId);
    }

    @Override
    public void insertEvent(UserProductEventVO event) {
        sqlSessionTemplate.insert("interaction.dao.ProductInteractionDAO.insertEvent", event);
    }

    @Override
    public List<UserProductEventVO> selectRecentProducts(int memberId) {
        return sqlSessionTemplate.selectList("interaction.dao.ProductInteractionDAO.selectRecentProducts", memberId);
    }
}
