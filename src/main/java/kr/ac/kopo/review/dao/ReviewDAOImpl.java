package kr.ac.kopo.review.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.ac.kopo.review.vo.ReviewVO;

@Repository
public class ReviewDAOImpl implements ReviewDAO {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    @Override
    public List<ReviewVO> selectByProductId(int productId) {
        return sqlSessionTemplate.selectList("review.dao.ReviewDAO.selectByProductId", productId);
    }

    @Override
    public List<ReviewVO> selectByProductIdAndSentiment(int productId, String sentimentLabel) {
        Map<String, Object> param = new HashMap<>();
        param.put("productId", productId);
        param.put("sentimentLabel", sentimentLabel);
        return sqlSessionTemplate.selectList("review.dao.ReviewDAO.selectBySentiment", param);
    }
}
