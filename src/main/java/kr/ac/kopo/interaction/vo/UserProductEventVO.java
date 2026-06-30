package kr.ac.kopo.interaction.vo;

public class UserProductEventVO {
    private Integer eventId;
    private Integer memberId;
    private Integer productId;
    private String eventType;
    private String eventValue;
    private String skinTypeAtTime;
    private String createdAt;
    private String productName;
    private String displayName;
    private String brand;
    private String imageUrl;

    public Integer getEventId() { return eventId; }
    public void setEventId(Integer eventId) { this.eventId = eventId; }
    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getEventValue() { return eventValue; }
    public void setEventValue(String eventValue) { this.eventValue = eventValue; }
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
