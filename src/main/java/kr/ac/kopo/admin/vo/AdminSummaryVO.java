package kr.ac.kopo.admin.vo;

import java.util.Map;

public class AdminSummaryVO {

    private int productCount;
    private int reviewCount;
    private int memberCount;
    private int commentCount;
    private int activeCommentCount;
    private int deletedCommentCount;
    private int imageFoundProductCount;
    private int imageMissingProductCount;
    private int faceRegisteredMemberCount;
    private int hiddenProductCount;
    private int recommendationExcludedProductCount;
    private int featuredProductCount;
    private int siteRatingCount;
    private double siteRatingAverage;
    private int favoriteCount;
    private int recentViewCount;
    private int pendingReportCount;
    private Map<String, Integer> platformProductCounts;
    private Map<String, Integer> skinTypeProductCounts;
    private Map<String, Integer> sentimentCounts;
    private Map<String, Integer> cautionLevelCounts;

    public AdminSummaryVO() {}

    public int getProductCount()                              { return productCount; }
    public void setProductCount(int v)                        { this.productCount = v; }

    public int getReviewCount()                               { return reviewCount; }
    public void setReviewCount(int v)                         { this.reviewCount = v; }

    public int getMemberCount()                               { return memberCount; }
    public void setMemberCount(int v)                         { this.memberCount = v; }

    public int getCommentCount()                              { return commentCount; }
    public void setCommentCount(int v)                        { this.commentCount = v; }

    public int getActiveCommentCount()                        { return activeCommentCount; }
    public void setActiveCommentCount(int v)                  { this.activeCommentCount = v; }

    public int getDeletedCommentCount()                       { return deletedCommentCount; }
    public void setDeletedCommentCount(int v)                 { this.deletedCommentCount = v; }

    public int getImageFoundProductCount()                    { return imageFoundProductCount; }
    public void setImageFoundProductCount(int v)              { this.imageFoundProductCount = v; }

    public int getImageMissingProductCount()                   { return imageMissingProductCount; }
    public void setImageMissingProductCount(int v)             { this.imageMissingProductCount = v; }

    public int getFaceRegisteredMemberCount()                  { return faceRegisteredMemberCount; }
    public void setFaceRegisteredMemberCount(int v)            { this.faceRegisteredMemberCount = v; }

    public int getHiddenProductCount()                         { return hiddenProductCount; }
    public void setHiddenProductCount(int v)                   { this.hiddenProductCount = v; }

    public int getRecommendationExcludedProductCount()          { return recommendationExcludedProductCount; }
    public void setRecommendationExcludedProductCount(int v)    { this.recommendationExcludedProductCount = v; }

    public int getFeaturedProductCount()                       { return featuredProductCount; }
    public void setFeaturedProductCount(int v)                 { this.featuredProductCount = v; }

    public int getSiteRatingCount()                            { return siteRatingCount; }
    public void setSiteRatingCount(int v)                      { this.siteRatingCount = v; }

    public double getSiteRatingAverage()                       { return siteRatingAverage; }
    public void setSiteRatingAverage(double v)                 { this.siteRatingAverage = v; }

    public int getFavoriteCount()                              { return favoriteCount; }
    public void setFavoriteCount(int v)                        { this.favoriteCount = v; }

    public int getRecentViewCount()                            { return recentViewCount; }
    public void setRecentViewCount(int v)                      { this.recentViewCount = v; }

    public int getPendingReportCount()                         { return pendingReportCount; }
    public void setPendingReportCount(int v)                   { this.pendingReportCount = v; }

    public Map<String, Integer> getPlatformProductCounts()    { return platformProductCounts; }
    public void setPlatformProductCounts(Map<String, Integer> v) { this.platformProductCounts = v; }

    public Map<String, Integer> getSkinTypeProductCounts()     { return skinTypeProductCounts; }
    public void setSkinTypeProductCounts(Map<String, Integer> v) { this.skinTypeProductCounts = v; }

    public Map<String, Integer> getSentimentCounts()          { return sentimentCounts; }
    public void setSentimentCounts(Map<String, Integer> v)    { this.sentimentCounts = v; }

    public Map<String, Integer> getCautionLevelCounts()       { return cautionLevelCounts; }
    public void setCautionLevelCounts(Map<String, Integer> v) { this.cautionLevelCounts = v; }
}
