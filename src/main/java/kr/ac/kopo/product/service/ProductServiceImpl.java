package kr.ac.kopo.product.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.ac.kopo.common.util.DisplayNameCleaner;
import kr.ac.kopo.common.vo.PageParam;
import kr.ac.kopo.product.dao.ProductDAO;
import kr.ac.kopo.product.vo.ProductVO;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductDAO productDAO;

    @Override
    public List<ProductVO> getProducts(PageParam param) {
        List<ProductVO> list = productDAO.selectList(param);
        for (ProductVO vo : list) {
            vo.setDisplayName(DisplayNameCleaner.clean(vo.getProductName()));
        }
        return list;
    }

    @Override
    public ProductVO getProduct(int productId) {
        ProductVO vo = productDAO.selectOne(productId);
        if (vo != null) {
            vo.setDisplayName(DisplayNameCleaner.clean(vo.getProductName()));
        }
        return vo;
    }
}
