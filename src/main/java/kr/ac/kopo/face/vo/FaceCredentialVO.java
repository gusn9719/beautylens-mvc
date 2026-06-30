package kr.ac.kopo.face.vo;

public class FaceCredentialVO {
    private Integer faceId;
    private Integer memberId;
    private String faceEmbedding;
    private String modelName;
    private String faceEnabled;
    private String createdAt;
    private String updatedAt;

    public Integer getFaceId() { return faceId; }
    public void setFaceId(Integer faceId) { this.faceId = faceId; }

    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }

    public String getFaceEmbedding() { return faceEmbedding; }
    public void setFaceEmbedding(String faceEmbedding) { this.faceEmbedding = faceEmbedding; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public String getFaceEnabled() { return faceEnabled; }
    public void setFaceEnabled(String faceEnabled) { this.faceEnabled = faceEnabled; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
