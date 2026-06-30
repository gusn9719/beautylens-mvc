package kr.ac.kopo.face.service;

public class FaceMatchResult {
    private Integer memberId;
    private boolean verified;
    private double similarity;
    private double threshold;
    private String message;
    private boolean ambiguous;

    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public double getSimilarity() { return similarity; }
    public void setSimilarity(double similarity) { this.similarity = similarity; }

    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isAmbiguous() { return ambiguous; }
    public void setAmbiguous(boolean ambiguous) { this.ambiguous = ambiguous; }
}
