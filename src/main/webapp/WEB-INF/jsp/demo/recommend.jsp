<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<main class="page page-shell">
  <section class="section-head">
    <div>
      <p class="eyebrow">맞춤 추천</p>
      <h1>추천 상품</h1>
      <p class="muted">피부 타입과 외부 리뷰 분석을 기준으로 후보 상품을 제안합니다.</p>
    </div>
    <a class="btn btn-ghost" href="<%= request.getContextPath() %>/mypage">피부 정보 수정</a>
  </section>

  <section class="recommend-guide">
    <article class="section-card">
      <h2>추천 기준</h2>
      <p class="muted">BeautyLens는 외부 리뷰의 긍정 신호와 주의 신호를 먼저 분석하고, 내부 사용자 반응은 보조 지표로 함께 참고합니다.</p>
      <p class="muted">마이페이지의 피부 관심사는 상품 태그와 맞을 때 추천 이유에 반영됩니다. 관심사는 2~4개만 골라두면 충분합니다.</p>
    </article>
    <article class="section-card">
      <h2>평가는 상세에서</h2>
      <p class="muted">별점, 자극 여부, 재구매 의사는 상품 정보를 충분히 본 뒤 상세 화면에서 남겨 주세요.</p>
    </article>
  </section>

  <section class="card card-pad recommendation-control" style="margin-bottom:18px;">
    <div class="section-head" style="margin-bottom:12px;">
      <div>
        <h2 style="margin-bottom:4px;">피부 타입 선택</h2>
        <p id="skin-basis" class="muted">추천 기준을 준비하는 중입니다.</p>
        <p id="concern-basis" class="muted">피부 관심사를 확인하는 중입니다.</p>
      </div>
    </div>
    <div class="filters skin-filter" aria-label="피부 타입 선택">
      <button class="btn btn-ghost" type="button" data-skin-type="dry" aria-pressed="false">건성</button>
      <button class="btn btn-ghost" type="button" data-skin-type="oily" aria-pressed="false">지성</button>
      <button class="btn btn-ghost" type="button" data-skin-type="combination" aria-pressed="false">복합성</button>
      <button class="btn btn-ghost" type="button" data-skin-type="sensitive" aria-pressed="false">민감성</button>
      <button class="btn btn-ghost" type="button" data-skin-type="normal" aria-pressed="false">중성</button>
    </div>
  </section>

  <section class="card card-pad recommendation-control" style="margin-bottom:18px;">
    <p id="filter-summary" class="muted filter-summary">필터: 전체</p>
    <div class="filters">
      <label class="badge">필터</label>
      <button class="btn btn-ghost is-selected" type="button" data-platform="all" aria-pressed="true">전체</button>
      <button class="btn btn-ghost" type="button" data-platform="oliveyoung" aria-pressed="false">Olive Young</button>
      <button class="btn btn-ghost" type="button" data-platform="musinsa" aria-pressed="false">Musinsa</button>
      <button class="btn btn-ghost" type="button" id="image-only" aria-pressed="false">이미지 있는 상품만</button>
    </div>
  </section>

  <div id="recommend-gate"></div>
  <section class="section">
    <div class="section-head compact-head">
      <div>
        <h2>추천 결과</h2>
        <p class="muted">먼저 상품을 비교하고, 자세한 근거는 상세 화면에서 확인하세요.</p>
      </div>
    </div>
    <div id="recommend-products" class="grid product-grid"><div class="loading">추천 상품을 불러오는 중입니다.</div></div>
  </section>
</main>

