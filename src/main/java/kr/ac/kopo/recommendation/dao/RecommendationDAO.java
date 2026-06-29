package kr.ac.kopo.recommendation.dao;

import java.util.List;
import java.util.Map;

import kr.ac.kopo.recommendation.vo.RecommendationVO;

public interface RecommendationDAO {
    List<RecommendationVO> selectBySkinType(Map<String, Object> param);
    List<RecommendationVO> selectFallback(Map<String, Object> param);
}
