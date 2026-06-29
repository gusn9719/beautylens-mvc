package kr.ac.kopo.review.vo;

public class ReviewVO {
    private Integer reviewId;
    private Integer productId;
    private String  platformReviewId;
    private Double  rating;
    private String  reviewText;
    private String  reviewDate;
    private String  reviewerSkinType;
    private String  reviewerSkinConcern;
    private String  sentimentLabel;
    private Integer sentimentId;
    private String  regDate;

    public ReviewVO() {}

    public Integer getReviewId() { return reviewId; }
    public void setReviewId(Integer reviewId) { this.reviewId = reviewId; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getPlatformReviewId() { return platformReviewId; }
    public void setPlatformReviewId(String platformReviewId) { this.platformReviewId = platformReviewId; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public String getReviewDate() { return reviewDate; }
    public void setReviewDate(String reviewDate) { this.reviewDate = reviewDate; }

    public String getReviewerSkinType() { return reviewerSkinType; }
    public void setReviewerSkinType(String reviewerSkinType) { this.reviewerSkinType = reviewerSkinType; }

    public String getReviewerSkinConcern() { return reviewerSkinConcern; }
    public void setReviewerSkinConcern(String reviewerSkinConcern) { this.reviewerSkinConcern = reviewerSkinConcern; }

    public String getSentimentLabel() { return sentimentLabel; }
    public void setSentimentLabel(String sentimentLabel) { this.sentimentLabel = sentimentLabel; }

    public Integer getSentimentId() { return sentimentId; }
    public void setSentimentId(Integer sentimentId) { this.sentimentId = sentimentId; }

    public String getRegDate() { return regDate; }
    public void setRegDate(String regDate) { this.regDate = regDate; }

    @Override
    public String toString() {
        return "ReviewVO [reviewId=" + reviewId + ", productId=" + productId
                + ", sentimentLabel=" + sentimentLabel + ", rating=" + rating + "]";
    }
}