<script>
document.addEventListener("DOMContentLoaded", async function () {
  const gate = document.getElementById("recommend-gate");
  const target = document.getElementById("recommend-products");
  const skinBasis = document.getElementById("skin-basis");
  const concernBasis = document.getElementById("concern-basis");
  const filterSummary = document.getElementById("filter-summary");
  let items = [];
  let platform = "all";
  let imageOnly = false;
  let selectedSkinType = "dry";
  let selectedConcerns = [];
  let hasUnmappedConcern = false;
  let hiddenRecommendationProductIds = {};
  const allowedConcerns = ["보습", "진정", "트러블", "여드름", "건조", "민감", "홍조", "모공", "피지", "각질", "미백", "장벽"];
  const skinLabels = {
    dry: "건성",
    oily: "지성",
    combination: "복합성",
    sensitive: "민감성",
    normal: "중성"
  };

  function setSkinActive(skinType) {
    document.querySelectorAll("[data-skin-type]").forEach(function (btn) {
      const selected = btn.dataset.skinType === skinType;
      btn.classList.toggle("is-selected", selected);
      btn.setAttribute("aria-pressed", selected ? "true" : "false");
    });
    skinBasis.textContent = skinLabels[skinType] + " 피부 기준 추천";
  }

  function updateFilterSummary() {
    const labels = [];
    labels.push(platform === "all" ? "전체 플랫폼" : BL.platformLabel(platform));
    if (imageOnly) labels.push("이미지 있는 상품만");
    filterSummary.textContent = "현재 추천 기준: " + skinLabels[selectedSkinType] + " 피부 · 필터: " + labels.join(", ");
  }

  function parseConcerns(value) {
    const raw = String(value || "").split(/[,，\s]+/).map(function (item) {
      return item.trim();
    }).filter(Boolean);
    const selected = raw.filter(function (item) {
      return allowedConcerns.indexOf(item) >= 0;
    });
    hasUnmappedConcern = raw.length > 0 && selected.length === 0;
    return selected;
  }

  function updateConcernBasis() {
    if (selectedConcerns.length) {
      concernBasis.textContent = "내 피부 관심사: " + selectedConcerns.join(", ") + " · 상품 태그와 맞으면 추천 이유에 표시됩니다.";
      return;
    }
    concernBasis.textContent = hasUnmappedConcern
      ? "이전에 입력한 피부 관심사가 현재 선택 항목과 맞지 않습니다. 마이페이지에서 2~4개를 다시 선택해 주세요."
      : "피부 관심사를 2~4개 설정하면 상품 태그와 맞는 항목을 추천 이유에서 확인할 수 있습니다.";
  }

  function productKey(productId) {
    return String(productId == null ? "" : productId);
  }

  function isHiddenRecommendation(productId) {
    const key = productKey(productId);
    return !!key && !!hiddenRecommendationProductIds[key];
  }

  async function loadHiddenRecommendationFeedback() {
    hiddenRecommendationProductIds = {};
    try {
      const res = await BL.get("/api/members/me/recommendation-feedback");
      (res.data || []).forEach(function (feedback) {
        if (feedback.feedbackType === "NOT_INTERESTED") {
          hiddenRecommendationProductIds[productKey(feedback.productId)] = true;
        }
      });
      return true;
    } catch (e) {
      return e.status === 401;
    }
  }

  function render() {
    let list = items.filter(function (p) { return !isHiddenRecommendation(p.productId); });
    if (platform !== "all") list = list.filter(function (p) { return p.platform === platform; });
    if (imageOnly) list = list.filter(function (p) { return !!p.imageUrl; });
    target.innerHTML = list.length ? list.map(function (p) { return BL.productCard(p, { reason: true, feedback: false }); }).join("") : BL.empty("조건에 맞는 추천 상품이 없습니다.");
    BL.syncFavoriteButtons(target);
    updateFilterSummary();
  }

  async function loadRecommendations(skinType) {
    target.innerHTML = BL.loading("추천 상품을 불러오는 중입니다.");
    selectedSkinType = skinType;
    setSkinActive(skinType);
    updateFilterSummary();

    const res = await BL.get("/api/recommendations?skinType=" + encodeURIComponent(skinType) + "&size=20");
    items = (res.data || []).filter(function (item) { return !isHiddenRecommendation(item.productId); });
    render();
  }

  try {
    const me = await BL.me();
    if (me) await loadHiddenRecommendationFeedback();
    selectedSkinType = me && me.skinType ? me.skinType : "dry";
    selectedConcerns = me ? parseConcerns(me.skinConcern) : [];
    updateConcernBasis();
    await loadRecommendations(selectedSkinType);
  } catch (e) {
    selectedConcerns = [];
    updateConcernBasis();
    target.innerHTML = e.status === 400
      ? BL.empty("피부 타입을 먼저 설정해 주세요. 마이페이지에서 수정할 수 있습니다.")
      : BL.empty("추천 상품을 불러오지 못했습니다.");
  }

  document.querySelectorAll("button[data-platform]").forEach(function (btn) {
    btn.addEventListener("click", function () {
      document.querySelectorAll("button[data-platform]").forEach(function (b) { b.classList.remove("is-selected"); });
      document.querySelectorAll("button[data-platform]").forEach(function (b) { b.setAttribute("aria-pressed", "false"); });
      btn.classList.add("is-selected");
      btn.setAttribute("aria-pressed", "true");
      platform = btn.dataset.platform;
      render();
    });
  });
  document.querySelectorAll("[data-skin-type]").forEach(function (btn) {
    btn.addEventListener("click", async function () {
      try {
        await loadRecommendations(btn.dataset.skinType);
      } catch (e) {
        target.innerHTML = BL.empty("추천 상품을 불러오지 못했습니다.");
      }
    });
  });
  document.getElementById("image-only").addEventListener("click", function () {
    imageOnly = !imageOnly;
    this.classList.toggle("is-selected", imageOnly);
    this.setAttribute("aria-pressed", imageOnly ? "true" : "false");
    render();
  });

  target.addEventListener("click", async function (event) {
    const favoriteButton = event.target.closest("[data-favorite-product]");
    if (favoriteButton) {
      event.preventDefault();
      await BL.handleFavoriteClick(favoriteButton);
      return;
    }
    const feedbackButton = event.target.closest("[data-rec-feedback]");
    if (feedbackButton) {
      event.preventDefault();
      await BL.handleRecommendationFeedbackClick(feedbackButton);
      if (feedbackButton.dataset.recFeedback === "NOT_INTERESTED") {
        hiddenRecommendationProductIds[productKey(feedbackButton.dataset.productId)] = true;
        items = items.filter(function (item) { return !isHiddenRecommendation(item.productId); });
        render();
      }
    }
  });
});
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
