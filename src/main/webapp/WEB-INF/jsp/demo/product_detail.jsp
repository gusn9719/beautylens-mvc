<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<%
  Integer productId = (Integer) request.getAttribute("productId");
%>
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<main class="page">
  <div id="detail-root" data-product-id="<%= productId %>">
    <div class="loading">상품 정보를 불러오는 중입니다.</div>
  </div>
</main>

<script>
document.addEventListener("DOMContentLoaded", async function () {
  const root = document.getElementById("detail-root");
  const productId = root.dataset.productId;
  let me = null;
  let product = null;

  function canDelete(comment) {
    return me && (me.role === "ADMIN" || Number(me.memberId) === Number(comment.memberId));
  }

  function canEdit(comment) {
    return me && Number(me.memberId) === Number(comment.memberId) && comment.status !== "DELETED";
  }

  function reviewItem(r, type) {
    return `
      <article class="review-item">
        <div class="meta-row">
          <span class="badge ${type === "negative" ? "badge-warning" : "badge-primary"}">${type === "negative" ? "주의 리뷰" : "긍정 리뷰"}</span>
          <span>평점 ${BL.score(r.rating)}</span>
          <span>${BL.escape(r.reviewDate || "")}</span>
        </div>
        <p class="review-text">${BL.escape(r.reviewText || "")}</p>
      </article>`;
  }

  async function loadReviewSection(path, targetId, type, emptyMessage) {
    const area = document.getElementById(targetId);
    if (!area) return;
    area.innerHTML = BL.loading("리뷰를 불러오는 중입니다.");
    try {
      const res = await BL.get(path);
      const list = Array.isArray(res.data) ? res.data : [];
      area.innerHTML = list.length
        ? list.slice(0, 5).map(function (r) { return reviewItem(r, type); }).join("")
        : BL.empty(emptyMessage);
    } catch (e) {
      area.innerHTML = BL.empty("리뷰를 불러오지 못했습니다. 잠시 후 다시 확인해 주세요.");
    }
  }

  async function loadComments() {
    const area = document.getElementById("comment-list");
    area.innerHTML = BL.loading("댓글을 불러오는 중입니다.");
    try {
      const res = await BL.get("/api/products/" + productId + "/comments");
      const comments = res.data || [];
      area.innerHTML = comments.length ? comments.map(function (c) {
        return `
          <article class="comment-item" data-comment-id="${c.commentId}">
            <div class="meta-row">
              <strong>${BL.escape(c.nickname || "회원")}</strong>
              <span>${BL.escape(c.createdAt || "")}</span>
              ${c.updatedAt ? `<span>수정됨 ${BL.escape(c.updatedAt)}</span>` : ""}
              ${me ? `<button class="btn btn-ghost" type="button" data-report-comment="${c.commentId}">신고</button>` : ""}
              ${canEdit(c) ? `<button class="btn btn-ghost" type="button" data-edit-comment="${c.commentId}">수정</button>` : ""}
              ${canDelete(c) ? `<button class="btn btn-danger" type="button" data-delete-comment="${c.commentId}">삭제</button>` : ""}
            </div>
            <p class="comment-text">${BL.escape(c.content || "")}</p>
          </article>`;
      }).join("") : BL.empty("아직 회원 댓글이 없습니다.");
    } catch (e) {
      area.innerHTML = BL.empty("댓글을 불러오지 못했습니다.");
    }
  }

  function commentForm() {
    if (!me) return BL.requireLoginView("댓글 작성은 로그인이 필요합니다.");
    return `
      <form id="comment-form" class="comment-form-panel">
        <div id="comment-notice"></div>
        <div class="field">
          <label for="comment-content">다른 회원에게 보이는 자유 의견</label>
          <textarea id="comment-content" maxlength="1000" rows="3" required placeholder="상품 선택에 도움이 되는 의견이나 질문을 남겨 주세요."></textarea>
        </div>
        <button class="btn btn-primary" type="submit">의견 작성</button>
      </form>`;
  }

  function ratingForm() {
    if (!me) return BL.requireLoginView("사이트 내부 평가는 로그인이 필요합니다.");
    return `
      <form id="rating-form" class="section-card">
        <div id="rating-notice"></div>
        <p class="muted">별점, 자극 여부, 재구매 의사는 댓글과 별도로 BeautyLens 내부 만족도 요약에 사용됩니다.</p>
        <div class="rating-grid">
          <div class="field">
            <label for="site-rating">사이트 별점</label>
            <select id="site-rating" required>
              <option value="5">5점 - 매우 만족</option>
              <option value="4">4점 - 만족</option>
              <option value="3">3점 - 보통</option>
              <option value="2">2점 - 아쉬움</option>
              <option value="1">1점 - 비추천</option>
            </select>
          </div>
          <div class="field">
            <label for="irritation-yn">자극 여부</label>
            <select id="irritation-yn">
              <option value="">선택 안 함</option>
              <option value="N">자극 없었음</option>
              <option value="Y">자극 있었음</option>
            </select>
          </div>
          <div class="field">
            <label for="repurchase-yn">재구매 의사</label>
            <select id="repurchase-yn">
              <option value="">선택 안 함</option>
              <option value="Y">재구매 의사 있음</option>
              <option value="N">재구매 의사 없음</option>
            </select>
          </div>
        </div>
        <div class="field">
          <label for="rating-text">짧은 평가 메모 선택 입력</label>
          <textarea id="rating-text" class="compact-textarea" maxlength="500" rows="2" placeholder="예: 산뜻하지만 겨울에는 보습이 조금 부족했어요."></textarea>
        </div>
        <div class="action-row">
          <button class="btn btn-primary" type="submit">평가 저장</button>
          <button id="rating-delete-btn" class="btn btn-ghost" type="button" style="display:none;">내 평가 삭제</button>
        </div>
      </form>`;
  }

  function metricCard(label, value, help) {
    return `
      <article class="kv">
        <div class="k">${BL.escape(label)}</div>
        <div class="v">${BL.escape(value == null || value === "" ? "-" : value)}</div>
        ${help ? `<p class="muted">${BL.escape(help)}</p>` : ""}
      </article>`;
  }

  function recommendationReason(product) {
    const skin = product.baseSkinType ? BL.skinLabel(product.baseSkinType) : "선택한 피부 타입";
    const tag = product.topConcernTags || product.topNeedTags || "";
    if (tag) return skin + " 기준 리뷰 신호에서 " + tag + " 관련 언급이 확인됩니다.";
    return skin + " 기준 외부 리뷰 점수와 주의 신호를 함께 반영했습니다.";
  }

  function externalReviewSummary(product) {
    const total = product.totalReviewCount || 0;
    if (!total) return "외부 리뷰 수가 부족해 분석 근거가 제한적입니다.";
    return "외부 리뷰 " + total + "건을 기준으로 긍정 포인트와 주의 포인트를 나누어 보여줍니다.";
  }

  function siteEvaluationSummary(product) {
    const count = product.siteRatingCount || 0;
    if (!count) {
      return `
        <div class="empty-state">아직 사이트 사용자 평가가 부족합니다. 평가가 쌓이면 별점, 자극 여부, 재구매 의사 요약에 반영됩니다.</div>`;
    }
    return `
      <div class="kv-grid">
        ${metricCard("내부 평균 별점", (product.siteRatingAvg || "-") + "점", "BeautyLens 회원이 남긴 별점 평균입니다.")}
        ${metricCard("평가 수", count + "건", "상품 상세에서 남긴 평가만 집계합니다.")}
        ${metricCard("자극 여부", "평가 수집 중", "자극 관련 응답이 더 쌓이면 요약됩니다.")}
        ${metricCard("재구매 의사", "평가 수집 중", "재구매 관련 응답이 더 쌓이면 요약됩니다.")}
      </div>`;
  }

  function recommendationFeedback(product) {
    if (!me) return "";
    return `
      <details class="recommend-feedback detail-feedback">
        <summary>이 추천이 맞지 않나요?</summary>
        <div class="feedback-actions">
          <button class="btn btn-ghost" type="button" data-rec-feedback="NOT_INTERESTED" data-product-id="${BL.escape(product.productId)}">추천 숨기기</button>
          <button class="btn btn-ghost" type="button" data-rec-feedback="LIKE" data-product-id="${BL.escape(product.productId)}">도움 됐어요</button>
          <button class="btn btn-ghost" type="button" data-rec-feedback="DISLIKE" data-product-id="${BL.escape(product.productId)}">맞지 않아요</button>
        </div>
      </details>`;
  }

  async function loadMyRating() {
    if (!me) return;
    try {
      const res = await BL.get("/api/products/" + productId + "/rating");
      const rating = res.data;
      if (!rating) return;
      document.getElementById("site-rating").value = String(rating.rating || 5);
      document.getElementById("irritation-yn").value = rating.irritationYn || "";
      document.getElementById("repurchase-yn").value = rating.repurchaseYn || "";
      document.getElementById("rating-text").value = rating.reviewText || "";
      const deleteButton = document.getElementById("rating-delete-btn");
      if (deleteButton) deleteButton.style.display = "inline-flex";
      BL.setNotice("rating-notice", "이미 남긴 사이트 평가를 불러왔습니다. 수정 후 다시 저장할 수 있습니다.", "success");
    } catch (e) {
      if (e.status !== 401) BL.setNotice("rating-notice", "기존 평가를 불러오지 못했습니다.", "error");
    }
  }

  try {
    me = await BL.me();
    const productRes = await BL.get("/api/products/" + productId);
    product = productRes.data;
    const title = BL.escape(product.displayName || product.productName || "상품");
    root.innerHTML = `
      <section class="detail-summary">
        <div class="card detail-image">${BL.imageHtml(product, "product-image")}</div>
        <div class="section-card detail-summary-card">
          <div class="product-meta"><span>${BL.escape(product.brand || "브랜드 정보 없음")}</span><span>${BL.platformLabel(product.platform)}</span></div>
          <h1>${title}</h1>
          <p class="muted">${BL.escape(product.category || "카테고리 정보 준비 중")}</p>
          <div class="product-card-badges" style="margin-bottom:16px;">
            <span class="badge badge-primary">추천 점수 ${BL.score(product.recommendationScore)}</span>
            ${product.serviceScore != null ? `<span class="badge badge-active">서비스 반응 ${BL.score(product.serviceScore)}</span>` : ""}
            <span class="badge ${BL.cautionClass(product.cautionLevel)}">${BL.cautionLabel(product.cautionLevel)}</span>
          </div>
          <div class="inline-actions">
            <button id="detail-favorite" class="btn btn-primary" type="button" data-favorite-product="${BL.escape(product.productId)}">찜하기</button>
            ${product.productUrl ? `<a class="btn btn-ghost" href="${BL.escape(product.productUrl)}" target="_blank" rel="noopener">외부 상품 페이지</a>` : ""}
          </div>
        </div>
      </section>

      <section class="section section-card">
        <div class="section-head compact-head">
          <div>
            <h2>추천 근거</h2>
            <p class="muted">추천 점수는 외부 리뷰 분석을 중심으로, 사이트 내부 반응은 보조 지표로 구분해 봅니다.</p>
          </div>
        </div>
        <div class="kv-grid">
          ${metricCard("피부 타입 기준", product.baseSkinType ? BL.skinLabel(product.baseSkinType) : "전체 피부", recommendationReason(product))}
          ${metricCard("긍정 신호", product.topNeedTags || product.topConcernTags || "리뷰 신호 분석 중", "외부 리뷰에서 반복적으로 언급된 장점입니다.")}
          ${metricCard("주의 신호", BL.cautionLabel(product.cautionLevel), "주의 리뷰 비율과 근거 부족 여부를 함께 봅니다.")}
          ${metricCard("내부 반응", product.siteRatingCount ? (product.siteRatingAvg || "-") + "점, " + product.siteRatingCount + "건" : "아직 부족", "BeautyLens 회원 평가가 쌓이면 더 신뢰도 있게 반영됩니다.")}
        </div>
        ${recommendationFeedback(product)}
      </section>

      <section class="section section-card">
        <div class="section-head compact-head">
          <div>
            <h2>외부 리뷰 분석</h2>
            <p class="muted">${BL.escape(externalReviewSummary(product))}</p>
          </div>
        </div>
        <div class="grid grid-2">
          <div><h3>긍정 포인트</h3><div id="positive-reviews" class="review-list">${BL.loading("긍정 리뷰를 불러오는 중입니다.")}</div></div>
          <div><h3>주의 포인트</h3><div id="negative-reviews" class="review-list">${BL.loading("주의 리뷰를 불러오는 중입니다.")}</div></div>
        </div>
      </section>

      <section class="section section-card">
        <div class="section-head compact-head">
          <div>
            <h2>사이트 사용자 평가</h2>
            <p class="muted">BeautyLens 회원이 상품 상세에서 남긴 정량 평가 요약입니다. 자유 댓글과 분리해 봅니다.</p>
          </div>
        </div>
        ${siteEvaluationSummary(product)}
      </section>

      <section class="section">
        <div class="section-head compact-head">
          <div>
            <h2>내 평가 남기기</h2>
            <p class="muted">상품 정보를 확인한 뒤 별점, 자극 여부, 재구매 의사를 남겨 주세요.</p>
          </div>
        </div>
        ${ratingForm()}
      </section>

      <section class="section section-card">
        <div class="section-head compact-head">
          <div>
            <h2>회원 의견</h2>
            <p class="muted">자유롭게 남기는 댓글입니다. 별점과 자극 여부 평가는 위 평가 영역에서 별도로 관리됩니다.</p>
          </div>
        </div>
        <div id="comment-list" class="comment-list" style="margin-top:14px;"></div>
        ${commentForm()}
      </section>`;

    await Promise.all([
      loadReviewSection("/api/products/" + productId + "/reviews/positive", "positive-reviews", "positive", "아직 긍정 리뷰가 충분하지 않습니다."),
      loadReviewSection("/api/products/" + productId + "/reviews/negative", "negative-reviews", "negative", "아직 주의 리뷰가 충분하지 않습니다.")
    ]);
    if (me) {
      try {
        await BL.post("/api/products/" + productId + "/events", { eventType: "DETAIL_VIEW" });
      } catch (e) {}
    }
    await loadMyRating();
    await loadComments();

    const detailFavorite = document.getElementById("detail-favorite");
    if (detailFavorite) {
      await BL.syncFavoriteButtons(root);
      detailFavorite.addEventListener("click", async function (event) {
        event.preventDefault();
        await BL.handleFavoriteClick(detailFavorite);
      });
    }

    root.querySelectorAll("[data-rec-feedback]").forEach(function (button) {
      button.addEventListener("click", async function (event) {
        event.preventDefault();
        await BL.handleRecommendationFeedbackClick(button);
      });
    });

    const ratingFormEl = document.getElementById("rating-form");
    if (ratingFormEl) {
      ratingFormEl.addEventListener("submit", async function (event) {
        event.preventDefault();
        const body = {
          rating: Number(document.getElementById("site-rating").value),
          irritationYn: document.getElementById("irritation-yn").value,
          repurchaseYn: document.getElementById("repurchase-yn").value,
          reviewText: document.getElementById("rating-text").value.trim()
        };
        try {
          await BL.post("/api/products/" + productId + "/rating", body);
          BL.setNotice("rating-notice", "사이트 평가가 저장되었습니다.", "success");
          const deleteButton = document.getElementById("rating-delete-btn");
          if (deleteButton) deleteButton.style.display = "inline-flex";
        } catch (e) {
          BL.setNotice("rating-notice", e.status === 401 ? "로그인이 필요합니다." : "사이트 평가 저장에 실패했습니다.", "error");
        }
      });
    }

    const ratingDeleteBtn = document.getElementById("rating-delete-btn");
    if (ratingDeleteBtn) {
      ratingDeleteBtn.addEventListener("click", async function () {
        if (!confirm("내 사이트 평가를 삭제하시겠습니까?")) return;
        try {
          await BL.del("/api/products/" + productId + "/rating");
          document.getElementById("site-rating").value = "5";
          document.getElementById("irritation-yn").value = "";
          document.getElementById("repurchase-yn").value = "";
          document.getElementById("rating-text").value = "";
          ratingDeleteBtn.style.display = "none";
          BL.setNotice("rating-notice", "내 평가를 삭제했습니다.", "success");
        } catch (e) {
          BL.setNotice("rating-notice", e.status === 401 ? "로그인이 필요합니다." : "평가 삭제에 실패했습니다.", "error");
        }
      });
    }

    const form = document.getElementById("comment-form");
    if (form) {
      form.addEventListener("submit", async function (event) {
        event.preventDefault();
        const content = document.getElementById("comment-content").value.trim();
        try {
          await BL.post("/api/products/" + productId + "/comments", { content: content });
          document.getElementById("comment-content").value = "";
          BL.setNotice("comment-notice", "댓글이 등록되었습니다.", "success");
          await loadComments();
        } catch (e) {
          BL.setNotice("comment-notice", "댓글 등록에 실패했습니다.", "error");
        }
      });
    }

    document.getElementById("comment-list").addEventListener("click", async function (event) {
      const button = event.target.closest("[data-delete-comment]");
      const reportButton = event.target.closest("[data-report-comment]");
      const editButton = event.target.closest("[data-edit-comment]");
      const saveEditButton = event.target.closest("[data-save-comment-edit]");
      const cancelEditButton = event.target.closest("[data-cancel-comment-edit]");
      if (!button && !reportButton && !editButton && !saveEditButton && !cancelEditButton) return;
      try {
        if (button) {
          if (!confirm("이 의견을 삭제하시겠습니까?")) return;
          await BL.del("/api/comments/" + button.dataset.deleteComment);
          await loadComments();
        } else if (reportButton) {
          if (!confirm("이 댓글을 신고하시겠습니까?")) return;
          await BL.post("/api/comments/" + reportButton.dataset.reportComment + "/report", {
            reasonType: "ETC",
            reasonText: "상품 상세에서 신고"
          });
          alert("신고가 접수되었습니다.");
        } else if (editButton) {
          const article = editButton.closest(".comment-item");
          if (!article || article.querySelector(".comment-edit-panel")) return;
          const text = article.querySelector(".comment-text");
          const current = text ? text.textContent.trim() : "";
          const panel = document.createElement("div");
          panel.className = "comment-edit-panel";
          panel.innerHTML = `
            <textarea maxlength="1000" rows="3" aria-label="의견 수정">${BL.escape(current)}</textarea>
            <div class="action-row">
              <button class="btn btn-primary" type="button" data-save-comment-edit="${editButton.dataset.editComment}">수정 저장</button>
              <button class="btn btn-ghost" type="button" data-cancel-comment-edit>취소</button>
            </div>`;
          if (text) text.style.display = "none";
          article.appendChild(panel);
        } else if (cancelEditButton) {
          const article = cancelEditButton.closest(".comment-item");
          const panel = cancelEditButton.closest(".comment-edit-panel");
          const text = article && article.querySelector(".comment-text");
          if (panel) panel.remove();
          if (text) text.style.display = "";
        } else if (saveEditButton) {
          const panel = saveEditButton.closest(".comment-edit-panel");
          const textarea = panel && panel.querySelector("textarea");
          const content = textarea ? textarea.value.trim() : "";
          if (!content) {
            alert("수정할 의견을 입력해 주세요.");
            return;
          }
          await BL.put("/api/comments/" + saveEditButton.dataset.saveCommentEdit, { content: content });
          await loadComments();
        }
      } catch (e) {
        alert("댓글 작업을 처리하지 못했습니다.");
      }
    });
  } catch (e) {
    root.innerHTML = BL.empty("상품 정보를 불러오지 못했습니다.");
  }
});
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
