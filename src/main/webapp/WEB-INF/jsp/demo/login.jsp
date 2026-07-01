<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<main class="page page-narrow">
  <section class="card form-card">
    <h1>로그인</h1>
    <p class="muted">계정으로 로그인해 주세요.</p>
    <div id="login-notice"></div>
    <form id="login-form">
      <div class="field">
        <label for="loginId">아이디</label>
        <input id="loginId" name="loginId" autocomplete="username" required>
      </div>
      <div class="field">
        <label for="password">비밀번호</label>
        <input id="password" name="password" type="password" autocomplete="current-password" required>
      </div>
      <button class="btn btn-primary" type="submit">로그인</button>
      <button id="face-login-btn" class="btn btn-ghost" type="button">얼굴로 로그인</button>
      <a class="btn btn-ghost" href="<%= request.getContextPath() %>/signup">회원가입</a>
    </form>
    <p class="muted" style="margin-top:14px;">얼굴 로그인이 원활하지 않으면 비밀번호로 로그인할 수 있습니다.</p>
  </section>
</main>

<script>
document.addEventListener("DOMContentLoaded", function () {
  function loginRedirect(member) {
    const params = new URLSearchParams(window.location.search);
    const redirect = BL.safeRedirectPath(params.get("redirect"), "");
    if (redirect) return BL.url(redirect);
    return BL.url(member && member.role === "ADMIN" ? "/admin" : "/recommend");
  }

  document.getElementById("login-form").addEventListener("submit", async function (event) {
    event.preventDefault();
    BL.setNotice("login-notice", "");
    const body = {
      loginId: document.getElementById("loginId").value.trim(),
      password: document.getElementById("password").value
    };
    try {
      const res = await BL.post("/api/auth/login", body);
      const member = res.data;
      window.location.href = loginRedirect(member);
    } catch (e) {
      BL.setNotice("login-notice", "로그인에 실패했습니다. 아이디와 비밀번호를 확인하세요.", "error");
    }
  });

  const faceLoginButton = document.getElementById("face-login-btn");
  let activeFaceLogin = false;

  faceLoginButton.addEventListener("click", async function () {
    if (activeFaceLogin) return;
    activeFaceLogin = true;
    faceLoginButton.disabled = true;
    const originalFaceLoginText = faceLoginButton.textContent;
    faceLoginButton.textContent = "얼굴 확인 중";
    BL.setNotice("login-notice", "");
    const loginId = document.getElementById("loginId").value.trim();
    async function submitFaceLogin(images, signal) {
      const payload = {
        loginId: loginId,
        image: images[0],
        images: images
      };
      const response = await fetch(BL.url("/api/auth/face-login"), {
        method: "POST",
        headers: { "Content-Type": "application/json; charset=utf-8" },
        credentials: "same-origin",
        body: JSON.stringify(payload),
        signal: signal
      });
      const result = await response.json().catch(function () {
        return { success: false, message: "얼굴 로그인 기능을 잠시 사용할 수 없습니다.", data: null };
      });
      if (!response.ok || result.success === false) {
        const error = new Error(result.message || "얼굴 로그인에 실패했습니다.");
        error.status = response.status;
        error.payload = result;
        throw error;
      }
      return result;
    }
    let faceSession = null;
    let loginAbort = null;
    let redirectPending = false;
    try {
      faceSession = await BL.faceCaptureLogin();
      const frontImage = faceSession.image;
      let res = null;
      try {
        loginAbort = new AbortController();
        faceSession.onCancel(function () {
          loginAbort.abort();
        });
        faceSession.setStatus(
          "로그인 정보 확인 중입니다.",
          "얼굴 정보와 계정 정보를 확인하고 있습니다. 잠시만 기다려 주세요.",
          88,
          "로그인 정보 확인 중",
          true
        );
        res = await submitFaceLogin([frontImage], loginAbort.signal);
        if (faceSession.isCanceled()) return;
      } catch (e) {
        if (e.name === "AbortError" || (faceSession && faceSession.isCanceled())) {
          BL.setNotice("login-notice", "얼굴 로그인이 취소되었습니다.", "");
          return;
        }
        if (e.status !== 409) throw e;
        faceSession.setStatus(
          "추가 확인이 필요합니다.",
          "비슷한 얼굴 후보가 있어 왼쪽과 오른쪽 얼굴을 추가로 확인합니다.",
          92,
          "추가 확인 필요",
          false
        );
        BL.setNotice("login-notice", "비슷한 얼굴 후보가 있어 왼쪽과 오른쪽 얼굴을 추가로 확인합니다.", "");
        faceSession.close();
        faceSession = null;
        const extraImages = await BL.faceCaptureSequence([
          "왼쪽을 살짝 바라봐 주세요.",
          "오른쪽을 살짝 바라봐 주세요."
        ], "추가 얼굴 확인");
        BL.setNotice("login-notice", "추가 얼굴 정보를 확인하고 있습니다. 잠시만 기다려 주세요.", "");
        res = await submitFaceLogin([frontImage].concat(extraImages));
      }
      const member = res.data;
      if (faceSession && faceSession.isCanceled()) return;
      if (faceSession) {
        faceSession.complete();
        redirectPending = true;
        window.setTimeout(function () {
          if (faceSession.isCanceled()) return;
          faceSession.close();
          window.location.href = loginRedirect(member);
        }, 650);
      } else {
        BL.setNotice("login-notice", "얼굴 확인이 완료되었습니다. 로그인 중입니다.", "success");
        redirectPending = true;
        window.setTimeout(function () {
          window.location.href = loginRedirect(member);
        }, 450);
      }
    } catch (e) {
      if (e.name === "AbortError" || (faceSession && faceSession.isCanceled())) {
        BL.setNotice("login-notice", "얼굴 로그인이 취소되었습니다.", "");
        return;
      }
      const serverMessage = e.payload && e.payload.message ? e.payload.message : "";
      const message = !e.status && e.message
        ? e.message
        : e.status === 404
        ? "등록된 얼굴 정보가 없습니다. 비밀번호로 로그인한 뒤 마이페이지에서 얼굴을 등록해 주세요."
        : e.status === 401
          ? (serverMessage || "등록된 얼굴과 일치하지 않습니다. 다른 얼굴로 인식되었습니다.")
          : e.status === 400
          ? (serverMessage || "얼굴을 인식하지 못했습니다. 밝은 곳에서 정면으로 다시 촬영해 주세요.")
          : "얼굴 로그인 기능을 잠시 사용할 수 없습니다. 다시 시도하거나 비밀번호로 로그인해 주세요.";
      if (faceSession && faceSession.fail) {
        faceSession.fail(message);
      }
      BL.setNotice("login-notice", message, "error");
    } finally {
      if (!redirectPending) {
        activeFaceLogin = false;
        faceLoginButton.disabled = false;
        faceLoginButton.textContent = originalFaceLoginText;
      }
    }
  });
});
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
