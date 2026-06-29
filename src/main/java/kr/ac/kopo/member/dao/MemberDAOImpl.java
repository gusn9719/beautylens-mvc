package kr.ac.kopo.member.dao;

import java.util.HashMap;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.ac.kopo.member.vo.MemberVO;

@Repository
public class MemberDAOImpl implements MemberDAO {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    @Override
    public int insert(MemberVO member) {
        return sqlSessionTemplate.insert("member.dao.MemberDAO.insert", member);
    }

    @Override
    public MemberVO selectByLoginId(String loginId) {
        return sqlSessionTemplate.selectOne("member.dao.MemberDAO.selectByLoginId", loginId);
    }

    @Override
    public MemberVO selectByLoginIdAndPassword(String loginId, String password) {
        Map<String, Object> param = new HashMap<>();
        param.put("loginId", loginId);
        param.put("password", password);
        return sqlSessionTemplate.selectOne("member.dao.MemberDAO.selectByLoginIdAndPassword", param);
    }

    @Override
    public MemberVO selectByMemberId(int memberId) {
        return sqlSessionTemplate.selectOne("member.dao.MemberDAO.selectByMemberId", memberId);
    }

    @Override
    public int updateProfile(MemberVO member) {
        return sqlSessionTemplate.update("member.dao.MemberDAO.updateProfile", member);
    }
}
