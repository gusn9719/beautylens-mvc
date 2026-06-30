package kr.ac.kopo.admin.vo;

public class ProductAdminFlagVO {
    private Integer productId;
    private String isVisible;
    private String excludeRecommendation;
    private String isFeatured;
    private String qualityStatus;
    private String hideReason;
    private String adminMemo;
    private Integer updatedBy;
    private String updatedAt;

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getIsVisible() { return isVisible; }
    public void setIsVisible(String isVisible) { this.isVisible = isVisible; }
    public String getExcludeRecommendation() { return excludeRecommendation; }
    public void setExcludeRecommendation(String excludeRecommendation) { this.excludeRecommendation = excludeRecommendation; }
    public String getIsFeatured() { return isFeatured; }
    public void setIsFeatured(String isFeatured) { this.isFeatured = isFeatured; }
    public String getQualityStatus() { return qualityStatus; }
    public void setQualityStatus(String qualityStatus) { this.qualityStatus = qualityStatus; }
    public String getHideReason() { return hideReason; }
    public void setHideReason(String hideReason) { this.hideReason = hideReason; }
    public String getAdminMemo() { return adminMemo; }
    public void setAdminMemo(String adminMemo) { this.adminMemo = adminMemo; }
    public Integer getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Integer updatedBy) { this.updatedBy = updatedBy; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
