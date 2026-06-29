package kr.ac.kopo.recommendation.dao;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.ac.kopo.recommendation.vo.RecommendationVO;

@Repository
public class RecommendationDAOImpl implements RecommendationDAO {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    @Override
    public List<RecommendationVO> selectBySkinType(Map<String, Object> param) {
        return sqlSessionTemplate.selectList(
                "recommendation.dao.RecommendationDAO.selectBySkinType", param);
    }

    @Override
    public List<RecommendationVO> selectFallback(Map<String, Object> param) {
        return sqlSessionTemplate.selectList(
                "recommendation.dao.RecommendationDAO.selectFallback", param);
    }
}
