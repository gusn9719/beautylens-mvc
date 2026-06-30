package kr.ac.kopo.interaction.vo;

public class RecommendationFeedbackVO {
    private Integer feedbackId;
    private Integer memberId;
    private Integer productId;
    private String feedbackType;
    private String skinTypeAtTime;
    private String createdAt;
    private String productName;
    private String displayName;
    private String brand;
    private String imageUrl;

    public Integer getFeedbackId() { return feedbackId; }
    public void setFeedbackId(Integer feedbackId) { this.feedbackId = feedbackId; }
    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getFeedbackType() { return feedbackType; }
    public void setFeedbackType(String feedbackType) { this.feedbackType = feedbackType; }
    public String getSkinTypeAtTime() { return skinTypeAtTime; }
    public void setSkinTypeAtTime(String skinTypeAtTime) { this.skinTypeAtTime = skinTypeAtTime; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
