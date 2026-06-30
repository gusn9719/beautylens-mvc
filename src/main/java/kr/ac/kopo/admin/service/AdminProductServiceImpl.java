package kr.ac.kopo.admin.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.ac.kopo.admin.dao.AdminProductDAO;
import kr.ac.kopo.admin.vo.ProductAdminFlagVO;
import kr.ac.kopo.common.util.DisplayNameCleaner;
import kr.ac.kopo.member.vo.MemberVO;
import kr.ac.kopo.product.vo.ProductVO;

@Service
public class AdminProductServiceImpl implements AdminProductService {

    @Autowired
    private AdminProductDAO adminProductDAO;

    @Autowired
    private AdminAuditLogService adminAuditLogService;

    @Override
    public List<ProductVO> getAdminProducts(Map<String, Object> param) {
        List<ProductVO> list = adminProductDAO.selectAdminProducts(param);
        for (ProductVO product : list) {
            product.setDisplayName(DisplayNameCleaner.clean(product.getProductName()));
        }
        return list;
    }

    @Override
    public ProductAdminFlagVO getFlags(int productId) {
        ProductAdminFlagVO flags = adminProductDAO.selectFlags(productId);
        if (flags == null) {
            flags = defaults(productId);
        }
        return flags;
    }

    @Override
    public void saveFlags(MemberVO admin, ProductAdminFlagVO flags) {
        ProductAdminFlagVO before = adminProductDAO.selectFlags(flags.getProductId());
        ProductAdminFlagVO normalized = normalize(flags);
        normalized.setUpdatedBy(admin.getMemberId());
        adminProductDAO.mergeFlags(normalized);
        adminAuditLogService.log(admin, "PRODUCT_FLAGS_SAVE", "PRODUCT", flags.getProductId(),
                before == null ? null : before.getQualityStatus() + "|" + before.getAdminMemo(),
                normalized.getQualityStatus() + "|" + normalized.getAdminMemo());
    }

    @Override
    public void hide(MemberVO admin, int productId, String reason) {
        shortcut(admin, productId, "IS_VISIBLE", "N", reason);
    }

    @Override
    public void restore(MemberVO admin, int productId) {
        shortcut(admin, productId, "IS_VISIBLE", "Y", null);
    }

    @Override
    public void excludeRecommendation(MemberVO admin, int productId) {
        shortcut(admin, productId, "EXCLUDE_RECOMMENDATION", "Y", null);
    }

    @Override
    public void includeRecommendation(MemberVO admin, int productId) {
        shortcut(admin, productId, "EXCLUDE_RECOMMENDATION", "N", null);
    }

    @Override
    public void feature(MemberVO admin, int productId) {
        shortcut(admin, productId, "IS_FEATURED", "Y", null);
    }

    @Override
    public void unfeature(MemberVO admin, int productId) {
        shortcut(admin, productId, "IS_FEATURED", "N", null);
    }

    private void shortcut(MemberVO admin, int productId, String field, String value, String reason) {
        ensureRow(admin, productId);
        Map<String, Object> param = new HashMap<>();
        param.put("productId", productId);
        param.put("field", field);
        param.put("value", value);
        param.put("reason", reason);
        param.put("updatedBy", admin.getMemberId());
        adminProductDAO.updateShortcut(param);
        adminAuditLogService.log(admin, "PRODUCT_" + field, "PRODUCT", productId, null, value);
    }

    private void ensureRow(MemberVO admin, int productId) {
        if (adminProductDAO.selectFlags(productId) == null) {
            ProductAdminFlagVO flags = defaults(productId);
            flags.setUpdatedBy(admin.getMemberId());
            adminProductDAO.mergeFlags(flags);
        }
    }

    private ProductAdminFlagVO defaults(int productId) {
        ProductAdminFlagVO flags = new ProductAdminFlagVO();
        flags.setProductId(productId);
        flags.setIsVisible("Y");
        flags.setExcludeRecommendation("N");
        flags.setIsFeatured("N");
        flags.setQualityStatus("NORMAL");
        return flags;
    }

    private ProductAdminFlagVO normalize(ProductAdminFlagVO flags) {
        ProductAdminFlagVO normalized = defaults(flags.getProductId());
        normalized.setIsVisible(yn(flags.getIsVisible(), "Y"));
        normalized.setExcludeRecommendation(yn(flags.getExcludeRecommendation(), "N"));
        normalized.setIsFeatured(yn(flags.getIsFeatured(), "N"));
        normalized.setQualityStatus(flags.getQualityStatus() == null || flags.getQualityStatus().isBlank()
                ? "NORMAL" : flags.getQualityStatus());
        normalized.setHideReason(flags.getHideReason());
        normalized.setAdminMemo(flags.getAdminMemo());
        return normalized;
    }

    private String yn(String value, String fallback) {
        if ("Y".equalsIgnoreCase(value)) return "Y";
        if ("N".equalsIgnoreCase(value)) return "N";
        return fallback;
    }
}
