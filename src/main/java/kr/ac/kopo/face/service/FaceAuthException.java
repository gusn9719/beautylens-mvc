package kr.ac.kopo.face.service;

public class FaceAuthException extends RuntimeException {
    private final int status;

    public FaceAuthException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
