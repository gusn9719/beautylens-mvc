<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<main class="page page-shell">
  <div class="admin-layout">
    <jsp:include page="/WEB-INF/jsp/admin/_sidebar.jsp" />
    <section class="admin-content">
      <div class="section-head">
        <div>
          <p class="eyebrow">운영 현황</p>
          <h1>관리자 대시보드</h1>
          <p class="muted">상품, 리뷰, 회원 의견, 이미지 수집 현황을 한눈에 확인합니다.</p>
        </div>
        <div class="actions">
          <a class="btn btn-primary" href="<%= request.getContextPath() %>/admin/products">상품 관리</a>
          <a class="btn btn-ghost" href="<%= request.getContextPath() %>/admin/comment-reports">신고 관리</a>
        </div>
      </div>

      <div id="admin-gate"></div>
      <div id="admin-content" style="display:none;">
    <div id="admin-stats" class="grid admin-stats"></div>
    <div id="admin-operations" class="grid admin-card-grid section"></div>

    <div class="grid grid-3 section">
      <div class="card card-pad">
        <h2>플랫폼별 상품</h2>
        <div id="platform-counts"></div>
      </div>
      <div class="card card-pad">
        <h2>피부 타입별 상품</h2>
        <div id="skin-counts"></div>
      </div>
      <div class="card card-pad">
        <h2>이미지 커버리지</h2>
        <div id="image-coverage"></div>
      </div>
    </div>
    <div class="grid grid-2 section">
      <div class="card card-pad">
        <h2>최근 댓글</h2>
        <div id="recent-comments" class="admin-list"></div>
      </div>
      <div class="card card-pad">
        <h2>주의 신호 분포</h2>
        <div id="caution-counts"></div>
      </div>
    </div>
      </div>
    </section>
  </div>
</main>

