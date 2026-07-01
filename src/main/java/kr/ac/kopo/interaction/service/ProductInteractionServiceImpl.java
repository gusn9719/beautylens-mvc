package kr.ac.kopo.interaction.service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.ac.kopo.common.util.DisplayNameCleaner;
import kr.ac.kopo.interaction.dao.ProductInteractionDAO;
import kr.ac.kopo.interaction.vo.ProductRatingVO;
import kr.ac.kopo.interaction.vo.RecommendationFeedbackVO;
import kr.ac.kopo.interaction.vo.UserProductEventVO;
import kr.ac.kopo.member.vo.MemberVO;
import kr.ac.kopo.product.vo.ProductVO;

@Service
public class ProductInteractionServiceImpl implements ProductInteractionService {

    @Autowired
    private ProductInteractionDAO productInteractionDAO;

    @Override
    public boolean productExists(int productId) {
        return productInteractionDAO.existsProduct(productId) > 0;
    }

    @Override
    public void favorite(MemberVO member, int productId) {
        Map<String, Object> param = memberProductParam(member.getMemberId(), productId);
        if (productInteractionDAO.existsFavorite(param) == 0) {
            productInteractionDAO.insertFavorite(param);
        }
        recordEvent(member, productId, "FAVORITE", null);
    }

    @Override
    public void unfavorite(MemberVO member, int productId) {
        productInteractionDAO.deleteFavorite(memberProductParam(member.getMemberId(), productId));
        recordEvent(member, productId, "UNFAVORITE", null);
    }

    @Override
    public List<ProductVO> getFavorites(int memberId) {
        List<ProductVO> list = productInteractionDAO.selectFavorites(memberId);
        for (ProductVO product : list) {
            product.setDisplayName(DisplayNameCleaner.clean(product.getProductName()));
        }
        return list;
    }

    @Override
    public void saveRating(MemberVO member, ProductRatingVO rating) {
        rating.setMemberId(member.getMemberId());
        rating.setSkinTypeAtTime(member.getSkinType());
        productInteractionDAO.mergeRating(rating);
        recordEvent(member, rating.getProductId(), "RATE", String.valueOf(rating.getRating()));
    }

    @Override
    public void deleteRating(MemberVO member, int productId) {
        productInteractionDAO.deleteRating(memberProductParam(member.getMemberId(), productId));
        recordEvent(member, productId, "RATE", "DELETE");
    }

    @Override
    public ProductRatingVO getRating(int memberId, int productId) {
        ProductRatingVO rating = productInteractionDAO.selectRating(memberProductParam(memberId, productId));
        if (rating != null) {
            rating.setDisplayName(DisplayNameCleaner.clean(rating.getProductName()));
        }
        return rating;
    }

    @Override
    public List<ProductRatingVO> getRatings(int memberId) {
        List<ProductRatingVO> list = productInteractionDAO.selectRatingsByMember(memberId);
        for (ProductRatingVO rating : list) {
            rating.setDisplayName(DisplayNameCleaner.clean(rating.getProductName()));
        }
        return list;
    }

    @Override
    public void saveRecommendationFeedback(MemberVO member, int productId, String feedbackType) {
        RecommendationFeedbackVO feedback = new RecommendationFeedbackVO();
        feedback.setMemberId(member.getMemberId());
        feedback.setProductId(productId);
        feedback.setFeedbackType(normalizeFeedbackType(feedbackType));
        feedback.setSkinTypeAtTime(member.getSkinType());
        productInteractionDAO.deleteRecommendationFeedback(memberProductParam(member.getMemberId(), productId));
        productInteractionDAO.insertRecommendationFeedback(feedback);

        String eventType = switch (feedback.getFeedbackType()) {
            case "LIKE" -> "RECOMMEND_LIKE";
            case "DISLIKE" -> "RECOMMEND_DISLIKE";
            case "NOT_INTERESTED" -> "NOT_INTERESTED";
            default -> "RECOMMEND_FEEDBACK";
        };
        recordEvent(member, productId, eventType, feedback.getFeedbackType());
    }

    @Override
    public void deleteRecommendationFeedback(MemberVO member, int productId) {
        productInteractionDAO.deleteRecommendationFeedback(memberProductParam(member.getMemberId(), productId));
        recordEvent(member, productId, "RECOMMEND_FEEDBACK", "CANCEL");
    }

    @Override
    public List<RecommendationFeedbackVO> getRecommendationFeedback(int memberId) {
        List<RecommendationFeedbackVO> list = productInteractionDAO.selectFeedbackByMember(memberId);
        for (RecommendationFeedbackVO feedback : list) {
            feedback.setDisplayName(DisplayNameCleaner.clean(feedback.getProductName()));
        }
        return list;
    }

    @Override
    public void recordEvent(MemberVO member, int productId, String eventType, String eventValue) {
        UserProductEventVO event = new UserProductEventVO();
        event.setMemberId(member.getMemberId());
        event.setProductId(productId);
        event.setEventType(normalizeEventType(eventType));
        event.setEventValue(eventValue);
        event.setSkinTypeAtTime(member.getSkinType());
        productInteractionDAO.insertEvent(event);
    }

    @Override
    public List<UserProductEventVO> getRecentProducts(int memberId) {
        List<UserProductEventVO> list = productInteractionDAO.selectRecentProducts(memberId);
        for (UserProductEventVO event : list) {
            event.setDisplayName(DisplayNameCleaner.clean(event.getProductName()));
        }
        return list;
    }

    private Map<String, Object> memberProductParam(int memberId, int productId) {
        Map<String, Object> param = new HashMap<>();
        param.put("memberId", memberId);
        param.put("productId", productId);
        return param;
    }

    private String normalizeFeedbackType(String feedbackType) {
        if (feedbackType == null) {
            throw new IllegalArgumentException("feedbackType is required");
        }
        String normalized = feedbackType.trim().toUpperCase(Locale.ROOT);
        if (!normalized.equals("LIKE") && !normalized.equals("DISLIKE") && !normalized.equals("NOT_INTERESTED")) {
            throw new IllegalArgumentException("unsupported feedbackType");
        }
        return normalized;
    }

    private String normalizeEventType(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            return "VIEW";
        }
        return eventType.trim().toUpperCase(Locale.ROOT);
    }
}
