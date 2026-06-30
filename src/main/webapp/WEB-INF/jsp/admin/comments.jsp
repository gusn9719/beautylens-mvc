<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<main class="page page-shell">
  <div class="admin-layout">
    <jsp:include page="/WEB-INF/jsp/admin/_sidebar.jsp" />
    <section class="admin-content">
      <div class="section-head">
        <div>
          <p class="eyebrow">회원 의견 운영</p>
          <h1>댓글 관리</h1>
          <p class="muted">회원 댓글의 상태와 삭제 사유를 확인하고 필요한 댓글을 삭제 처리합니다.</p>
        </div>
        <a class="btn btn-ghost" href="<%= request.getContextPath() %>/admin/comment-reports">신고 관리</a>
      </div>

      <div id="comments-gate"></div>
      <div id="comments-content" style="display:none;">
    <div class="card card-pad" style="margin-bottom:16px;">
      <div class="search-row" style="margin-bottom:12px;">
        <div class="field search-field">
          <label for="comment-keyword">검색어</label>
          <input id="comment-keyword" placeholder="상품명, 작성자, 댓글 내용을 검색하세요">
        </div>
        <button id="comment-search" class="btn btn-primary" type="button">검색</button>
      </div>
      <div class="filters">
        <label class="badge">상태</label>
        <button class="btn btn-ghost is-selected" type="button" data-status="" aria-pressed="true">전체</button>
        <button class="btn btn-ghost" type="button" data-status="ACTIVE" aria-pressed="false">게시중</button>
        <button class="btn btn-ghost" type="button" data-status="DELETED" aria-pressed="false">삭제됨</button>
      </div>
    </div>
    <div id="admin-comment-notice"></div>
    <div id="admin-comment-table" class="table-wrap"></div>
      </div>
    </section>
  </div>
</main>

<script>
document.addEventListener("DOMContentLoaded", async function () {
  const gate = document.getElementById("comments-gate");
  const content = document.getElementById("comments-content");
  const table = document.getElementById("admin-comment-table");
  const keywordInput = document.getElementById("comment-keyword");
  let status = "";
  let rows = [];

  function badge(value) {
    return value === "ACTIVE"
      ? '<span class="badge badge-active">게시중</span>'
      : '<span class="badge badge-deleted">삭제됨</span>';
  }

  function renderRows(list) {
    const keyword = keywordInput.value.trim().toLowerCase();
    const filtered = keyword
      ? list.filter(function (c) {
          return [c.productName, c.displayName, c.nickname, c.loginId, c.content]
            .join(" ")
            .toLowerCase()
            .indexOf(keyword) >= 0;
        })
      : list;
    if (!filtered.length) {
      table.innerHTML = BL.empty("댓글이 없습니다.");
      return;
    }
    table.innerHTML = `
      <table>
        <thead>
          <tr>
            <th>댓글 번호</th><th>상품명</th><th>작성자</th><th>내용</th><th>상태</th><th>작성일</th><th>삭제일</th><th>삭제 사유</th><th>처리</th>
          </tr>
        </thead>
        <tbody>
          ${filtered.map(function (c) {
            const productTitle = BL.escape(c.displayName || c.productName || "");
            return `<tr data-row-comment="${c.commentId}">
              <td>${c.commentId}</td>
              <td><a class="table-link" href="${BL.url("/products/" + c.productId)}">${productTitle}</a></td>
              <td>${BL.escape(c.nickname || "")}</td>
              <td>${BL.escape(c.content || "")}</td>
              <td>${badge(c.status)}</td>
              <td>${BL.escape(c.createdAt || "")}</td>
              <td>${BL.escape(c.deletedAt || "")}</td>
              <td>${BL.escape(c.deleteReason || "")}</td>
              <td>${c.status === "ACTIVE"
                ? `<button class="btn btn-danger" type="button" data-admin-delete="${c.commentId}">삭제</button>`
                : `<button class="btn btn-ghost" type="button" data-admin-restore="${c.commentId}">복구</button>`}</td>
            </tr>`;
          }).join("")}
        </tbody>
      </table>`;
  }

  async function loadComments() {
    table.innerHTML = BL.loading("댓글 목록을 불러오는 중입니다.");
    const query = status ? "?status=" + encodeURIComponent(status) + "&size=100" : "?size=100";
    const res = await BL.get("/api/admin/comments" + query);
    rows = res.data || [];
    renderRows(rows);
  }

  try {
    const me = await BL.me();
    if (!me) {
      gate.innerHTML = BL.requireLoginView("댓글 관리는 로그인이 필요합니다.");
      return;
    }
    if (me.role !== "ADMIN") {
      gate.innerHTML = '<div class="notice notice-error"><strong>관리자만 접근할 수 있습니다.</strong><div style="margin-top:12px;"><a class="btn btn-ghost" href="' + BL.url("/") + '">홈으로 이동</a></div></div>';
      return;
    }
    content.style.display = "block";
    await loadComments();
  } catch (e) {
    gate.innerHTML = '<div class="notice notice-error">댓글 관리 데이터를 불러오지 못했습니다.</div>';
  }

  document.querySelectorAll("[data-status]").forEach(function (btn) {
    btn.addEventListener("click", async function () {
      document.querySelectorAll("[data-status]").forEach(function (b) { b.classList.remove("is-selected"); });
      document.querySelectorAll("[data-status]").forEach(function (b) { b.setAttribute("aria-pressed", "false"); });
      btn.classList.add("is-selected");
      btn.setAttribute("aria-pressed", "true");
      status = btn.dataset.status;
      await loadComments();
    });
  });

  document.getElementById("comment-search").addEventListener("click", function () {
    renderRows(rows);
  });

  keywordInput.addEventListener("keydown", function (event) {
    if (event.key === "Enter") renderRows(rows);
  });

  table.addEventListener("click", async function (event) {
    const button = event.target.closest("[data-admin-delete]");
    const restoreButton = event.target.closest("[data-admin-restore]");
    if (!button && !restoreButton) return;
    try {
      if (button) {
        if (!confirm("이 댓글을 삭제 처리하시겠습니까?")) return;
        await BL.del("/api/admin/comments/" + button.dataset.adminDelete);
        BL.setNotice("admin-comment-notice", "댓글을 삭제 처리했습니다.", "success");
      } else if (restoreButton) {
        if (!confirm("이 댓글을 복구하시겠습니까?")) return;
        await BL.post("/api/admin/comments/" + restoreButton.dataset.adminRestore + "/restore", {});
        BL.setNotice("admin-comment-notice", "댓글을 복구했습니다.", "success");
      }
      await loadComments();
    } catch (e) {
      BL.setNotice("admin-comment-notice", "댓글 처리에 실패했습니다.", "error");
    }
  });
});
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
