<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<%
  request.setAttribute("pageTitle", "BeautyLens");
  request.setAttribute("activePage", "home");
%>
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<main class="page">
  <section class="hero">
    <div class="hero-panel">
      <p class="eyebrow">Review-powered cosmetic recommendation</p>
      <h1>BeautyLens</h1>
      <p class="lead">피부 타입과 리뷰 데이터를 바탕으로 나에게 맞는 화장품을 더 쉽게 고를 수 있도록 돕습니다.</p>
      <div class="actions">
        <a class="btn btn-primary" href="<%= request.getContextPath() %>/recommend">추천 받기</a>
        <a class="btn btn-ghost" href="<%= request.getContextPath() %>/products">상품 둘러보기</a>
      </div>
      <div class="metric-strip">
        <div><strong>1,521</strong><span>상품</span></div>
        <div><strong>323,574</strong><span>리뷰</span></div>
        <div><strong>2</strong><span>플랫폼</span></div>
        <div><strong>5</strong><span>피부 타입</span></div>
      </div>
    </div>
    <div class="hero-panel">
      <div class="grid grid-3">
        <div class="feature-card">
          <h3>피부 타입별 추천</h3>
          <p>건성, 지성, 복합성, 민감성, 중성 기준으로 상품을 탐색할 수 있습니다.</p>
        </div>
        <div class="feature-card">
          <h3>리뷰 기반 분석</h3>
          <p>긍정 리뷰와 주의해서 볼 리뷰를 분리해 선택에 필요한 단서를 보여줍니다.</p>
        </div>
        <div class="feature-card">
          <h3>사용자 의견 확인</h3>
          <p>상품을 사용해 본 회원 의견을 함께 확인하고 나의 의견도 남길 수 있습니다.</p>
        </div>
      </div>
    </div>
  </section>

  <section class="section">
    <div class="section-head">
      <div>
        <h2>이미지 있는 추천 상품</h2>
        <p class="muted">추천 점수 상위 상품 중 이미지가 준비된 상품을 우선 표시합니다.</p>
      </div>
      <a class="btn btn-ghost" href="<%= request.getContextPath() %>/products">상품 더 보기</a>
    </div>
    <div id="home-products" class="grid product-grid"><div class="loading">상품을 불러오는 중입니다.</div></div>
  </section>
</main>

<script>
document.addEventListener("DOMContentLoaded", async function () {
  const target = document.getElementById("home-products");
  try {
    const res = await BL.get("/api/products?sortBy=score&size=40");
    const products = (res.data || []).filter(function (p) { return !!p.imageUrl; }).slice(0, 12);
    const list = products.length ? products : (res.data || []).slice(0, 8);
    target.innerHTML = list.length ? list.map(function (p) { return BL.productCard(p); }).join("") : BL.empty("표시할 상품이 없습니다.");
    await BL.syncFavoriteButtons(target);
  } catch (e) {
    target.innerHTML = BL.empty("상품을 불러오지 못했습니다.");
  }

  target.addEventListener("click", async function (event) {
    const favoriteButton = event.target.closest("[data-favorite-product]");
    if (!favoriteButton) return;
    event.preventDefault();
    await BL.handleFavoriteClick(favoriteButton);
  });
});
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
