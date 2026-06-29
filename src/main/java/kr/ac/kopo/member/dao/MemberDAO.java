package kr.ac.kopo.member.dao;

import kr.ac.kopo.member.vo.MemberVO;

public interface MemberDAO {
    int insert(MemberVO member);
    MemberVO selectByLoginId(String loginId);
    MemberVO selectByLoginIdAndPassword(String loginId, String password);
    MemberVO selectByMemberId(int memberId);
    int updateProfile(MemberVO member);
}
