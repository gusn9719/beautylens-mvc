package kr.ac.kopo.admin.service;

import java.util.List;
import java.util.Map;

import kr.ac.kopo.admin.vo.ProductAdminFlagVO;
import kr.ac.kopo.member.vo.MemberVO;
import kr.ac.kopo.product.vo.ProductVO;

public interface AdminProductService {
    List<ProductVO> getAdminProducts(Map<String, Object> param);
    ProductAdminFlagVO getFlags(int productId);
    void saveFlags(MemberVO admin, ProductAdminFlagVO flags);
    void hide(MemberVO admin, int productId, String reason);
    void restore(MemberVO admin, int productId);
    void excludeRecommendation(MemberVO admin, int productId);
    void includeRecommendation(MemberVO admin, int productId);
    void feature(MemberVO admin, int productId);
    void unfeature(MemberVO admin, int productId);
}