<script>
document.addEventListener("DOMContentLoaded", async function () {
  const gate = document.getElementById("admin-gate");
  const content = document.getElementById("admin-content");

  function stat(label, value) {
    return `<article class="card card-pad stat"><div class="muted">${BL.escape(label)}</div><div class="value">${BL.escape(value)}</div></article>`;
  }

  function operationCard(title, value, description, href, actionText, tone) {
    const badgeClass = tone === "warning" ? "badge-warning" : tone === "danger" ? "badge-danger" : "badge-primary";
    return `<article class="section-card">
      <div class="section-head compact-head">
        <div>
          <span class="badge ${badgeClass}">${BL.escape(value)}</span>
          <h2>${BL.escape(title)}</h2>
        </div>
      </div>
      <p class="muted">${BL.escape(description)}</p>
      <a class="btn btn-ghost" href="${BL.url(href)}">${BL.escape(actionText)}</a>
    </article>`;
  }

  function mapRows(map, labelFn) {
    const entries = Object.entries(map || {});
    return entries.length ? entries.map(function (entry) {
      const label = labelFn ? labelFn(entry[0]) : entry[0];
      return `<div class="meta-row" style="justify-content:space-between;padding:8px 0;border-bottom:1px solid var(--line);"><span>${BL.escape(label)}</span><strong>${BL.escape(entry[1])}</strong></div>`;
    }).join("") : BL.empty("데이터가 없습니다.");
  }

  function percent(found, total) {
    if (!total) return 0;
    return Math.round((Number(found || 0) / Number(total)) * 100);
  }

  function renderRecentComments(list) {
    const target = document.getElementById("recent-comments");
    if (!list || !list.length) {
      target.innerHTML = BL.empty("최근 댓글이 없습니다.");
      return;
    }
    target.innerHTML = list.slice(0, 5).map(function (c) {
      const title = BL.escape(c.displayName || c.productName || "상품명 없음");
      return `<div class="admin-list-item">
        <strong>${title}</strong>
        <span class="muted">${BL.escape(c.nickname || c.loginId || "작성자")} · ${BL.escape(c.createdAt || "")}</span>
        <span>${BL.escape(c.content || "")}</span>
        <a class="btn btn-ghost" href="${BL.url("/products/" + c.productId)}">상품 보기</a>
      </div>`;
    }).join("");
  }

  try {
    const me = await BL.me();
    if (!me) {
      gate.innerHTML = BL.requireLoginView("관리자 화면은 로그인이 필요합니다.");
      return;
    }
    if (me.role !== "ADMIN") {
      gate.innerHTML = '<div class="notice notice-error"><strong>관리자만 접근할 수 있습니다.</strong><div style="margin-top:12px;"><a class="btn btn-ghost" href="' + BL.url("/") + '">홈으로 이동</a></div></div>';
      return;
    }
    const res = await BL.get("/api/admin/summary");
    const s = res.data;
    content.style.display = "block";
    document.getElementById("admin-stats").innerHTML = [
      stat("상품 수", s.productCount),
      stat("리뷰 수", s.reviewCount),
      stat("회원 수", s.memberCount),
      stat("얼굴 등록 회원", s.faceRegisteredMemberCount || 0),
      stat("전체 댓글", s.commentCount),
      stat("활성 댓글", s.activeCommentCount),
      stat("삭제 댓글", s.deletedCommentCount),
      stat("이미지 확보 상품", s.imageFoundProductCount),
      stat("이미지 미확보 상품", s.imageMissingProductCount || Math.max(0, (s.productCount || 0) - (s.imageFoundProductCount || 0))),
      stat("숨김 상품", s.hiddenProductCount || 0),
      stat("추천 제외 상품", s.recommendationExcludedProductCount || 0),
      stat("내부 별점 수", s.siteRatingCount || 0),
      stat("평균 내부 별점", s.siteRatingAverage || 0),
      stat("찜 수", s.favoriteCount || 0),
      stat("최근 7일 조회", s.recentViewCount || 0),
      stat("신고 대기", s.pendingReportCount || 0),
      stat("운영 상태", "정상")
    ].join("");
    document.getElementById("admin-operations").innerHTML = [
      operationCard("신고 대기", (s.pendingReportCount || 0) + "건", "회원 신고를 먼저 확인하고 처리 상태를 남깁니다.", "/admin/comment-reports", "신고 확인", (s.pendingReportCount || 0) > 0 ? "warning" : "primary"),
      operationCard("이미지 미확보", (s.imageMissingProductCount || 0) + "개", "이미지가 없는 상품은 추천 신뢰도가 낮아질 수 있습니다.", "/admin/products", "상품 품질 확인", (s.imageMissingProductCount || 0) > 0 ? "warning" : "primary"),
      operationCard("추천 제외", (s.recommendationExcludedProductCount || 0) + "개", "추천에서 제외된 상품과 운영 메모를 정기적으로 점검합니다.", "/admin/products#recommendation", "추천 운영 확인", "primary"),
      operationCard("최근 운영 로그", "기록 확인", "상품 상태와 댓글 처리 이력을 추적합니다.", "/admin/logs", "운영 로그 보기", "primary")
    ].join("");
    document.getElementById("platform-counts").innerHTML = mapRows(s.platformProductCounts, BL.platformLabel);
    document.getElementById("skin-counts").innerHTML = mapRows(s.skinTypeProductCounts, BL.skinLabel);
    document.getElementById("caution-counts").innerHTML = mapRows(s.cautionLevelCounts, BL.cautionLabel);
    const coverage = percent(s.imageFoundProductCount, s.productCount);
    document.getElementById("image-coverage").innerHTML =
      `<div class="meta-row" style="justify-content:space-between;"><span>확보율</span><strong>${coverage}%</strong></div>
       <div class="coverage-bar" aria-label="이미지 확보율"><span style="width:${coverage}%"></span></div>
       <p class="muted" style="margin-top:12px;">이미지 확보 ${BL.escape(s.imageFoundProductCount || 0)}개 · 미확보 ${BL.escape(s.imageMissingProductCount || 0)}개</p>`;
    try {
      const comments = await BL.get("/api/admin/comments?size=5");
      renderRecentComments(comments.data || []);
    } catch (e) {
      document.getElementById("recent-comments").innerHTML = BL.empty("최근 댓글을 불러오지 못했습니다.");
    }
  } catch (e) {
    gate.innerHTML = e.status === 403
      ? '<div class="notice notice-error">관리자 권한이 필요합니다.</div>'
      : '<div class="notice notice-error">관리자 데이터를 불러오지 못했습니다.</div>';
  }
});
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
