package kr.ac.kopo.interaction.vo;

public class ProductRatingVO {
    private Integer ratingId;
    private Integer memberId;
    private Integer productId;
    private Double rating;
    private String skinTypeAtTime;
    private String irritationYn;
    private String repurchaseYn;
    private String reviewText;
    private String createdAt;
    private String updatedAt;
    private String productName;
    private String displayName;
    private String brand;
    private String imageUrl;

    public Integer getRatingId() { return ratingId; }
    public void setRatingId(Integer ratingId) { this.ratingId = ratingId; }
    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public String getSkinTypeAtTime() { return skinTypeAtTime; }
    public void setSkinTypeAtTime(String skinTypeAtTime) { this.skinTypeAtTime = skinTypeAtTime; }
    public String getIrritationYn() { return irritationYn; }
    public void setIrritationYn(String irritationYn) { this.irritationYn = irritationYn; }
    public String getRepurchaseYn() { return repurchaseYn; }
    public void setRepurchaseYn(String repurchaseYn) { this.repurchaseYn = repurchaseYn; }
    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
