package kr.ac.kopo.recommendation.service;

import java.util.List;

import kr.ac.kopo.recommendation.vo.RecommendationVO;

public interface RecommendationService {
    List<RecommendationVO> recommend(String skinType, String skinConcern, int size);
    List<RecommendationVO> recommend(String skinType, String skinConcern, int size, Integer memberId);
}
