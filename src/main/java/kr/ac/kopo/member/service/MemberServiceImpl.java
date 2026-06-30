package kr.ac.kopo.member.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.ac.kopo.member.dao.MemberDAO;
import kr.ac.kopo.member.vo.MemberVO;

@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    private MemberDAO memberDAO;

    private String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    @Override
    public boolean register(MemberVO member) {
        if (memberDAO.selectByLoginId(member.getLoginId()) != null) {
            return false;
        }
        member.setPassword(sha256(member.getPassword()));
        return memberDAO.insert(member) > 0;
    }

    @Override
    public MemberVO login(String loginId, String password) {
        return memberDAO.selectByLoginIdAndPassword(loginId, sha256(password));
    }

    @Override
    public MemberVO findByLoginId(String loginId) {
        return memberDAO.selectByLoginId(loginId);
    }

    @Override
    public MemberVO getMember(int memberId) {
        return memberDAO.selectByMemberId(memberId);
    }

    @Override
    public boolean updateProfile(MemberVO member) {
        return memberDAO.updateProfile(member) > 0;
    }
}
