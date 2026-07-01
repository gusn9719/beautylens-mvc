package kr.ac.kopo.face.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.ac.kopo.face.dao.FaceCredentialDAO;
import kr.ac.kopo.face.vo.FaceCredentialVO;
import kr.ac.kopo.face.vo.FaceStatusVO;
import kr.ac.kopo.member.vo.MemberVO;

@Service
public class FaceCredentialServiceImpl implements FaceCredentialService {

    private static final double AMBIGUOUS_MARGIN = 0.04;

    @Autowired
    private FaceCredentialDAO faceCredentialDAO;

    @Autowired
    private FacePythonClient facePythonClient;

    @Override
    public FaceStatusVO getStatus(int memberId) {
        FaceCredentialVO credential = faceCredentialDAO.selectActiveByMemberId(memberId);
        if (credential == null) {
            return new FaceStatusVO(false, null, null, null);
        }
        return new FaceStatusVO(true, credential.getModelName(), credential.getCreatedAt(), credential.getUpdatedAt());
    }

    @Override
    public FaceStatusVO enroll(int memberId, List<String> images) {
        if (images == null || images.size() < 2) {
            throw new FaceAuthException(400, "at least two face images are required");
        }
        FacePythonClient.EnrollResult result = facePythonClient.enroll(memberId, images);
        FaceCredentialVO credential = new FaceCredentialVO();
        credential.setMemberId(memberId);
        credential.setFaceEmbedding(result.getEmbeddingJson());
        credential.setModelName(result.getModelName());
        faceCredentialDAO.upsert(credential);
        return getStatus(memberId);
    }

    @Override
    public boolean disable(int memberId) {
        return faceCredentialDAO.disableByMemberId(memberId) > 0;
    }

    @Override
    public FacePythonClient.VerifyResult verify(MemberVO member, String image) {
        return verify(member, List.of(image));
    }

    @Override
    public FacePythonClient.VerifyResult verify(MemberVO member, List<String> images) {
        if (images == null || images.isEmpty()) {
            throw new FaceAuthException(400, "face image is required");
        }
        FaceCredentialVO credential = faceCredentialDAO.selectActiveByMemberId(member.getMemberId());
        if (credential == null) {
            throw new FaceAuthException(404, "face credential is not registered");
        }
        CandidateScore score = scoreCandidate(images, credential);
        FacePythonClient.VerifyResult result = new FacePythonClient.VerifyResult();
        result.setVerified(score.verified);
        result.setSimilarity(score.similarity);
        result.setThreshold(score.threshold);
        result.setMessage(score.message);
        return result;
    }

    @Override
    public FaceMatchResult identify(String image) {
        return identify(List.of(image));
    }

    @Override
    public FaceMatchResult identify(List<String> images) {
        if (images == null || images.isEmpty()) {
            throw new FaceAuthException(400, "face image is required");
        }
        List<FaceCredentialVO> credentials = faceCredentialDAO.selectActiveAll();
        if (credentials == null || credentials.isEmpty()) {
            throw new FaceAuthException(404, "registered face credential not found");
        }

        FaceMatchResult best = new FaceMatchResult();
        best.setSimilarity(-1.0);
        best.setMessage("registered face credential not matched");
        double secondSimilarity = -1.0;

        for (FaceCredentialVO credential : credentials) {
            CandidateScore score = scoreCandidate(images, credential);
            if (score.similarity > best.getSimilarity()) {
                secondSimilarity = best.getSimilarity();
                best.setMemberId(credential.getMemberId());
                best.setVerified(score.verified);
                best.setSimilarity(score.similarity);
                best.setThreshold(score.threshold);
                best.setMessage(score.message);
            } else if (score.similarity > secondSimilarity) {
                secondSimilarity = score.similarity;
            }
        }
        if (best.isVerified()
                && secondSimilarity >= 0
                && best.getSimilarity() - secondSimilarity < AMBIGUOUS_MARGIN) {
            if (images.size() < 3) {
                best.setAmbiguous(true);
                best.setVerified(false);
                best.setMessage("ambiguous face match");
            } else {
                best.setAmbiguous(false);
                best.setVerified(false);
                best.setMemberId(null);
                best.setMessage("face match is still ambiguous after additional angles");
            }
        }
        return best;
    }

    private CandidateScore scoreCandidate(List<String> images, FaceCredentialVO credential) {
        double sum = 0.0;
        double threshold = 0.0;
        int count = 0;
        String message = "not verified";
        for (String image : images) {
            FacePythonClient.VerifyResult result = facePythonClient.verify(image, credential.getFaceEmbedding());
            if (result.getSimilarity() >= 0) {
                sum += result.getSimilarity();
                threshold = result.getThreshold();
                count++;
                message = result.getMessage();
            }
        }
        CandidateScore score = new CandidateScore();
        score.similarity = count == 0 ? -1.0 : sum / count;
        score.threshold = threshold;
        score.verified = score.similarity >= threshold;
        score.message = message;
        return score;
    }

    private static class CandidateScore {
        private double similarity;
        private double threshold;
        private boolean verified;
        private String message;
    }
}
