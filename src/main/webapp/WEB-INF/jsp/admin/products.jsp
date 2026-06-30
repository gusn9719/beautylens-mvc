<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<main class="page page-shell">
  <div class="admin-layout">
    <jsp:include page="/WEB-INF/jsp/admin/_sidebar.jsp" />
    <section class="admin-content">
      <div class="section-head">
        <div>
          <p class="eyebrow">상품 운영</p>
          <h1>상품 관리</h1>
          <p class="muted">원본 상품 데이터는 삭제하지 않고 노출, 추천 제외, 품질 상태, 운영 메모만 관리합니다.</p>
        </div>
        <a class="btn btn-ghost" href="<%= request.getContextPath() %>/admin">대시보드</a>
      </div>

      <div id="admin-products-gate"></div>
      <div id="admin-products-content" style="display:none;">
    <div class="card card-pad product-browser-panel">
      <div class="search-row">
        <div class="field search-field">
          <label for="admin-product-keyword">검색어</label>
          <input id="admin-product-keyword" placeholder="상품명 또는 브랜드">
        </div>
        <div class="field compact-field">
          <label for="admin-platform">플랫폼</label>
          <select id="admin-platform">
            <option value="all">전체</option>
            <option value="oliveyoung">Olive Young</option>
            <option value="musinsa">Musinsa</option>
          </select>
        </div>
        <div class="field compact-field">
          <label for="admin-visible">노출</label>
          <select id="admin-visible">
            <option value="all">전체</option>
            <option value="Y">노출</option>
            <option value="N">숨김</option>
          </select>
        </div>
        <div class="field compact-field">
          <label for="admin-exclude">추천 제외</label>
          <select id="admin-exclude">
            <option value="all">전체</option>
            <option value="Y">제외</option>
            <option value="N">포함</option>
          </select>
        </div>
        <button id="admin-product-search" class="btn btn-primary" type="button">조회</button>
      </div>
      <div class="filters">
        <button id="admin-image-missing" class="btn btn-ghost" type="button" aria-pressed="false">이미지 없음</button>
        <select id="admin-quality" class="field-select">
          <option value="all">품질 상태 전체</option>
          <option value="NORMAL">정상</option>
          <option value="IMAGE_MISSING">이미지 확인 필요</option>
          <option value="LOW_REVIEW">리뷰 부족</option>
          <option value="HIGH_CAUTION">주의 신호 높음</option>
          <option value="NAME_REVIEW_NEEDED">상품명 확인 필요</option>
          <option value="LINK_BROKEN">링크 확인 필요</option>
        </select>
      </div>
    </div>

    <div id="admin-products-notice" style="margin-top:14px;"></div>
    <div id="admin-products-table" class="table-wrap section"></div>
      </div>
    </section>
  </div>
</main>

