package kr.ac.kopo.face.service;

import java.util.List;

import kr.ac.kopo.face.vo.FaceStatusVO;
import kr.ac.kopo.member.vo.MemberVO;

public interface FaceCredentialService {
    FaceStatusVO getStatus(int memberId);
    FaceStatusVO enroll(int memberId, List<String> images);
    boolean disable(int memberId);
    FacePythonClient.VerifyResult verify(MemberVO member, String image);
    FacePythonClient.VerifyResult verify(MemberVO member, List<String> images);
    FaceMatchResult identify(String image);
    FaceMatchResult identify(List<String> images);
}
