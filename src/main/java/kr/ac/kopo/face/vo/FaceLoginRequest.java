package kr.ac.kopo.face.vo;

import java.util.List;

public class FaceLoginRequest {
    private String loginId;
    private String image;
    private List<String> images;

    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
}
