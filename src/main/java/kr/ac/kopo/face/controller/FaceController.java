package kr.ac.kopo.face.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import kr.ac.kopo.common.vo.ApiResponse;
import kr.ac.kopo.face.service.FaceAuthException;
import kr.ac.kopo.face.service.FaceCredentialService;
import kr.ac.kopo.face.service.FaceMatchResult;
import kr.ac.kopo.face.service.FacePythonClient;
import kr.ac.kopo.face.vo.FaceEnrollRequest;
import kr.ac.kopo.face.vo.FaceLoginRequest;
import kr.ac.kopo.face.vo.FaceStatusVO;
import kr.ac.kopo.member.service.MemberService;
import kr.ac.kopo.member.vo.MemberVO;

@RestController
public class FaceController {

    private static final double LOW_CONFIDENCE_MARGIN = 0.04;

    @Autowired
    private FaceCredentialService faceCredentialService;

    @Autowired
    private MemberService memberService;

    @GetMapping("/api/members/me/face")
    public ResponseEntity<ApiResponse<FaceStatusVO>> status(HttpSession session) {
        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "not logged in", null));
        }
        FaceStatusVO status = faceCredentialService.getStatus(loginMember.getMemberId());
        return ResponseEntity.ok(new ApiResponse<>(true, "face status found", status));
    }

    @PostMapping("/api/members/me/face")
    public ResponseEntity<ApiResponse<FaceStatusVO>> enroll(
            @RequestBody FaceEnrollRequest req,
            HttpSession session) {
        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "not logged in", null));
        }
        try {
            FaceStatusVO status = faceCredentialService.enroll(loginMember.getMemberId(), req.getImages());
            return ResponseEntity.ok(new ApiResponse<>(true, "face registered", status));
        } catch (FaceAuthException e) {
            return faceError(e);
        }
    }

    @DeleteMapping("/api/members/me/face")
    public ResponseEntity<ApiResponse<Void>> disable(HttpSession session) {
        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "not logged in", null));
        }
        faceCredentialService.disable(loginMember.getMemberId());
        return ResponseEntity.ok(new ApiResponse<>(true, "face registration disabled", null));
    }

    @PostMapping("/api/auth/face-login")
    public ResponseEntity<ApiResponse<MemberVO>> faceLogin(
            @RequestBody FaceLoginRequest req,
            HttpSession session) {
        List<String> images = loginImages(req);
        if (images.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "face image is required", null));
        }
        if (req.getLoginId() == null || req.getLoginId().isBlank()) {
            return identifyFaceLogin(images, session);
        }
        MemberVO member = memberService.findByLoginId(req.getLoginId().trim());
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "face login failed", null));
        }
        try {
            FacePythonClient.VerifyResult result = faceCredentialService.verify(member, images);
            if (needsAdditionalAngles(images, result)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponse<>(false,
                                "비슷한 얼굴 후보가 있어 왼쪽과 오른쪽 얼굴을 추가로 확인합니다.", null));
            }
            if (!result.isVerified()) {
                String message = faceFailureMessage(result);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, message, null));
            }
            session.setAttribute("loginMember", member);
            return ResponseEntity.ok(new ApiResponse<>(true, "face login success", member));
        } catch (FaceAuthException e) {
            if (e.getStatus() == 404) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, e.getMessage(), null));
            }
            return faceError(e);
        }
    }

    private ResponseEntity<ApiResponse<MemberVO>> identifyFaceLogin(List<String> images, HttpSession session) {
        try {
            FaceMatchResult result = faceCredentialService.identify(images);
            if (result.isAmbiguous()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponse<>(false,
                                "얼굴이 여러 계정과 비슷합니다. 왼쪽과 오른쪽 얼굴을 추가로 촬영해 주세요.", null));
            }
            if (!result.isVerified()) {
                FacePythonClient.VerifyResult verifyResult = new FacePythonClient.VerifyResult();
                verifyResult.setMessage(result.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, faceFailureMessage(verifyResult), null));
            }
            MemberVO member = memberService.getMember(result.getMemberId());
            if (member == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "face login failed", null));
            }
            session.setAttribute("loginMember", member);
            return ResponseEntity.ok(new ApiResponse<>(true, "face login success", member));
        } catch (FaceAuthException e) {
            if (e.getStatus() == 404) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "등록된 얼굴 정보가 없습니다.", null));
            }
            return faceError(e);
        }
    }

    private List<String> loginImages(FaceLoginRequest req) {
        if (req.getImages() != null && !req.getImages().isEmpty()) {
            return req.getImages().stream()
                    .filter(v -> v != null && !v.isBlank())
                    .toList();
        }
        if (req.getImage() != null && !req.getImage().isBlank()) {
            return List.of(req.getImage());
        }
        return List.of();
    }

    private boolean needsAdditionalAngles(List<String> images, FacePythonClient.VerifyResult result) {
        if (images == null || images.size() >= 3 || result == null || !result.isVerified()) {
            return false;
        }
        return result.getThreshold() > 0
                && result.getSimilarity() > 0
                && result.getSimilarity() - result.getThreshold() < LOW_CONFIDENCE_MARGIN;
    }

    private <T> ResponseEntity<ApiResponse<T>> faceError(FaceAuthException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (e.getStatus() == 400) status = HttpStatus.BAD_REQUEST;
        else if (e.getStatus() == 401) status = HttpStatus.UNAUTHORIZED;
        else if (e.getStatus() == 403) status = HttpStatus.FORBIDDEN;
        else if (e.getStatus() == 404) status = HttpStatus.NOT_FOUND;
        else if (e.getStatus() == 503) status = HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(false, e.getMessage(), null));
    }

    private String faceFailureMessage(FacePythonClient.VerifyResult result) {
        String message = result.getMessage();
        if (message == null) {
            return "등록된 얼굴과 일치하지 않습니다. 다른 얼굴로 인식되었습니다.";
        }
        String lower = message.toLowerCase();
        if (lower.contains("one face") || lower.contains("face") && lower.contains("required")) {
            return "얼굴을 인식하지 못했습니다. 밝은 곳에서 정면으로 다시 촬영해 주세요.";
        }
        if (lower.contains("not verified")) {
            return "등록된 얼굴과 일치하지 않습니다. 다른 얼굴로 인식되었습니다.";
        }
        if (lower.contains("ambiguous")) {
            return "얼굴을 확실히 확인하지 못했습니다. 아이디를 입력하고 다시 시도하거나 비밀번호로 로그인해 주세요.";
        }
        return "얼굴 인증에 실패했습니다. 다시 촬영해 주세요.";
    }
}
