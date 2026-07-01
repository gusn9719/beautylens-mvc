<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<main class="page page-narrow">
  <section class="card form-card">
    <h1>회원가입</h1>
    <p class="muted">피부 타입과 관심사를 입력하면 추천 화면에서 바로 사용할 수 있습니다.</p>
    <div id="signup-notice"></div>
    <form id="signup-form">
      <div class="field">
        <label for="loginId">아이디</label>
        <input id="loginId" required>
      </div>
      <div class="field">
        <label for="password">비밀번호</label>
        <input id="password" type="password" required>
      </div>
      <div class="field">
        <label for="nickname">닉네임</label>
        <input id="nickname" required>
      </div>
      <div class="field">
        <label for="skinType">피부 타입</label>
        <select id="skinType" required>
          <option value="dry">건성</option>
          <option value="oily">지성</option>
          <option value="combination">복합성</option>
          <option value="sensitive">민감성</option>
          <option value="normal">중성</option>
        </select>
      </div>
      <div class="field">
        <label>피부 관심사</label>
        <p class="field-help">추천 이유를 만들 때 상품 태그와 비교합니다. 가장 신경 쓰는 항목을 2~4개만 선택해 주세요.</p>
        <div class="choice-grid" data-skin-concern-group>
          <label><input type="checkbox" value="보습"> 보습</label>
          <label><input type="checkbox" value="진정"> 진정</label>
          <label><input type="checkbox" value="트러블"> 트러블</label>
          <label><input type="checkbox" value="여드름"> 여드름</label>
          <label><input type="checkbox" value="건조"> 건조</label>
          <label><input type="checkbox" value="민감"> 민감</label>
          <label><input type="checkbox" value="홍조"> 홍조</label>
          <label><input type="checkbox" value="모공"> 모공</label>
          <label><input type="checkbox" value="피지"> 피지</label>
          <label><input type="checkbox" value="각질"> 각질</label>
          <label><input type="checkbox" value="미백"> 미백</label>
          <label><input type="checkbox" value="장벽"> 장벽</label>
        </div>
        <input id="skinConcern" type="hidden">
        <p id="skinConcernHelp" class="field-help">선택한 관심사는 추천 카드의 추천 이유에 반영됩니다.</p>
      </div>
      <button class="btn btn-primary" type="submit">가입하기</button>
      <a class="btn btn-ghost" href="<%= request.getContextPath() %>/login">로그인으로 이동</a>
    </form>
  </section>
</main>

<script>
document.addEventListener("DOMContentLoaded", function () {
  const concernGroup = document.querySelector("[data-skin-concern-group]");
  const concernInput = document.getElementById("skinConcern");
  const concernHelp = document.getElementById("skinConcernHelp");

  function selectedConcerns() {
    return Array.from(concernGroup.querySelectorAll("input:checked")).map(function (input) {
      return input.value;
    });
  }

  function syncConcerns() {
    const selected = selectedConcerns();
    concernInput.value = selected.join(" ");
    concernHelp.textContent = selected.length
      ? "선택됨: " + selected.join(", ") + " · 추천 이유에 이 키워드가 반영됩니다."
      : "선택한 관심사는 추천 카드의 추천 이유에 반영됩니다.";
    concernGroup.querySelectorAll("input:not(:checked)").forEach(function (input) {
      input.disabled = selected.length >= 4;
    });
  }

  concernGroup.addEventListener("change", function (event) {
    const selected = selectedConcerns();
    if (selected.length > 4) {
      event.target.checked = false;
      BL.setNotice("signup-notice", "피부 관심사는 2~4개 정도만 선택해 주세요.", "error");
    }
    syncConcerns();
  });
  syncConcerns();

  document.getElementById("signup-form").addEventListener("submit", async function (event) {
    event.preventDefault();
    BL.setNotice("signup-notice", "");
    const selected = selectedConcerns();
    if (selected.length < 2 || selected.length > 4) {
      BL.setNotice("signup-notice", "피부 관심사는 2~4개를 선택해 주세요.", "error");
      return;
    }
    const body = {
      loginId: document.getElementById("loginId").value.trim(),
      password: document.getElementById("password").value,
      nickname: document.getElementById("nickname").value.trim(),
      skinType: document.getElementById("skinType").value,
      skinConcern: document.getElementById("skinConcern").value.trim()
    };
    try {
      await BL.post("/api/members", body);
      BL.setNotice("signup-notice", "회원가입이 완료되었습니다. 로그인 화면으로 이동합니다.", "success");
      setTimeout(function () { window.location.href = BL.url("/login"); }, 800);
    } catch (e) {
      BL.setNotice("signup-notice", e.status === 409 ? "이미 사용 중인 아이디입니다." : "회원가입에 실패했습니다.", "error");
    }
  });
});
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
