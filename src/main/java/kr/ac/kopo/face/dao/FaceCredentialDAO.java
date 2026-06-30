package kr.ac.kopo.face.dao;

import java.util.List;

import kr.ac.kopo.face.vo.FaceCredentialVO;

public interface FaceCredentialDAO {
    FaceCredentialVO selectActiveByMemberId(int memberId);
    List<FaceCredentialVO> selectActiveAll();
    int upsert(FaceCredentialVO credential);
    int disableByMemberId(int memberId);
}
