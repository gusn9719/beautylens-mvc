package kr.ac.kopo.product.service;

import java.util.List;

import kr.ac.kopo.common.vo.PageParam;
import kr.ac.kopo.product.vo.ProductVO;

public interface ProductService {
    List<ProductVO> getProducts(PageParam param);
    ProductVO getProduct(int productId);
}
