package kr.ac.kopo.product.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.ac.kopo.common.vo.PageParam;
import kr.ac.kopo.product.vo.ProductVO;

@Repository
public class ProductDAOImpl implements ProductDAO {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    @Override
    public List<ProductVO> selectList(PageParam param) {
        return sqlSessionTemplate.selectList("product.dao.ProductDAO.selectList", param);
    }

    @Override
    public ProductVO selectOne(int productId) {
        return sqlSessionTemplate.selectOne("product.dao.ProductDAO.selectOne", productId);
    }
}
