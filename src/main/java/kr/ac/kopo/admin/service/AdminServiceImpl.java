package kr.ac.kopo.admin.service;

import kr.ac.kopo.admin.dao.AdminDAO;
import kr.ac.kopo.admin.vo.AdminSummaryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminDAO adminDAO;

    @Override
    public AdminSummaryVO getSummary() {
        AdminSummaryVO s = new AdminSummaryVO();
        s.setProductCount(adminDAO.selectProductCount());
        s.setReviewCount(adminDAO.selectReviewCount());
        s.setMemberCount(adminDAO.selectMemberCount());
        s.setCommentCount(adminDAO.selectCommentCount());
        s.setActiveCommentCount(adminDAO.selectActiveCommentCount());
        s.setDeletedCommentCount(adminDAO.selectDeletedCommentCount());
        s.setImageFoundProductCount(adminDAO.selectImageFoundCount());
        s.setImageMissingProductCount(adminDAO.selectImageMissingCount());
        try {
            s.setFaceRegisteredMemberCount(adminDAO.selectFaceRegisteredCount());
        } catch (RuntimeException e) {
            s.setFaceRegisteredMemberCount(0);
        }
        s.setHiddenProductCount(adminDAO.selectHiddenProductCount());
        s.setRecommendationExcludedProductCount(adminDAO.selectRecommendationExcludedProductCount());
        s.setFeaturedProductCount(adminDAO.selectFeaturedProductCount());
        s.setSiteRatingCount(adminDAO.selectSiteRatingCount());
        s.setSiteRatingAverage(adminDAO.selectSiteRatingAverage());
        s.setFavoriteCount(adminDAO.selectFavoriteCount());
        s.setRecentViewCount(adminDAO.selectRecentViewCount());
        s.setPendingReportCount(adminDAO.selectPendingReportCount());

        s.setPlatformProductCounts(toCountMap(adminDAO.selectPlatformCounts(), "PLATFORM", "CNT"));
        s.setSkinTypeProductCounts(toCountMap(adminDAO.selectSkinTypeCounts(), "SKINTYPE", "CNT"));
        s.setSentimentCounts(toCountMap(adminDAO.selectSentimentCounts(), "SENTIMENTLABEL", "CNT"));
        s.setCautionLevelCounts(toCountMap(adminDAO.selectCautionCounts(), "CAUTIONLEVEL", "CNT"));
        return s;
    }

    private Map<String, Integer> toCountMap(List<Map<String, Object>> rows, String keyCol, String cntCol) {
        Map<String, Integer> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String key = null;
            Object cnt = null;
            // Oracle returns uppercase column names
            for (Map.Entry<String, Object> e : row.entrySet()) {
                if (e.getKey().equalsIgnoreCase(keyCol)) key = String.valueOf(e.getValue());
                if (e.getKey().equalsIgnoreCase(cntCol))  cnt = e.getValue();
            }
            if (key != null && cnt != null) {
                result.put(key, ((Number) cnt).intValue());
            }
        }
        return result;
    }
}
