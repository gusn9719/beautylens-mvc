package kr.ac.kopo.recommendation.vo;

public class RecommendationVO {

    private Integer productId;
    private String platform;
    private String productName;
    private String displayName;
    private String productUrl;
    private String imageUrl;
    private String imageStatus;
    private String brand;
    private String category;
    private Double price;
    private Double avgRating;
    private Integer totalReviewCount;
    private Double overallPosRate;
    private Double overallNegRate;
    private String baseSkinType;
    private Integer skinReviewCount;
    private Double skinPosRate;
    private Double skinNegRate;
    private String topNeedTags;
    private String topConcernTags;
    private String cautionLevel;
    private Double recommendationScore;
    private String recommendationTier;
    private String evidenceLevel;
    private String reason;
    private Integer favoriteCount;
    private Integer siteRatingCount;
    private Double siteRatingAvg;
    private Double sameSkinRatingAvg;
    private Integer viewCount;
    private Integer commentCount;
    private Integer feedbackLikeCount;
    private Integer feedbackDislikeCount;
    private Double serviceScore;

    public RecommendationVO() {}

    public Integer getProductId()              { return productId; }
    public void setProductId(Integer v)        { this.productId = v; }

    public String getPlatform()               { return platform; }
    public void setPlatform(String v)         { this.platform = v; }

    public String getProductName()             { return productName; }
    public void setProductName(String v)       { this.productName = v; }

    public String getDisplayName()             { return displayName; }
    public void setDisplayName(String v)       { this.displayName = v; }

    public String getProductUrl()              { return productUrl; }
    public void setProductUrl(String v)        { this.productUrl = v; }

    public String getImageUrl()                { return imageUrl; }
    public void setImageUrl(String v)          { this.imageUrl = v; }

    public String getImageStatus()             { return imageStatus; }
    public void setImageStatus(String v)       { this.imageStatus = v; }

    public String getBrand()                   { return brand; }
    public void setBrand(String v)             { this.brand = v; }

    public String getCategory()                { return category; }
    public void setCategory(String v)          { this.category = v; }

    public Double getPrice()                   { return price; }
    public void setPrice(Double v)             { this.price = v; }

    public Double getAvgRating()               { return avgRating; }
    public void setAvgRating(Double v)         { this.avgRating = v; }

    public Integer getTotalReviewCount()       { return totalReviewCount; }
    public void setTotalReviewCount(Integer v) { this.totalReviewCount = v; }

    public Double getOverallPosRate()          { return overallPosRate; }
    public void setOverallPosRate(Double v)    { this.overallPosRate = v; }

    public Double getOverallNegRate()          { return overallNegRate; }
    public void setOverallNegRate(Double v)    { this.overallNegRate = v; }

    public String getBaseSkinType()            { return baseSkinType; }
    public void setBaseSkinType(String v)      { this.baseSkinType = v; }

    public Integer getSkinReviewCount()        { return skinReviewCount; }
    public void setSkinReviewCount(Integer v)  { this.skinReviewCount = v; }

    public Double getSkinPosRate()             { return skinPosRate; }
    public void setSkinPosRate(Double v)       { this.skinPosRate = v; }

    public Double getSkinNegRate()             { return skinNegRate; }
    public void setSkinNegRate(Double v)       { this.skinNegRate = v; }

    public String getTopNeedTags()             { return topNeedTags; }
    public void setTopNeedTags(String v)       { this.topNeedTags = v; }

    public String getTopConcernTags()          { return topConcernTags; }
    public void setTopConcernTags(String v)    { this.topConcernTags = v; }

    public String getCautionLevel()            { return cautionLevel; }
    public void setCautionLevel(String v)      { this.cautionLevel = v; }

    public Double getRecommendationScore()     { return recommendationScore; }
    public void setRecommendationScore(Double v) { this.recommendationScore = v; }

    public String getRecommendationTier()      { return recommendationTier; }
    public void setRecommendationTier(String v) { this.recommendationTier = v; }

    public String getEvidenceLevel()           { return evidenceLevel; }
    public void setEvidenceLevel(String v)     { this.evidenceLevel = v; }

    public String getReason()                  { return reason; }
    public void setReason(String v)            { this.reason = v; }

    public Integer getFavoriteCount()          { return favoriteCount; }
    public void setFavoriteCount(Integer v)    { this.favoriteCount = v; }

    public Integer getSiteRatingCount()        { return siteRatingCount; }
    public void setSiteRatingCount(Integer v)  { this.siteRatingCount = v; }

    public Double getSiteRatingAvg()           { return siteRatingAvg; }
    public void setSiteRatingAvg(Double v)     { this.siteRatingAvg = v; }

    public Double getSameSkinRatingAvg()       { return sameSkinRatingAvg; }
    public void setSameSkinRatingAvg(Double v) { this.sameSkinRatingAvg = v; }

    public Integer getViewCount()              { return viewCount; }
    public void setViewCount(Integer v)        { this.viewCount = v; }

    public Integer getCommentCount()           { return commentCount; }
    public void setCommentCount(Integer v)     { this.commentCount = v; }

    public Integer getFeedbackLikeCount()      { return feedbackLikeCount; }
    public void setFeedbackLikeCount(Integer v) { this.feedbackLikeCount = v; }

    public Integer getFeedbackDislikeCount()   { return feedbackDislikeCount; }
    public void setFeedbackDislikeCount(Integer v) { this.feedbackDislikeCount = v; }

    public Double getServiceScore()            { return serviceScore; }
    public void setServiceScore(Double v)      { this.serviceScore = v; }
}
