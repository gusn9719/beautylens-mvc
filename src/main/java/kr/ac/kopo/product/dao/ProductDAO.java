package kr.ac.kopo.product.dao;

import java.util.List;

import kr.ac.kopo.common.vo.PageParam;
import kr.ac.kopo.product.vo.ProductVO;

public interface ProductDAO {
    List<ProductVO> selectList(PageParam param);
    ProductVO selectOne(int productId);
}
