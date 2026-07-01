<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<main class="page page-shell">
  <section class="section-head">
    <div>
      <p class="eyebrow">My BeautyLens</p>
      <h1>마이페이지</h1>
      <p class="muted">프로필, 피부 타입, 얼굴 로그인, 상품 활동을 한곳에서 관리합니다.</p>
    </div>
    <a class="btn btn-primary" href="<%= request.getContextPath() %>/recommend">추천 받기</a>
  </section>

  <div id="mypage-gate" class="loading">회원 정보를 확인하는 중입니다.</div>

  <div id="mypage-layout" class="mypage-layout" style="display:none;">
    <aside class="mypage-sidebar" aria-label="마이페이지 메뉴">
      <button class="mypage-nav-item is-active" type="button" data-tab-target="profile">내 프로필</button>
      <button class="mypage-nav-item" type="button" data-tab-target="skin">피부 타입</button>
      <button class="mypage-nav-item" type="button" data-tab-target="face">얼굴 로그인</button>
      <button class="mypage-nav-item" type="button" data-tab-target="favorites">찜한 상품</button>
      <button class="mypage-nav-item" type="button" data-tab-target="ratings">내가 평가한 상품</button>
      <button class="mypage-nav-item" type="button" data-tab-target="recent">최근 본 상품</button>
      <button class="mypage-nav-item" type="button" data-tab-target="comments">내가 남긴 의견</button>
      <button class="mypage-nav-item" type="button" data-tab-target="feedback">추천 피드백</button>
      <button class="mypage-nav-item" type="button" data-tab-target="account">계정 설정</button>
    </aside>

    <div class="mypage-content">
      <section class="tab-panel active" data-tab-panel="profile">
        <div class="section-card">
          <div class="section-head">
            <div>
              <h2>내 프로필</h2>
              <p class="muted">계정 정보와 BeautyLens 활동 요약입니다.</p>
            </div>
          </div>
          <div class="admin-card-grid">
            <article class="card card-pad stat">
              <div class="muted">회원</div>
              <div id="profile-member-name" class="value">-</div>
            </article>
            <article class="card card-pad stat">
              <div class="muted">피부 타입</div>
              <div id="profile-skin-type" class="value">-</div>
            </article>
            <article class="card card-pad stat">
              <div class="muted">얼굴 로그인</div>
              <div id="profile-face-status" class="value">확인 중</div>
            </article>
            <article class="card card-pad stat">
              <div class="muted">최근 본 상품</div>
              <div id="profile-recent-count" class="value">0</div>
            </article>
            <article class="card card-pad stat">
              <div class="muted">찜한 상품</div>
              <div id="profile-favorite-count" class="value">0</div>
            </article>
            <article class="card card-pad stat">
              <div class="muted">작성한 의견</div>
              <div id="profile-comment-count" class="value">0</div>
            </article>
          </div>
        </div>
      </section>

      <section class="tab-panel" data-tab-panel="skin">
        <div class="section-card">
          <h2>피부 타입 설정</h2>
          <p class="muted">추천 기준에 사용하는 기본 피부 정보를 관리합니다.</p>
          <form id="profile-form" style="display:none;">
            <div id="mypage-notice"></div>
            <div class="field">
              <label>아이디</label>
              <input id="loginId" disabled>
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
            <div class="actions">
              <button class="btn btn-primary" type="submit">수정 저장</button>
              <a class="btn btn-ghost" href="<%= request.getContextPath() %>/recommend">추천 페이지로 이동</a>
            </div>
          </form>
        </div>
      </section>

      <section id="face-section" class="tab-panel" data-tab-panel="face">
        <div class="section-card">
          <div class="section-head">
            <div>
              <h2>얼굴 로그인 설정</h2>
              <p class="muted">얼굴 원본 사진은 저장하지 않고, 로그인 확인에 필요한 얼굴 정보만 안전하게 보관합니다.</p>
            </div>
          </div>
          <div id="face-notice"></div>
          <div id="face-status" class="face-status">
            <span class="badge">상태 확인 중</span>
          </div>
          <div class="face-step-list" aria-label="얼굴 등록 단계">
            <span>안내</span>
            <span>정면</span>
            <span>왼쪽</span>
            <span>오른쪽</span>
            <span>위쪽</span>
            <span>아래쪽</span>
            <span>완료</span>
          </div>
          <p class="muted">각 단계에서 얼굴을 화면 중앙에 맞춘 뒤 촬영합니다. 카메라 권한이 필요하며, 실패하면 다시 시도할 수 있습니다.</p>
          <div class="actions">
            <button id="face-enroll-btn" class="btn btn-primary" type="button">얼굴 등록하기</button>
            <button id="face-disable-btn" class="btn btn-danger" type="button" style="display:none;">얼굴 등록 해제</button>
          </div>
        </div>
      </section>

      <section id="my-favorites-section" class="tab-panel" data-tab-panel="favorites">
        <div class="section-card">
          <div class="section-head">
            <div>
              <h2>찜한 상품</h2>
              <p class="muted">BeautyLens 안에서 저장한 관심 상품입니다.</p>
            </div>
          </div>
          <div id="my-favorites-notice"></div>
          <div id="my-favorites" class="my-comment-list"></div>
        </div>
      </section>

      <section id="my-ratings-section" class="tab-panel" data-tab-panel="ratings">
        <div class="section-card">
          <div class="section-head">
            <div>
              <h2>내가 평가한 상품</h2>
              <p class="muted">크롤링 리뷰와 별도로 저장된 사이트 내부 평가입니다.</p>
            </div>
          </div>
          <div id="my-ratings" class="my-comment-list"></div>
        </div>
      </section>

      <section id="recent-products-section" class="tab-panel" data-tab-panel="recent">
        <div class="section-card">
          <div class="section-head">
            <div>
              <h2>최근 본 상품</h2>
              <p class="muted">로그인 상태에서 열어 본 상품 상세 기록입니다.</p>
            </div>
          </div>
          <div id="recent-products" class="grid product-grid"></div>
        </div>
      </section>

      <section id="my-comments-section" class="tab-panel" data-tab-panel="comments">
        <div class="section-card">
          <div class="section-head">
            <div>
              <h2>내가 남긴 의견</h2>
              <p class="muted">최근 작성한 상품 의견을 확인하고 상품 상세로 이동할 수 있습니다.</p>
            </div>
          </div>
          <div id="my-comments-notice"></div>
          <div id="my-comments" class="my-comment-list">
            <div class="loading">내 의견을 불러오는 중입니다.</div>
          </div>
        </div>
      </section>

      <section id="my-feedback-section" class="tab-panel" data-tab-panel="feedback">
        <div class="section-card">
          <div class="section-head">
            <div>
              <h2>추천 피드백 내역</h2>
              <p class="muted">추천 결과에서 남긴 피드백을 확인합니다.</p>
            </div>
          </div>
          <div id="my-feedback" class="my-comment-list"></div>
        </div>
      </section>

      <section class="tab-panel" data-tab-panel="account">
        <div class="section-card">
          <h2>계정 설정</h2>
          <p class="muted">비밀번호 로그인은 그대로 유지됩니다. 얼굴 로그인 설정은 왼쪽 메뉴의 얼굴 로그인에서 관리할 수 있습니다.</p>
          <div class="empty-state">추가 계정 설정은 다음 단계에서 정리할 예정입니다.</div>
        </div>
      </section>
    </div>
  </div>
