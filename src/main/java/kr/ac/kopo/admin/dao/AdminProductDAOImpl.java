package kr.ac.kopo.admin.dao;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.ac.kopo.admin.vo.ProductAdminFlagVO;
import kr.ac.kopo.product.vo.ProductVO;

@Repository
public class AdminProductDAOImpl implements AdminProductDAO {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    @Override
    public List<ProductVO> selectAdminProducts(Map<String, Object> param) {
        return sqlSessionTemplate.selectList("admin.product.dao.AdminProductDAO.selectAdminProducts", param);
    }

    @Override
    public ProductAdminFlagVO selectFlags(int productId) {
        return sqlSessionTemplate.selectOne("admin.product.dao.AdminProductDAO.selectFlags", productId);
    }

    @Override
    public void mergeFlags(ProductAdminFlagVO flags) {
        sqlSessionTemplate.update("admin.product.dao.AdminProductDAO.mergeFlags", flags);
    }

    @Override
    public void updateShortcut(Map<String, Object> param) {
        sqlSessionTemplate.update("admin.product.dao.AdminProductDAO.updateShortcut", param);
    }
}
