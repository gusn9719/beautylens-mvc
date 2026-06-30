package kr.ac.kopo.member.service;

import kr.ac.kopo.member.vo.MemberVO;

public interface MemberService {
    boolean register(MemberVO member);
    MemberVO login(String loginId, String password);
    MemberVO findByLoginId(String loginId);
    MemberVO getMember(int memberId);
    boolean updateProfile(MemberVO member);
}
