<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<main class="page page-shell">
  <section class="section-head">
    <div>
      <p class="eyebrow">상품 탐색</p>
      <h1>상품 탐색</h1>
      <p class="muted">브랜드, 플랫폼, 피부 타입 기준으로 외부 리뷰 분석 상품을 비교해 보세요.</p>
    </div>
    <a class="btn btn-primary" href="<%= request.getContextPath() %>/recommend">추천 받기</a>
  </section>

  <section class="card card-pad product-browser-panel">
    <div class="browse-intro">
      <div>
        <h2>비교할 상품을 찾아보세요</h2>
        <p class="muted">카드에서는 핵심 정보만 보고, 자세한 추천 근거와 평가는 상품 상세에서 확인합니다.</p>
      </div>
      <div class="badge badge-primary">외부 리뷰 분석 기반</div>
    </div>
    <div class="search-row">
      <div class="field search-field">
        <label for="product-keyword">검색어</label>
        <input id="product-keyword" placeholder="상품명 또는 브랜드를 입력하세요">
      </div>
      <div class="field compact-field">
        <label for="sort-by">정렬</label>
        <select id="sort-by">
          <option value="score">추천 점수순</option>
          <option value="reviewCount">리뷰 많은 순</option>
          <option value="rating">평점순</option>
        </select>
      </div>
      <button id="product-search" class="btn btn-primary" type="button">검색</button>
    </div>

    <div class="filter-block">
      <span class="badge">플랫폼</span>
      <button class="btn btn-ghost is-selected" type="button" data-platform="all" aria-pressed="true">전체</button>
      <button class="btn btn-ghost" type="button" data-platform="oliveyoung" aria-pressed="false">Olive Young</button>
      <button class="btn btn-ghost" type="button" data-platform="musinsa" aria-pressed="false">Musinsa</button>
    </div>

    <div class="filter-block">
      <span class="badge">피부 타입</span>
      <button class="btn btn-ghost is-selected" type="button" data-skin="" aria-pressed="true">전체</button>
      <button class="btn btn-ghost" type="button" data-skin="건성" aria-pressed="false">건성</button>
      <button class="btn btn-ghost" type="button" data-skin="지성" aria-pressed="false">지성</button>
      <button class="btn btn-ghost" type="button" data-skin="복합성" aria-pressed="false">복합성</button>
      <button class="btn btn-ghost" type="button" data-skin="민감성" aria-pressed="false">민감성</button>
      <button class="btn btn-ghost" type="button" data-skin="중성" aria-pressed="false">중성</button>
    </div>

    <div class="filter-block">
      <button id="products-image-only" class="btn btn-ghost" type="button" aria-pressed="false">이미지 있는 상품만</button>
      <span id="products-filter-summary" class="muted filter-summary-inline">전체 상품을 추천 점수순으로 보고 있습니다.</span>
    </div>
  </section>

  <section class="section">
    <div class="section-head compact-head">
      <div>
        <h2>상품 목록</h2>
        <p class="muted">상품 카드는 빠른 비교를 위해 간결하게 표시됩니다.</p>
      </div>
    </div>
    <div id="products-list" class="grid product-grid"><div class="loading">상품을 불러오는 중입니다.</div></div>
    <div class="load-more-row">
      <button id="products-more" class="btn btn-ghost" type="button" style="display:none;">더 보기</button>
    </div>
  </section>
</main>

<script>
document.addEventListener("DOMContentLoaded", function () {
  const target = document.getElementById("products-list");
  const moreBtn = document.getElementById("products-more");
  const summary = document.getElementById("products-filter-summary");
  const keywordInput = document.getElementById("product-keyword");
  const sortSelect = document.getElementById("sort-by");
  let page = 1;
  let platform = "all";
  let skinType = "";
  let imageOnly = false;
  let loading = false;

  function selectedButton(selector, value) {
    document.querySelectorAll(selector).forEach(function (btn) {
      const current = selector === "button[data-platform]" ? btn.dataset.platform : btn.dataset.skin;
      const selected = current === value;
      btn.classList.toggle("is-selected", selected);
      btn.setAttribute("aria-pressed", selected ? "true" : "false");
    });
  }

  function updateSummary() {
    const labels = [];
    labels.push(platform === "all" ? "전체 플랫폼" : BL.platformLabel(platform));
    if (skinType) labels.push(BL.skinLabel(skinType));
    if (imageOnly) labels.push("이미지 있는 상품만");
    const sortLabel = sortSelect.options[sortSelect.selectedIndex].textContent;
    summary.textContent = labels.join(", ") + " · " + sortLabel;
  }

  function buildQuery() {
    const params = new URLSearchParams();
    params.set("page", page);
    params.set("size", "20");
    params.set("sortBy", sortSelect.value);
    const keyword = keywordInput.value.trim();
    if (keyword) params.set("keyword", keyword);
    if (platform !== "all") params.set("platform", platform);
    if (skinType) params.set("skinType", skinType);
    if (imageOnly) params.set("imageOnly", "true");
    return params.toString();
  }

  async function load(reset) {
    if (loading) return;
    loading = true;
    if (reset) {
      page = 1;
      target.innerHTML = BL.loading("상품을 불러오는 중입니다.");
      moreBtn.style.display = "none";
    }
    updateSummary();
    try {
      const res = await BL.get("/api/products?" + buildQuery());
      const list = res.data || [];
      const html = list.length ? list.map(function (p) { return BL.productCard(p); }).join("") : BL.empty("조건에 맞는 상품이 없습니다.");
      target.innerHTML = reset ? html : target.innerHTML + html;
      await BL.syncFavoriteButtons(target);
      moreBtn.style.display = list.length === 20 ? "inline-flex" : "none";
    } catch (e) {
      target.innerHTML = BL.empty("상품을 불러오지 못했습니다.");
      moreBtn.style.display = "none";
    } finally {
      loading = false;
    }
  }

  document.getElementById("product-search").addEventListener("click", function () {
    load(true);
  });

  keywordInput.addEventListener("keydown", function (event) {
    if (event.key === "Enter") load(true);
  });

  sortSelect.addEventListener("change", function () {
    load(true);
  });

  document.querySelectorAll("button[data-platform]").forEach(function (btn) {
    btn.addEventListener("click", function () {
      platform = btn.dataset.platform;
      selectedButton("button[data-platform]", platform);
      load(true);
    });
  });

  document.querySelectorAll("[data-skin]").forEach(function (btn) {
    btn.addEventListener("click", function () {
      skinType = btn.dataset.skin;
      selectedButton("[data-skin]", skinType);
      load(true);
    });
  });

  document.getElementById("products-image-only").addEventListener("click", function () {
    imageOnly = !imageOnly;
    this.classList.toggle("is-selected", imageOnly);
    this.setAttribute("aria-pressed", imageOnly ? "true" : "false");
    load(true);
  });

  moreBtn.addEventListener("click", function () {
    page += 1;
    load(false);
  });

  target.addEventListener("click", async function (event) {
    const favoriteButton = event.target.closest("[data-favorite-product]");
    if (!favoriteButton) return;
    event.preventDefault();
    await BL.handleFavoriteClick(favoriteButton);
  });

  load(true);
});
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
