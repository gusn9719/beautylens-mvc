package kr.ac.kopo.admin.dao;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public class AdminDAOImpl implements AdminDAO {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    @Override
    public int selectProductCount() {
        return sqlSessionTemplate.selectOne("admin.dao.AdminDAO.selectProductCount");
    }

    @Override
    public int selectReviewCount() {
        return sqlSessionTemplate.selectOne("admin.dao.AdminDAO.selectReviewCount");
    }

    @Override
    public int selectMemberCount() {
        return sqlSessionTemplate.selectOne("admin.dao.AdminDAO.selectMemberCount");
    }

    @Override
    public int selectCommentCount() {
        return sqlSessionTemplate.selectOne("admin.dao.AdminDAO.selectCommentCount");
    }

    @Override
    public int selectActiveCommentCount() {
        return sqlSessionTemplate.selectOne("admin.dao.AdminDAO.selectActiveCommentCount");
    }

    @Override
    public int selectDeletedCommentCount() {
        return sqlSessionTemplate.selectOne("admin.dao.AdminDAO.selectDeletedCommentCount");
    }

    @Override
    public int selectImageFoundCount() {
        return sqlSessionTemplate.selectOne("admin.dao.AdminDAO.selectImageFoundCount");
    }

    @Override
    public int selectImageMissingCount() {
        return sqlSessionTemplate.selectOne("admin.dao.AdminDAO.selectImageMissingCount");
    }

    @Override
    public int selectFaceRegisteredCount() {
        return sqlSessionTemplate.selectOne("admin.dao.AdminDAO.selectFaceRegisteredCount");
    }

    @Override
    public int selectHiddenProductCount() {
        return sqlSessionTemplate.selectOne("admin.dao.AdminDAO.selectHiddenProductCount");
    }

    @Override
    public int selectRecommendationExcludedProductCount() {
        return sqlSessionTemplate.selectOne("admin.dao.AdminDAO.selectRecommendationExcludedProductCount");
    }

    @Override
    public int selectFeaturedProductCount() {
        return sqlSessionTemplate.selectOne("admin.dao.AdminDAO.selectFeaturedProductCount");
    }

    @Override
    public int selectSiteRatingCount() {
        return sqlSessionTemplate.selectOne("admin.dao.AdminDAO.selectSiteRatingCount");
    }

    @Override
    public double selectSiteRatingAverage() {
        Number n = sqlSessionTemplate.selectOne("admin.dao.AdminDAO.selectSiteRatingAverage");
        return n == null ? 0.0 : n.doubleValue();
    }

    @Override
    public int selectFavoriteCount() {
        return sqlSessionTemplate.selectOne("admin.dao.AdminDAO.selectFavoriteCount");
    }

    @Override
    public int selectRecentViewCount() {
        return sqlSessionTemplate.selectOne("admin.dao.AdminDAO.selectRecentViewCount");
    }

    @Override
    public int selectPendingReportCount() {
        return sqlSessionTemplate.selectOne("admin.dao.AdminDAO.selectPendingReportCount");
    }

    @Override
    public List<Map<String, Object>> selectPlatformCounts() {
        return sqlSessionTemplate.selectList("admin.dao.AdminDAO.selectPlatformCounts");
    }

    @Override
    public List<Map<String, Object>> selectSkinTypeCounts() {
        return sqlSessionTemplate.selectList("admin.dao.AdminDAO.selectSkinTypeCounts");
    }

    @Override
    public List<Map<String, Object>> selectSentimentCounts() {
        return sqlSessionTemplate.selectList("admin.dao.AdminDAO.selectSentimentCounts");
    }

    @Override
    public List<Map<String, Object>> selectCautionCounts() {
        return sqlSessionTemplate.selectList("admin.dao.AdminDAO.selectCautionCounts");
    }
}