</main>

<script>
document.addEventListener("DOMContentLoaded", async function () {
  const gate = document.getElementById("mypage-gate");
  const mypageLayout = document.getElementById("mypage-layout");
  const form = document.getElementById("profile-form");
  const faceSection = document.getElementById("face-section");
  const faceStatus = document.getElementById("face-status");
  const faceEnrollBtn = document.getElementById("face-enroll-btn");
  const faceDisableBtn = document.getElementById("face-disable-btn");
  const commentsSection = document.getElementById("my-comments-section");
  const commentsTarget = document.getElementById("my-comments");
  const favoritesSection = document.getElementById("my-favorites-section");
  const favoritesTarget = document.getElementById("my-favorites");
  const ratingsSection = document.getElementById("my-ratings-section");
  const ratingsTarget = document.getElementById("my-ratings");
  const recentSection = document.getElementById("recent-products-section");
  const recentTarget = document.getElementById("recent-products");
  const feedbackSection = document.getElementById("my-feedback-section");
  const feedbackTarget = document.getElementById("my-feedback");
  const concernGroup = document.querySelector("[data-skin-concern-group]");
  const concernInput = document.getElementById("skinConcern");
  const concernHelp = document.getElementById("skinConcernHelp");
  const allowedConcerns = ["보습", "진정", "트러블", "여드름", "건조", "민감", "홍조", "모공", "피지", "각질", "미백", "장벽"];
  let hasUnmappedConcern = false;
  let me = null;

  function updateCount(id, count) {
    const el = document.getElementById(id);
    if (el) el.textContent = String(count || 0);
  }

  function updateText(id, value) {
    const el = document.getElementById(id);
    if (el) el.textContent = value || "-";
  }

  function statusBadge(status) {
    const deleted = status === "DELETED";
    return `<span class="badge ${deleted ? "badge-deleted" : "badge-active"}">${deleted ? "삭제됨" : "게시중"}</span>`;
  }

  function ratingChoiceLabel(value, yesText, noText) {
    if (value === "Y") return yesText;
    if (value === "N") return noText;
    return "선택 안 함";
  }

  function selectedConcerns() {
    return Array.from(concernGroup.querySelectorAll("input:checked")).map(function (input) {
      return input.value;
    });
  }

  function syncConcerns() {
    const selected = selectedConcerns();
    concernInput.value = selected.join(" ");
    if (selected.length) {
      concernHelp.textContent = "선택됨: " + selected.join(", ") + " · 상품 태그와 맞으면 추천 이유에 표시됩니다.";
    } else if (hasUnmappedConcern) {
      concernHelp.textContent = "기존 피부 관심사가 현재 선택 항목과 맞지 않습니다. 2~4개를 다시 선택해 주세요.";
    } else {
      concernHelp.textContent = "선택한 관심사는 상품 태그와 맞을 때 추천 이유에 표시됩니다.";
    }
    concernGroup.querySelectorAll("input:not(:checked)").forEach(function (input) {
      input.disabled = selected.length >= 4;
    });
  }

  function applyConcernValue(value) {
    const saved = String(value || "").split(/[,，\s]+/).filter(Boolean);
    hasUnmappedConcern = saved.length > 0 && saved.every(function (item) {
      return allowedConcerns.indexOf(item) < 0;
    });
    concernGroup.querySelectorAll("input").forEach(function (input) {
      input.checked = saved.indexOf(input.value) >= 0;
    });
    syncConcerns();
  }

  function commentCard(comment) {
    const title = BL.escape(comment.displayName || comment.productName || "상품");
    const brand = comment.brand ? `<span>${BL.escape(comment.brand)}</span>` : "";
    const deletedAt = comment.deletedAt ? `<span>삭제일 ${BL.escape(comment.deletedAt)}</span>` : "";
    const updatedAt = comment.updatedAt ? `<span>수정됨 ${BL.escape(comment.updatedAt)}</span>` : "";
    const deleteButton = comment.status === "ACTIVE"
      ? `<button class="btn btn-danger" type="button" data-delete-comment="${comment.commentId}">삭제</button>`
      : "";
    const editButton = comment.status === "ACTIVE"
      ? `<button class="btn btn-ghost" type="button" data-edit-my-comment="${comment.commentId}">수정</button>`
      : "";
    return `
      <article class="card my-comment-card">
        <a class="my-comment-thumb" href="${BL.url("/products/" + comment.productId)}" aria-label="${title} 상세 보기">
          ${BL.imageHtml(comment)}
        </a>
        <div class="my-comment-body">
          <div class="product-meta">${brand}${statusBadge(comment.status)}</div>
          <a class="product-title" href="${BL.url("/products/" + comment.productId)}">${title}</a>
          <p class="comment-text">${BL.escape(comment.content || "")}</p>
          <div class="meta-row">
            <span>${BL.escape(comment.createdAt || "")}</span>
            ${deletedAt}
            ${updatedAt}
          </div>
          <div class="product-card-actions">
            <a class="btn btn-ghost" href="${BL.url("/products/" + comment.productId)}">상품 보러가기</a>
            ${editButton}
            ${deleteButton}
          </div>
        </div>
      </article>`;
  }

  async function loadMyComments() {
    commentsTarget.innerHTML = BL.loading("내 의견을 불러오는 중입니다.");
    try {
      const res = await BL.get("/api/members/me/comments");
      const comments = res.data || [];
      updateCount("profile-comment-count", comments.length);
      commentsTarget.innerHTML = comments.length
        ? comments.map(commentCard).join("")
        : BL.empty("아직 남긴 의견이 없습니다.");
    } catch (e) {
      commentsTarget.innerHTML = BL.empty("내 의견을 불러오지 못했습니다.");
    }
  }

  function activityCard(item, metaHtml, actionsHtml) {
    const title = BL.escape(item.displayName || item.productName || "상품");
    const brand = item.brand ? `<span>${BL.escape(item.brand)}</span>` : "";
    return `
      <article class="card my-comment-card">
        <a class="my-comment-thumb" href="${BL.url("/products/" + item.productId)}" aria-label="${title} 상세 보기">
          ${BL.imageHtml(item)}
        </a>
        <div class="my-comment-body">
          <div class="product-meta">${brand}${metaHtml || ""}</div>
          <a class="product-title" href="${BL.url("/products/" + item.productId)}">${title}</a>
          <div class="product-card-actions">
            <a class="btn btn-ghost" href="${BL.url("/products/" + item.productId)}">상품 보기</a>
            ${actionsHtml || ""}
          </div>
        </div>
      </article>`;
  }

  async function loadFavorites() {
    favoritesTarget.innerHTML = BL.loading("찜한 상품을 불러오는 중입니다.");
    try {
      const res = await BL.get("/api/members/me/favorites");
      const list = res.data || [];
      BL.favoriteProductIds = {};
      list.forEach(function (product) {
        if (product.productId != null) BL.favoriteProductIds[String(product.productId)] = true;
      });
      updateCount("profile-favorite-count", list.length);
      favoritesTarget.innerHTML = list.length
        ? list.map(function (p) {
            return activityCard(p, `<span class="badge badge-primary">찜한 상품</span>`, `<button class="btn btn-danger" type="button" data-unfavorite="${p.productId}">찜 해제</button>`);
          }).join("")
        : BL.empty("아직 찜한 상품이 없습니다.");
    } catch (e) {
      favoritesTarget.innerHTML = BL.empty("찜한 상품을 불러오지 못했습니다.");
    }
  }

  async function loadMyRatings() {
    ratingsTarget.innerHTML = BL.loading("내 평가를 불러오는 중입니다.");
    try {
      const res = await BL.get("/api/members/me/ratings");
      const list = res.data || [];
      ratingsTarget.innerHTML = list.length
        ? list.map(function (r) {
            const meta = `<span class="badge badge-primary">${BL.escape(r.rating)}점</span><span>자극 ${ratingChoiceLabel(r.irritationYn, "있었음", "없었음")}</span><span>재구매 ${ratingChoiceLabel(r.repurchaseYn, "의사 있음", "의사 없음")}</span><span>${BL.escape(r.updatedAt || r.createdAt || "")}</span>`;
            return activityCard(r, meta, `<a class="btn btn-primary" href="${BL.url("/products/" + r.productId)}">평가 수정</a><button class="btn btn-ghost" type="button" data-delete-rating="${r.productId}">평가 삭제</button>`);
          }).join("")
        : BL.empty("아직 평가한 상품이 없습니다.");
    } catch (e) {
      ratingsTarget.innerHTML = BL.empty("내 평가를 불러오지 못했습니다.");
    }
  }

  async function loadRecentProducts() {
    recentTarget.innerHTML = BL.loading("최근 본 상품을 불러오는 중입니다.");
    try {
      const res = await BL.get("/api/members/me/recent-products");
      const list = res.data || [];
      updateCount("profile-recent-count", list.length);
      recentTarget.innerHTML = list.length
        ? list.map(function (p) { return BL.productCard(p); }).join("")
        : BL.empty("최근 본 상품이 없습니다.");
      await BL.syncFavoriteButtons(recentTarget);
    } catch (e) {
      recentTarget.innerHTML = BL.empty("최근 본 상품을 불러오지 못했습니다.");
    }
  }

  async function loadRecommendationFeedback() {
    feedbackTarget.innerHTML = BL.loading("추천 피드백을 불러오는 중입니다.");
    try {
      const res = await BL.get("/api/members/me/recommendation-feedback");
      const list = res.data || [];
      feedbackTarget.innerHTML = list.length
        ? list.map(function (f) {
            const label = f.feedbackType === "LIKE" ? "좋아요" : f.feedbackType === "DISLIKE" ? "별로예요" : "관심 없음";
            const meta = `<span class="badge badge-primary">${label}</span><span>${BL.escape(f.createdAt || "")}</span>`;
            return activityCard(f, meta, `<button class="btn btn-ghost" type="button" data-delete-feedback="${f.productId}">피드백 취소</button>`);
          }).join("")
        : BL.empty("아직 추천 피드백 내역이 없습니다.");
    } catch (e) {
      feedbackTarget.innerHTML = BL.empty("추천 피드백을 불러오지 못했습니다.");
    }
  }

  async function loadFaceStatus() {
    try {
      const res = await BL.get("/api/members/me/face");
      const status = res.data || {};
      if (status.registered) {
        faceStatus.innerHTML = `
          <span class="badge badge-active">등록됨</span>
          <span class="muted">최근 등록일 ${BL.escape(status.updatedAt || status.createdAt || "-")}</span>`;
        updateText("profile-face-status", "등록됨");
        faceEnrollBtn.textContent = "얼굴 재등록";
        faceDisableBtn.style.display = "inline-flex";
      } else {
        faceStatus.innerHTML = `<span class="badge">미등록</span><span class="muted">얼굴 로그인을 사용하려면 먼저 등록해 주세요.</span>`;
        updateText("profile-face-status", "미등록");
        faceEnrollBtn.textContent = "얼굴 등록하기";
        faceDisableBtn.style.display = "none";
      }
    } catch (e) {
      updateText("profile-face-status", "확인 필요");
      faceStatus.innerHTML = `<span class="badge badge-warning">확인 필요</span><span class="muted">얼굴 로그인 상태를 확인하지 못했습니다.</span>`;
    }
  }

  try {
    me = await BL.me();
    if (!me) {
      gate.innerHTML = BL.requireLoginView("마이페이지는 로그인이 필요합니다.");
      return;
    }
    gate.style.display = "none";
    mypageLayout.style.display = "grid";
    form.style.display = "block";
    document.getElementById("loginId").value = me.loginId || "";
    document.getElementById("nickname").value = me.nickname || "";
    document.getElementById("skinType").value = me.skinType || "dry";
    applyConcernValue(me.skinConcern || "");
    updateText("profile-member-name", me.nickname || me.loginId || "회원");
    updateText("profile-skin-type", BL.skinLabel(me.skinType || "dry"));
    await loadFaceStatus();
    await loadMyComments();
    await loadFavorites();
    await loadMyRatings();
    await loadRecentProducts();
    await loadRecommendationFeedback();
  } catch (e) {
    gate.innerHTML = BL.empty("회원 정보를 불러오지 못했습니다.");
  }

  form.addEventListener("submit", async function (event) {
    event.preventDefault();
    const selected = selectedConcerns();
    if (selected.length < 2 || selected.length > 4) {
      BL.setNotice("mypage-notice", "피부 관심사는 2~4개를 선택해 주세요.", "error");
      return;
    }
    const body = {
      nickname: document.getElementById("nickname").value.trim(),
      skinType: document.getElementById("skinType").value,
      skinConcern: document.getElementById("skinConcern").value.trim()
    };
    try {
      await BL.put("/api/members/me", body);
      BL.setNotice("mypage-notice", "회원 정보가 수정되었습니다.", "success");
      await BL.initNav();
    } catch (e) {
      BL.setNotice("mypage-notice", "수정에 실패했습니다.", "error");
    }
  });

  concernGroup.addEventListener("change", function (event) {
    hasUnmappedConcern = false;
    const selected = selectedConcerns();
    if (selected.length > 4) {
      event.target.checked = false;
      BL.setNotice("mypage-notice", "피부 관심사는 2~4개 정도만 선택해 주세요.", "error");
    }
    syncConcerns();
  });

  commentsTarget.addEventListener("click", async function (event) {
    const button = event.target.closest("[data-delete-comment]");
    const editButton = event.target.closest("[data-edit-my-comment]");
    const saveButton = event.target.closest("[data-save-my-comment-edit]");
    const cancelButton = event.target.closest("[data-cancel-my-comment-edit]");
    if (!button && !editButton && !saveButton && !cancelButton) return;
    try {
      if (button) {
        if (!confirm("이 의견을 삭제하시겠습니까?")) return;
        await BL.del("/api/comments/" + button.dataset.deleteComment);
        BL.setNotice("my-comments-notice", "의견이 삭제되었습니다.", "success");
        await loadMyComments();
      } else if (editButton) {
        const card = editButton.closest(".my-comment-card");
        if (!card || card.querySelector(".comment-edit-panel")) return;
        const text = card.querySelector(".comment-text");
        const panel = document.createElement("div");
        panel.className = "comment-edit-panel";
        panel.innerHTML = `
          <textarea maxlength="1000" rows="3" aria-label="내 의견 수정">${BL.escape(text ? text.textContent.trim() : "")}</textarea>
          <div class="action-row">
            <button class="btn btn-primary" type="button" data-save-my-comment-edit="${editButton.dataset.editMyComment}">수정 저장</button>
            <button class="btn btn-ghost" type="button" data-cancel-my-comment-edit>취소</button>
          </div>`;
        if (text) text.style.display = "none";
        const body = card.querySelector(".my-comment-body");
        if (body) body.appendChild(panel);
      } else if (cancelButton) {
        const card = cancelButton.closest(".my-comment-card");
        const panel = cancelButton.closest(".comment-edit-panel");
        const text = card && card.querySelector(".comment-text");
        if (panel) panel.remove();
        if (text) text.style.display = "";
      } else if (saveButton) {
        const panel = saveButton.closest(".comment-edit-panel");
        const textarea = panel && panel.querySelector("textarea");
        const content = textarea ? textarea.value.trim() : "";
        if (!content) {
          BL.setNotice("my-comments-notice", "수정할 의견을 입력해 주세요.", "error");
          return;
        }
        await BL.put("/api/comments/" + saveButton.dataset.saveMyCommentEdit, { content: content });
        BL.setNotice("my-comments-notice", "의견을 수정했습니다.", "success");
        await loadMyComments();
      }
    } catch (e) {
      const message = e.status === 403 ? "의견을 처리할 권한이 없습니다." : "의견 처리에 실패했습니다.";
      BL.setNotice("my-comments-notice", message, "error");
    }
  });

  ratingsTarget.addEventListener("click", async function (event) {
    const button = event.target.closest("[data-delete-rating]");
    if (!button) return;
    if (!confirm("이 상품에 남긴 평가를 삭제하시겠습니까?")) return;
    try {
      await BL.del("/api/products/" + button.dataset.deleteRating + "/rating");
      await loadMyRatings();
    } catch (e) {
      alert("평가 삭제에 실패했습니다.");
    }
  });

  favoritesTarget.addEventListener("click", async function (event) {
    const button = event.target.closest("[data-unfavorite]");
    if (!button) return;
    try {
      await BL.del("/api/products/" + button.dataset.unfavorite + "/favorite");
      if (BL.favoriteProductIds) {
        delete BL.favoriteProductIds[String(button.dataset.unfavorite)];
      }
      BL.updateFavoriteButtons(button.dataset.unfavorite, false);
      BL.setNotice("my-favorites-notice", "찜을 해제했습니다.", "success");
      await loadFavorites();
    } catch (e) {
      BL.setNotice("my-favorites-notice", e.status === 401 ? "로그인이 필요합니다." : "찜 해제에 실패했습니다.", "error");
    }
  });

  recentTarget.addEventListener("click", async function (event) {
    const favoriteButton = event.target.closest("[data-favorite-product]");
    if (!favoriteButton) return;
    event.preventDefault();
    await BL.handleFavoriteClick(favoriteButton);
  });

  feedbackTarget.addEventListener("click", async function (event) {
    const button = event.target.closest("[data-delete-feedback]");
    if (!button) return;
    try {
      await BL.del("/api/members/me/recommendation-feedback/" + button.dataset.deleteFeedback);
      await loadRecommendationFeedback();
    } catch (e) {
      alert("추천 피드백 취소에 실패했습니다.");
    }
  });

  faceEnrollBtn.addEventListener("click", async function () {
    const steps = [
      "정면을 바라봐 주세요.",
      "왼쪽을 살짝 바라봐 주세요.",
      "오른쪽을 살짝 바라봐 주세요.",
      "위쪽을 살짝 바라봐 주세요.",
      "아래쪽을 살짝 바라봐 주세요."
    ];
    try {
      BL.setNotice("face-notice", "");
      const images = await BL.faceCaptureSequence(steps);
      BL.setNotice("face-notice", "얼굴 정보를 저장하고 있습니다. 잠시만 기다려 주세요.", "");
      await BL.post("/api/members/me/face", { images: images });
      BL.setNotice("face-notice", "얼굴 로그인이 등록되었습니다.", "success");
      await loadFaceStatus();
    } catch (e) {
      const message = e.status === 503
        ? "얼굴 로그인 기능을 잠시 사용할 수 없습니다. 비밀번호로 로그인해 주세요."
        : e.status === 400
          ? "얼굴을 정확히 확인하지 못했습니다. 밝은 곳에서 한 사람만 촬영해 주세요."
          : e.message || "얼굴 등록에 실패했습니다.";
      BL.setNotice("face-notice", message, "error");
    }
  });

  faceDisableBtn.addEventListener("click", async function () {
    try {
      await BL.del("/api/members/me/face");
      BL.setNotice("face-notice", "얼굴 로그인이 해제되었습니다.", "success");
      await loadFaceStatus();
    } catch (e) {
      BL.setNotice("face-notice", "얼굴 등록 해제에 실패했습니다.", "error");
    }
  });
});
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
