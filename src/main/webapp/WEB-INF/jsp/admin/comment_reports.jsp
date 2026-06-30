<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<main class="page page-shell">
  <div class="admin-layout">
    <jsp:include page="/WEB-INF/jsp/admin/_sidebar.jsp" />
    <section class="admin-content">
      <div class="section-head">
        <div>
          <p class="eyebrow">신고 처리</p>
          <h1>댓글 신고 관리</h1>
          <p class="muted">회원 신고 사유와 댓글 상태를 확인하고 처리합니다.</p>
        </div>
        <a class="btn btn-ghost" href="<%= request.getContextPath() %>/admin/comments">댓글 관리</a>
      </div>

      <div id="reports-gate"></div>
      <div id="reports-content" style="display:none;">
    <div class="card card-pad" style="margin-bottom:16px;">
      <div class="filters">
        <label class="badge">처리 상태</label>
        <button class="btn btn-ghost is-selected" type="button" data-report-status="all">전체</button>
        <button class="btn btn-ghost" type="button" data-report-status="PENDING">대기</button>
        <button class="btn btn-ghost" type="button" data-report-status="RESOLVED">처리 완료</button>
        <button class="btn btn-ghost" type="button" data-report-status="REJECTED">반려</button>
      </div>
    </div>
    <div id="reports-notice"></div>
    <div id="reports-table" class="table-wrap"></div>
      </div>
    </section>
  </div>
</main>

<script>
document.addEventListener("DOMContentLoaded", async function () {
  const gate = document.getElementById("reports-gate");
  const content = document.getElementById("reports-content");
  const table = document.getElementById("reports-table");
  let status = "all";

  function badge(value) {
    const cls = value === "PENDING" ? "badge-warning" : value === "RESOLVED" ? "badge-active" : "badge";
    const labels = { PENDING: "대기", RESOLVED: "처리 완료", REJECTED: "반려" };
    return `<span class="badge ${cls}">${BL.escape(labels[value] || "전체")}</span>`;
  }

  function reasonLabel(value) {
    const labels = {
      SPAM: "스팸",
      ABUSE: "비방 또는 욕설",
      AD: "광고",
      FALSE_INFO: "잘못된 정보",
      ETC: "기타"
    };
    return labels[value] || "기타";
  }

  function commentStatusLabel(value) {
    const labels = {
      ACTIVE: "게시중",
      DELETED: "삭제됨"
    };
    return labels[value] || "확인 필요";
  }

  function statusActionLabel(value) {
    return value === "RESOLVED" ? "처리 완료" : "반려";
  }

  async function loadReports() {
    table.innerHTML = BL.loading("신고 목록을 불러오는 중입니다.");
    const res = await BL.get("/api/admin/comment-reports?status=" + encodeURIComponent(status) + "&size=100");
    const list = res.data || [];
    if (!list.length) {
      table.innerHTML = BL.empty("신고 내역이 없습니다.");
      return;
    }
    table.innerHTML = `
      <table>
        <thead>
          <tr><th>신고 번호</th><th>상품/댓글</th><th>신고자</th><th>사유</th><th>상태</th><th>처리</th></tr>
        </thead>
        <tbody>
          ${list.map(function (r) {
            const title = BL.escape(r.displayName || r.productName || "");
            return `<tr>
              <td>${r.reportId}</td>
              <td>
                <a class="table-link" href="${BL.url("/products/" + r.productId)}">${title}</a>
                <div>${BL.escape(r.commentContent || "")}</div>
                <div class="muted">댓글 상태: ${BL.escape(commentStatusLabel(r.commentStatus))}</div>
              </td>
              <td>${BL.escape(r.reporterNickname || "")}</td>
              <td><strong>${BL.escape(reasonLabel(r.reasonType))}</strong><div>${BL.escape(r.reasonText || "상세 사유 없음")}</div></td>
              <td>${badge(r.status)}<div class="muted">${BL.escape(r.createdAt || "")}</div></td>
              <td>
                <div class="table-actions">
                  <button class="btn btn-primary" type="button" data-resolve-report="${r.reportId}" data-status="RESOLVED">처리 완료</button>
                  <button class="btn btn-ghost" type="button" data-resolve-report="${r.reportId}" data-status="REJECTED">반려</button>
                  <button class="btn btn-ghost" type="button" data-restore-comment="${r.commentId}">댓글 복구</button>
                </div>
              </td>
            </tr>`;
          }).join("")}
        </tbody>
      </table>`;
  }

  try {
    const me = await BL.me();
    if (!me) {
      gate.innerHTML = BL.requireLoginView("댓글 신고 관리는 로그인이 필요합니다.");
      return;
    }
    if (me.role !== "ADMIN") {
      gate.innerHTML = '<div class="notice notice-error">관리자만 접근할 수 있습니다.</div>';
      return;
    }
    content.style.display = "block";
    await loadReports();
  } catch (e) {
    gate.innerHTML = '<div class="notice notice-error">신고 데이터를 불러오지 못했습니다.</div>';
  }

  document.querySelectorAll("[data-report-status]").forEach(function (btn) {
    btn.addEventListener("click", async function () {
      document.querySelectorAll("[data-report-status]").forEach(function (b) { b.classList.remove("is-selected"); });
      btn.classList.add("is-selected");
      status = btn.dataset.reportStatus;
      await loadReports();
    });
  });

  table.addEventListener("click", async function (event) {
    const resolveButton = event.target.closest("[data-resolve-report]");
    const restoreButton = event.target.closest("[data-restore-comment]");
    try {
      if (resolveButton) {
        if (!confirm("신고를 '" + statusActionLabel(resolveButton.dataset.status) + "' 상태로 변경하시겠습니까?")) return;
        await BL.post("/api/admin/comment-reports/" + resolveButton.dataset.resolveReport + "/resolve", {
          status: resolveButton.dataset.status
        });
        BL.setNotice("reports-notice", "신고를 처리했습니다.", "success");
      } else if (restoreButton) {
        if (!confirm("삭제된 댓글을 복구하시겠습니까?")) return;
        await BL.post("/api/admin/comments/" + restoreButton.dataset.restoreComment + "/restore", {});
        BL.setNotice("reports-notice", "댓글을 복구했습니다.", "success");
      }
      await loadReports();
    } catch (e) {
      BL.setNotice("reports-notice", "신고 처리 작업에 실패했습니다.", "error");
    }
  });
});
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
