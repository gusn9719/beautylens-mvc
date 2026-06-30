package kr.ac.kopo.admin.dao;

import java.util.List;
import java.util.Map;

import kr.ac.kopo.admin.vo.ProductAdminFlagVO;
import kr.ac.kopo.product.vo.ProductVO;

public interface AdminProductDAO {
    List<ProductVO> selectAdminProducts(Map<String, Object> param);
    ProductAdminFlagVO selectFlags(int productId);
    void mergeFlags(ProductAdminFlagVO flags);
    void updateShortcut(Map<String, Object> param);
}
