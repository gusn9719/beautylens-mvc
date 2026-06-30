package kr.ac.kopo.face.vo;

public class FaceStatusVO {
    private boolean registered;
    private String modelName;
    private String createdAt;
    private String updatedAt;

    public FaceStatusVO() {}

    public FaceStatusVO(boolean registered, String modelName, String createdAt, String updatedAt) {
        this.registered = registered;
        this.modelName = modelName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public boolean isRegistered() { return registered; }
    public void setRegistered(boolean registered) { this.registered = registered; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
