package kr.ac.kopo.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import kr.ac.kopo.common.vo.ApiResponse;
import kr.ac.kopo.member.service.MemberService;
import kr.ac.kopo.member.vo.MemberVO;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private MemberService memberService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<MemberVO>> login(
            @RequestBody MemberVO req, HttpSession session) {
        if (req.getLoginId() == null || req.getPassword() == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "loginId and password are required", null));
        }
        MemberVO member = memberService.login(req.getLoginId(), req.getPassword());
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "invalid loginId or password", null));
        }
        session.setAttribute("loginMember", member);
        return ResponseEntity.ok(new ApiResponse<>(true, "login success", member));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(new ApiResponse<>(true, "logout success", null));
    }
}
