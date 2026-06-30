package kr.ac.kopo.face.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.ac.kopo.face.vo.FaceCredentialVO;

@Repository
public class FaceCredentialDAOImpl implements FaceCredentialDAO {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    @Override
    public FaceCredentialVO selectActiveByMemberId(int memberId) {
        return sqlSessionTemplate.selectOne("face.dao.FaceCredentialDAO.selectActiveByMemberId", memberId);
    }

    @Override
    public List<FaceCredentialVO> selectActiveAll() {
        return sqlSessionTemplate.selectList("face.dao.FaceCredentialDAO.selectActiveAll");
    }

    @Override
    public int upsert(FaceCredentialVO credential) {
        return sqlSessionTemplate.update("face.dao.FaceCredentialDAO.upsert", credential);
    }

    @Override
    public int disableByMemberId(int memberId) {
        return sqlSessionTemplate.update("face.dao.FaceCredentialDAO.disableByMemberId", memberId);
    }
}
