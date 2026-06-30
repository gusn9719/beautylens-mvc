package kr.ac.kopo.comment.vo;

public class CommentVO {

    private Integer commentId;
    private Integer productId;
    private Integer memberId;
    private String  nickname;
    private String  productName;
    private String  displayName;
    private String  brand;
    private String  imageUrl;
    private String  content;
    private String  status;
    private String  createdAt;
    private String  updatedAt;
    private String  deletedAt;
    private Integer deletedBy;
    private String  deleteReason;

    public CommentVO() {}

    public Integer getCommentId()              { return commentId; }
    public void setCommentId(Integer v)        { this.commentId = v; }

    public Integer getProductId()              { return productId; }
    public void setProductId(Integer v)        { this.productId = v; }

    public Integer getMemberId()               { return memberId; }
    public void setMemberId(Integer v)         { this.memberId = v; }

    public String getNickname()                { return nickname; }
    public void setNickname(String v)          { this.nickname = v; }

    public String getProductName()             { return productName; }
    public void setProductName(String v)       { this.productName = v; }

    public String getDisplayName()             { return displayName; }
    public void setDisplayName(String v)       { this.displayName = v; }

    public String getBrand()                   { return brand; }
    public void setBrand(String v)             { this.brand = v; }

    public String getImageUrl()                { return imageUrl; }
    public void setImageUrl(String v)          { this.imageUrl = v; }

    public String getContent()                 { return content; }
    public void setContent(String v)           { this.content = v; }

    public String getStatus()                  { return status; }
    public void setStatus(String v)            { this.status = v; }

    public String getCreatedAt()               { return createdAt; }
    public void setCreatedAt(String v)         { this.createdAt = v; }

    public String getUpdatedAt()               { return updatedAt; }
    public void setUpdatedAt(String v)         { this.updatedAt = v; }

    public String getDeletedAt()               { return deletedAt; }
    public void setDeletedAt(String v)         { this.deletedAt = v; }

    public Integer getDeletedBy()              { return deletedBy; }
    public void setDeletedBy(Integer v)        { this.deletedBy = v; }

    public String getDeleteReason()            { return deleteReason; }
    public void setDeleteReason(String v)      { this.deleteReason = v; }
}
