package kr.ac.kopo.review.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.ac.kopo.review.dao.ReviewDAO;
import kr.ac.kopo.review.vo.ReviewVO;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewDAO reviewDAO;

    @Override
    public List<ReviewVO> getReviews(int productId) {
        return reviewDAO.selectByProductId(productId);
    }

    @Override
    public List<ReviewVO> getReviewsBySentiment(int productId, String sentimentLabel) {
        return reviewDAO.selectByProductIdAndSentiment(productId, sentimentLabel);
    }
}
