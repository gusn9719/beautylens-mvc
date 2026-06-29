package kr.ac.kopo.review.dao;

import java.util.List;

import kr.ac.kopo.review.vo.ReviewVO;

public interface ReviewDAO {
    List<ReviewVO> selectByProductId(int productId);
    List<ReviewVO> selectByProductIdAndSentiment(int productId, String sentimentLabel);
}
