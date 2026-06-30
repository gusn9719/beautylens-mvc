package kr.ac.kopo.admin.dao;

import java.util.List;
import java.util.Map;

public interface AdminDAO {
    int selectProductCount();
    int selectReviewCount();
    int selectMemberCount();
    int selectCommentCount();
    int selectActiveCommentCount();
    int selectDeletedCommentCount();
    int selectImageFoundCount();
    int selectImageMissingCount();
    int selectFaceRegisteredCount();
    int selectHiddenProductCount();
    int selectRecommendationExcludedProductCount();
    int selectFeaturedProductCount();
    int selectSiteRatingCount();
    double selectSiteRatingAverage();
    int selectFavoriteCount();
    int selectRecentViewCount();
    int selectPendingReportCount();
    List<Map<String, Object>> selectPlatformCounts();
    List<Map<String, Object>> selectSkinTypeCounts();
    List<Map<String, Object>> selectSentimentCounts();
    List<Map<String, Object>> selectCautionCounts();
}
