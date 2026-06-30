package kr.ac.kopo.comment.vo;

public class CommentReportVO {
    private Integer reportId;
    private Integer commentId;
    private Integer reporterId;
    private String reporterNickname;
    private String reasonType;
    private String reasonText;
    private String status;
    private Integer handledBy;
    private String handledByNickname;
    private String handledAt;
    private String createdAt;
    private Integer productId;
    private String productName;
    private String displayName;
    private String commentContent;
    private String commentStatus;

    public Integer getReportId() { return reportId; }
    public void setReportId(Integer reportId) { this.reportId = reportId; }
    public Integer getCommentId() { return commentId; }
    public void setCommentId(Integer commentId) { this.commentId = commentId; }
    public Integer getReporterId() { return reporterId; }
    public void setReporterId(Integer reporterId) { this.reporterId = reporterId; }
    public String getReporterNickname() { return reporterNickname; }
    public void setReporterNickname(String reporterNickname) { this.reporterNickname = reporterNickname; }
    public String getReasonType() { return reasonType; }
    public void setReasonType(String reasonType) { this.reasonType = reasonType; }
    public String getReasonText() { return reasonText; }
    public void setReasonText(String reasonText) { this.reasonText = reasonText; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getHandledBy() { return handledBy; }
    public void setHandledBy(Integer handledBy) { this.handledBy = handledBy; }
    public String getHandledByNickname() { return handledByNickname; }
    public void setHandledByNickname(String handledByNickname) { this.handledByNickname = handledByNickname; }
    public String getHandledAt() { return handledAt; }
    public void setHandledAt(String handledAt) { this.handledAt = handledAt; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getCommentContent() { return commentContent; }
    public void setCommentContent(String commentContent) { this.commentContent = commentContent; }
    public String getCommentStatus() { return commentStatus; }
    public void setCommentStatus(String commentStatus) { this.commentStatus = commentStatus; }
}