<script>
document.addEventListener("DOMContentLoaded", async function () {
  const gate = document.getElementById("admin-products-gate");
  const content = document.getElementById("admin-products-content");
  const table = document.getElementById("admin-products-table");
  let rows = [];
  let imageMissing = false;

  function ynBadge(value, yesText, noText) {
    return value === "Y"
      ? `<span class="badge badge-primary">${yesText}</span>`
      : `<span class="badge">${noText}</span>`;
  }

  function qualityLabel(value) {
    const labels = {
      NORMAL: "정상",
      IMAGE_MISSING: "이미지 확인 필요",
      LOW_REVIEW: "리뷰 부족",
      HIGH_CAUTION: "주의 신호 높음",
      NAME_REVIEW_NEEDED: "상품명 확인 필요",
      LINK_BROKEN: "링크 확인 필요"
    };
    return labels[value] || "정상";
  }

  function query() {
    const params = new URLSearchParams();
    params.set("size", "100");
    params.set("platform", document.getElementById("admin-platform").value);
    params.set("visible", document.getElementById("admin-visible").value);
    params.set("excludeRecommendation", document.getElementById("admin-exclude").value);
    params.set("qualityStatus", document.getElementById("admin-quality").value);
    params.set("imageMissing", imageMissing ? "true" : "false");
    const keyword = document.getElementById("admin-product-keyword").value.trim();
    if (keyword) params.set("keyword", keyword);
    return params.toString();
  }

  function render() {
    if (!rows.length) {
      table.innerHTML = BL.empty("조건에 맞는 상품이 없습니다.");
      return;
    }
    table.innerHTML = `
      <table>
        <thead>
          <tr>
            <th>상품 번호</th><th>상품</th><th>운영 상태</th><th>사이트 반응</th><th>품질/메모</th><th>작업</th>
          </tr>
        </thead>
        <tbody>
          ${rows.map(function (p) {
            const title = BL.escape(p.displayName || p.productName || "");
            return `<tr data-product-row="${p.productId}">
              <td>${p.productId}</td>
              <td>
                <a class="table-link" href="${BL.url("/products/" + p.productId)}">${title}</a>
                <div class="muted">${BL.escape(p.brand || "")} · ${BL.platformLabel(p.platform)} · 이미지 ${p.imageUrl ? "있음" : "없음"}</div>
              </td>
              <td>
                ${ynBadge(p.isVisible, "노출", "숨김")}
                ${ynBadge(p.excludeRecommendation, "추천 제외", "추천 포함")}
                ${ynBadge(p.isFeatured, "메인 노출", "일반")}
              </td>
              <td>
                <div>찜 ${p.favoriteCount || 0}</div>
                <div>내부 별점 ${p.siteRatingAvg || "-"} (${p.siteRatingCount || 0})</div>
                <div>조회 ${p.viewCount || 0} · 댓글 ${p.commentCount || 0}</div>
              </td>
              <td>
                <select data-quality="${p.productId}">
                  ${["NORMAL","IMAGE_MISSING","LOW_REVIEW","HIGH_CAUTION","NAME_REVIEW_NEEDED","LINK_BROKEN"].map(function (q) {
                    return `<option value="${q}" ${p.qualityStatus === q ? "selected" : ""}>${qualityLabel(q)}</option>`;
                  }).join("")}
                </select>
                <textarea data-memo="${p.productId}" placeholder="운영 메모">${BL.escape(p.adminMemo || "")}</textarea>
              </td>
              <td>
                <div class="table-actions">
                  <button class="btn btn-ghost" type="button" data-action="${p.isVisible === "N" ? "restore" : "hide"}" data-product-id="${p.productId}">${p.isVisible === "N" ? "복구" : "숨김"}</button>
                  <button class="btn btn-ghost" type="button" data-action="${p.excludeRecommendation === "Y" ? "include-recommendation" : "exclude-recommendation"}" data-product-id="${p.productId}">${p.excludeRecommendation === "Y" ? "추천 포함" : "추천 제외"}</button>
                  <button class="btn btn-ghost" type="button" data-action="${p.isFeatured === "Y" ? "unfeature" : "feature"}" data-product-id="${p.productId}">${p.isFeatured === "Y" ? "메인 해제" : "메인 노출"}</button>
                  <button class="btn btn-primary" type="button" data-save-flags="${p.productId}">메모 저장</button>
                </div>
              </td>
            </tr>`;
          }).join("")}
        </tbody>
      </table>`;
  }

  async function loadProducts() {
    table.innerHTML = BL.loading("관리자 상품 목록을 불러오는 중입니다.");
    const res = await BL.get("/api/admin/products?" + query());
    rows = res.data || [];
    render();
  }

  function actionLabel(action) {
    const labels = {
      hide: "상품을 숨김 처리",
      restore: "상품을 복구",
      "exclude-recommendation": "추천에서 제외",
      "include-recommendation": "추천에 다시 포함",
      feature: "메인 노출로 지정",
      unfeature: "메인 노출을 해제"
    };
    return labels[action] || "상품 상태를 변경";
  }

  try {
    const me = await BL.me();
    if (!me) {
      gate.innerHTML = BL.requireLoginView("관리자 상품 관리는 로그인이 필요합니다.");
      return;
    }
    if (me.role !== "ADMIN") {
      gate.innerHTML = '<div class="notice notice-error">관리자만 접근할 수 있습니다.</div>';
      return;
    }
    content.style.display = "block";
    await loadProducts();
  } catch (e) {
    gate.innerHTML = '<div class="notice notice-error">관리자 상품 데이터를 불러오지 못했습니다.</div>';
  }

  document.getElementById("admin-product-search").addEventListener("click", loadProducts);
  document.getElementById("admin-image-missing").addEventListener("click", function () {
    imageMissing = !imageMissing;
    this.classList.toggle("is-selected", imageMissing);
    this.setAttribute("aria-pressed", imageMissing ? "true" : "false");
    loadProducts();
  });
  ["admin-platform", "admin-visible", "admin-exclude", "admin-quality"].forEach(function (id) {
    document.getElementById(id).addEventListener("change", loadProducts);
  });

  table.addEventListener("click", async function (event) {
    const actionButton = event.target.closest("[data-action]");
    const saveButton = event.target.closest("[data-save-flags]");
    try {
      if (actionButton) {
        const action = actionButton.dataset.action;
        const productId = actionButton.dataset.productId;
        if (!confirm(actionLabel(action) + "하시겠습니까?")) return;
        const body = action === "hide" ? { reason: "ADMIN_HIDE" } : {};
        await BL.post("/api/admin/products/" + productId + "/" + action, body);
        BL.setNotice("admin-products-notice", "상품 운영 상태를 변경했습니다.", "success");
        await loadProducts();
      } else if (saveButton) {
        const productId = saveButton.dataset.saveFlags;
        if (!confirm("품질 상태와 운영 메모를 저장하시겠습니까?")) return;
        const row = rows.find(function (p) { return Number(p.productId) === Number(productId); }) || {};
        const body = {
          isVisible: row.isVisible || "Y",
          excludeRecommendation: row.excludeRecommendation || "N",
          isFeatured: row.isFeatured || "N",
          qualityStatus: document.querySelector("[data-quality='" + productId + "']").value,
          adminMemo: document.querySelector("[data-memo='" + productId + "']").value
        };
        await BL.put("/api/admin/products/" + productId + "/flags", body);
        BL.setNotice("admin-products-notice", "운영 메모와 품질 상태를 저장했습니다.", "success");
        await loadProducts();
      }
    } catch (e) {
      BL.setNotice("admin-products-notice", "관리자 작업에 실패했습니다.", "error");
    }
  });
});
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
