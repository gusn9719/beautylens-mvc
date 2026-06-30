package kr.ac.kopo.member.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import kr.ac.kopo.comment.service.CommentService;
import kr.ac.kopo.comment.vo.CommentVO;
import kr.ac.kopo.common.vo.ApiResponse;
import kr.ac.kopo.member.service.MemberService;
import kr.ac.kopo.member.vo.MemberVO;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody MemberVO member) {
        if (member.getLoginId() == null || member.getLoginId().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "loginId is required", null));
        }
        if (member.getPassword() == null || member.getPassword().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "password is required", null));
        }
        boolean ok = memberService.register(member);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, "loginId already exists", null));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "member registered", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberVO>> me(HttpSession session) {
        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "not logged in", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "member found", loginMember));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<MemberVO>> update(
            @RequestBody MemberVO req, HttpSession session) {
        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "not logged in", null));
        }
        req.setMemberId(loginMember.getMemberId());
        memberService.updateProfile(req);

        MemberVO updated = memberService.getMember(loginMember.getMemberId());
        session.setAttribute("loginMember", updated);
        return ResponseEntity.ok(new ApiResponse<>(true, "profile updated", updated));
    }

    @GetMapping("/me/comments")
    public ResponseEntity<ApiResponse<List<CommentVO>>> myComments(HttpSession session) {
        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "not logged in", null));
        }
        List<CommentVO> list = commentService.getMyComments(loginMember.getMemberId());
        return ResponseEntity.ok(new ApiResponse<>(true, "my comments found", list));
    }
}
