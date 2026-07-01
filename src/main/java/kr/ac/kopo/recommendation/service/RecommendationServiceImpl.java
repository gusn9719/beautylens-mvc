package kr.ac.kopo.recommendation.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.ac.kopo.common.util.DisplayNameCleaner;
import kr.ac.kopo.recommendation.dao.RecommendationDAO;
import kr.ac.kopo.recommendation.vo.RecommendationVO;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private static final double BASE_SCORE_WEIGHT = 0.70;
    private static final double SITE_RATING_WEIGHT = 0.15;
    private static final double SAME_SKIN_RATING_WEIGHT = 0.10;
    private static final double ENGAGEMENT_WEIGHT = 0.05;
    private static final double IMAGE_MISSING_PENALTY = 3.0;
    private static final double DISLIKE_PENALTY = 1.0;

    @Autowired
    private RecommendationDAO recommendationDAO;

    private String toKoreanSkinType(String skinType) {
        if (skinType == null) return null;
        switch (skinType.toLowerCase()) {
            case "dry":         return "건성";
            case "oily":        return "지성";
            case "combination": return "복합성";
            case "sensitive":   return "민감성";
            case "normal":      return "중성";
            default:            return skinType;
        }
    }

    @Override
    public List<RecommendationVO> recommend(String skinType, String skinConcern, int size) {
        return recommend(skinType, skinConcern, size, null);
    }

    @Override
    public List<RecommendationVO> recommend(String skinType, String skinConcern, int size, Integer memberId) {
        String koreanSkinType = toKoreanSkinType(skinType);

        Map<String, Object> param = new HashMap<>();
        param.put("skinType", koreanSkinType);
        param.put("size", size);
        param.put("memberId", memberId);

        List<RecommendationVO> list = recommendationDAO.selectBySkinType(param);

        if (list.size() < size) {
            Map<String, Object> fallbackParam = new HashMap<>();
            fallbackParam.put("size", Math.max(size * 2, size + 10));
            fallbackParam.put("skinType", koreanSkinType);
            fallbackParam.put("memberId", memberId);
            List<RecommendationVO> fallback = recommendationDAO.selectFallback(fallbackParam);
            for (RecommendationVO candidate : fallback) {
                if (list.size() >= size) break;
                if (!containsProduct(list, candidate.getProductId())) {
                    list.add(candidate);
                }
            }
        }

        for (RecommendationVO vo : list) {
            vo.setDisplayName(DisplayNameCleaner.clean(vo.getProductName()));
            vo.setServiceScore(calculateServiceScore(vo));
            vo.setReason(buildReason(vo, koreanSkinType, skinConcern));
        }

        return list;
    }

    private boolean containsProduct(List<RecommendationVO> list, Integer productId) {
        if (productId == null) return false;
        for (RecommendationVO vo : list) {
            if (productId.equals(vo.getProductId())) return true;
        }
        return false;
    }

    private double calculateServiceScore(RecommendationVO vo) {
        double base = nvl(vo.getRecommendationScore());
        double siteRatingScore = vo.getSiteRatingAvg() == null ? base : vo.getSiteRatingAvg() * 20.0;
        double sameSkinScore = vo.getSameSkinRatingAvg() == null ? siteRatingScore : vo.getSameSkinRatingAvg() * 20.0;
        double engagementScore = engagementScore(vo);
        double penalty = 0.0;
        if (vo.getImageUrl() == null || vo.getImageUrl().isBlank()) {
            penalty += IMAGE_MISSING_PENALTY;
        }
        penalty += nvl(vo.getFeedbackDislikeCount()) * DISLIKE_PENALTY;

        double score = base * BASE_SCORE_WEIGHT
                + siteRatingScore * SITE_RATING_WEIGHT
                + sameSkinScore * SAME_SKIN_RATING_WEIGHT
                + engagementScore * ENGAGEMENT_WEIGHT
                - penalty;
        return Math.max(0.0, Math.round(score * 100.0) / 100.0);
    }

    private double engagementScore(RecommendationVO vo) {
        double raw = nvl(vo.getFavoriteCount()) * 3.0
                + nvl(vo.getViewCount()) * 0.2
                + nvl(vo.getCommentCount()) * 2.0
                + nvl(vo.getFeedbackLikeCount()) * 4.0;
        return Math.min(100.0, raw);
    }

    private double nvl(Number value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    private String buildReason(RecommendationVO vo, String koreanSkinType, String skinConcern) {
        StringBuilder sb = new StringBuilder();

        boolean skinTypeMatch = koreanSkinType != null
                && koreanSkinType.equals(vo.getBaseSkinType());

        if (skinTypeMatch) {
            sb.append(vo.getBaseSkinType()).append(" 피부 타입 기준 추천 점수가 높습니다.");
        } else {
            sb.append("다양한 피부 타입에 적합한 상품으로 추천됩니다.");
        }

        if (skinConcern != null && !skinConcern.isBlank()) {
            StringBuilder matchedKeywords = new StringBuilder();
            String[] concerns = skinConcern.split("[,，\\s]+");
            for (String c : concerns) {
                String keyword = c.trim();
                if (keyword.isEmpty()) continue;
                if ((vo.getTopNeedTags() != null && vo.getTopNeedTags().contains(keyword))
                        || (vo.getTopConcernTags() != null && vo.getTopConcernTags().contains(keyword))) {
                    if (matchedKeywords.length() > 0) {
                        matchedKeywords.append(", ");
                    }
                    matchedKeywords.append(keyword);
                }
            }
            if (matchedKeywords.length() > 0) {
                sb.append(" 선택한 관심사(").append(matchedKeywords).append(")와 관련된 태그가 포함되어 있습니다.");
            }
        }

        String caution = vo.getCautionLevel();
        if ("high_negative_signal".equals(caution)) {
            sb.append(" 부정 리뷰 신호가 높은 편이므로 구매 전 부정 리뷰 확인이 필요합니다.");
        } else if ("moderate_negative_signal".equals(caution)) {
            sb.append(" 일부 부정 리뷰 신호가 있어 상세 리뷰 확인을 권장합니다.");
        } else if ("insufficient_evidence".equals(caution)) {
            sb.append(" 리뷰 데이터가 충분하지 않아 신뢰도 판단에 참고가 필요합니다.");
        } else {
            sb.append(" 부정 리뷰 신호가 낮은 편입니다.");
        }

        return sb.toString().trim();
    }
}
