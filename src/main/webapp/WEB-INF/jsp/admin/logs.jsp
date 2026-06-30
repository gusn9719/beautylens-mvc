<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<main class="page page-shell">
  <div class="admin-layout">
    <jsp:include page="/WEB-INF/jsp/admin/_sidebar.jsp" />
    <section class="admin-content">
      <div class="section-head">
        <div>
          <p class="eyebrow">운영 추적</p>
          <h1>운영 로그</h1>
          <p class="muted">상품 운영, 댓글 처리, 신고 처리 작업을 추적합니다.</p>
        </div>
        <a class="btn btn-ghost" href="<%= request.getContextPath() %>/admin">대시보드</a>
      </div>

      <div id="logs-gate"></div>
      <div id="logs-content" style="display:none;">
    <div class="card card-pad" style="margin-bottom:16px;">
      <div class="search-row">
        <div class="field compact-field">
          <label for="log-action">액션</label>
          <select id="log-action">
            <option value="all">전체</option>
            <option value="PRODUCT_IS_VISIBLE">상품 노출</option>
            <option value="PRODUCT_EXCLUDE_RECOMMENDATION">추천 제외</option>
            <option value="PRODUCT_IS_FEATURED">메인 노출</option>
            <option value="PRODUCT_FLAGS_SAVE">상품 메모</option>
            <option value="COMMENT_DELETE">댓글 삭제</option>
            <option value="COMMENT_RESTORE">댓글 복구</option>
            <option value="COMMENT_REPORT_RESOLVE">신고 처리</option>
          </select>
        </div>
        <div class="field compact-field">
          <label for="log-target">대상</label>
          <select id="log-target">
            <option value="all">전체</option>
            <option value="PRODUCT">상품</option>
            <option value="COMMENT">댓글</option>
            <option value="COMMENT_REPORT">신고</option>
          </select>
        </div>
        <button id="log-search" class="btn btn-primary" type="button">조회</button>
      </div>
    </div>
    <div id="logs-table" class="table-wrap"></div>
      </div>
    </section>
  </div>
</main>

<script>
document.addEventListener("DOMContentLoaded", async function () {
  const gate = document.getElementById("logs-gate");
  const content = document.getElementById("logs-content");
  const table = document.getElementById("logs-table");

  function query() {
    const params = new URLSearchParams();
    params.set("size", "100");
    params.set("actionType", document.getElementById("log-action").value);
    params.set("targetType", document.getElementById("log-target").value);
    return params.toString();
  }

  function actionLabel(value) {
    const labels = {
      PRODUCT_IS_VISIBLE: "상품 노출 변경",
      PRODUCT_EXCLUDE_RECOMMENDATION: "추천 제외 변경",
      PRODUCT_IS_FEATURED: "메인 노출 변경",
      PRODUCT_FLAGS_SAVE: "상품 메모 저장",
      COMMENT_DELETE: "댓글 삭제",
      COMMENT_RESTORE: "댓글 복구",
      COMMENT_REPORT_RESOLVE: "신고 처리"
    };
    return labels[value] || value || "";
  }

  function targetLabel(value) {
    const labels = { PRODUCT: "상품", COMMENT: "댓글", COMMENT_REPORT: "신고" };
    return labels[value] || value || "";
  }

  function fieldLabel(value) {
    const labels = {
      isVisible: "노출",
      excludeRecommendation: "추천 제외",
      isFeatured: "메인 노출",
      qualityStatus: "품질 상태",
      adminMemo: "운영 메모",
      status: "상태",
      reason: "사유"
    };
    return labels[value] || value;
  }

  function valueLabel(value) {
    const labels = {
      Y: "예",
      N: "아니오",
      ACTIVE: "게시중",
      DELETED: "삭제됨",
      PENDING: "대기",
      RESOLVED: "처리 완료",
      REJECTED: "반려",
      NORMAL: "정상",
      IMAGE_MISSING: "이미지 확인 필요",
      LOW_REVIEW: "리뷰 부족",
      HIGH_CAUTION: "주의 신호 높음",
      NAME_REVIEW_NEEDED: "상품명 확인 필요",
      LINK_BROKEN: "링크 확인 필요",
      ADMIN_HIDE: "관리자 숨김"
    };
    if (value == null || value === "") return "-";
    if (labels[value]) return labels[value];
    if (typeof value === "string" && value.trim().charAt(0) === "{") {
      try {
        const obj = JSON.parse(value);
        return Object.keys(obj).map(function (key) {
          return fieldLabel(key) + ": " + valueLabel(obj[key]);
        }).join(", ");
      } catch (e) {
        return "상세 변경값";
      }
    }
    return String(value);
  }

  async function loadLogs() {
    table.innerHTML = BL.loading("관리자 로그를 불러오는 중입니다.");
    const res = await BL.get("/api/admin/logs?" + query());
    const list = res.data || [];
    if (!list.length) {
      table.innerHTML = BL.empty("관리자 로그가 없습니다.");
      return;
    }
    table.innerHTML = `
      <table>
        <thead><tr><th>로그 번호</th><th>관리자</th><th>액션</th><th>대상</th><th>변경 전</th><th>변경 후</th><th>시간</th></tr></thead>
        <tbody>
          ${list.map(function (l) {
            return `<tr>
              <td>${l.logId}</td>
              <td>${BL.escape(l.adminNickname || l.adminId || "")}</td>
              <td><span class="badge badge-primary">${BL.escape(actionLabel(l.actionType))}</span></td>
              <td>${BL.escape(targetLabel(l.targetType))} #${BL.escape(l.targetId || "")}</td>
              <td>${BL.escape(valueLabel(l.beforeValue))}</td>
              <td>${BL.escape(valueLabel(l.afterValue))}</td>
              <td>${BL.escape(l.createdAt || "")}</td>
            </tr>`;
          }).join("")}
        </tbody>
      </table>`;
  }

  try {
    const me = await BL.me();
    if (!me) {
      gate.innerHTML = BL.requireLoginView("관리자 로그는 로그인이 필요합니다.");
      return;
    }
    if (me.role !== "ADMIN") {
      gate.innerHTML = '<div class="notice notice-error">관리자만 접근할 수 있습니다.</div>';
      return;
    }
    content.style.display = "block";
    await loadLogs();
  } catch (e) {
    gate.innerHTML = '<div class="notice notice-error">관리자 로그를 불러오지 못했습니다.</div>';
  }
  document.getElementById("log-search").addEventListener("click", loadLogs);
});
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
