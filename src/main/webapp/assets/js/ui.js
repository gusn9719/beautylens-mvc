(function (window) {
  const BL = window.BL || {};

  BL.escape = function (value) {
    return String(value == null ? "" : value)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#039;");
  };

  BL.score = function (value) {
    if (value == null || value === "") return "-";
    const n = Number(value);
    return Number.isFinite(n) ? n.toFixed(1) : "-";
  };

  BL.skinLabel = function (value) {
    const labels = {
      dry: "건성",
      oily: "지성",
      combination: "복합성",
      sensitive: "민감성",
      normal: "중성",
      "건성": "건성",
      "지성": "지성",
      "복합성": "복합성",
      "민감성": "민감성",
      "중성": "중성"
    };
    return labels[value] || value || "-";
  };

  BL.platformLabel = function (value) {
    if (value === "oliveyoung") return "Olive Young";
    if (value === "musinsa") return "Musinsa";
    return value || "-";
  };

  BL.cautionLabel = function (value) {
    if (value === "normal") return "안정적";
    if (value === "moderate_negative_signal") return "리뷰 확인 권장";
    if (value === "high_negative_signal") return "주의 리뷰 있음";
    if (value === "insufficient_evidence") return "근거 부족";
    return value || "-";
  };

  BL.cautionClass = function (value) {
    if (value === "high_negative_signal") return "badge-danger";
    if (value === "moderate_negative_signal" || value === "insufficient_evidence") return "badge-warning";
    return "badge-primary";
  };

  BL.ynLabel = function (value, yesText, noText, emptyText) {
    if (value === "Y") return yesText || "예";
    if (value === "N") return noText || "아니오";
    return emptyText || "선택 안 함";
  };

  BL.currentInternalPath = function () {
    const context = BL.contextPath();
    const path = window.location.pathname.indexOf(context) === 0
      ? window.location.pathname.slice(context.length)
      : window.location.pathname;
    return (path || "/") + window.location.search + window.location.hash;
  };

  BL.safeRedirectPath = function (value, fallback) {
    const target = value || fallback || "";
    if (!target) return "";
    if (/[\u0000-\u001F\u007F]/.test(target)) return "";
    if (target.indexOf("\\") >= 0) return "";
    if (target.indexOf("//") === 0) return "";
    if (target.indexOf("/") !== 0) return "";
    if (/^\/\s*javascript:/i.test(target)) return "";
    if (/^\/\s*https?:/i.test(target)) return "";
    return target;
  };

  BL.loginPath = function (redirectPath) {
    const redirect = BL.safeRedirectPath(redirectPath || BL.currentInternalPath(), "");
    return redirect ? "/login?redirect=" + encodeURIComponent(redirect) : "/login";
  };

  BL.imageHtml = function (item, className) {
    const name = BL.escape(item.displayName || item.productName || "BeautyLens");
    if (item.imageUrl) {
      return `<img class="${className || "product-image"}" src="${BL.escape(item.imageUrl)}" alt="${name}" onerror="this.outerHTML='<div class=&quot;image-placeholder&quot;>이미지 준비 중</div>'">`;
    }
    return `<div class="image-placeholder">이미지 준비 중</div>`;
  };

  BL.productCard = function (item, options) {
    const opts = options || {};
    const detailUrl = BL.url("/products/" + item.productId);
    const title = BL.escape(item.displayName || item.productName || "상품명 정보 없음");
    const brand = BL.escape(item.brand || "브랜드 정보 없음");
    const reasonText = item.reason || item.topConcernTags || item.topNeedTags || "";
    const reason = opts.reason && reasonText
      ? `<p class="product-card-reason">${BL.escape(reasonText)}</p>`
      : `<p class="product-card-reason muted">외부 리뷰 데이터를 기준으로 비교해 보세요.</p>`;
    const score = item.recommendationScore != null
      ? `<span class="badge badge-primary">추천 점수 ${BL.score(item.recommendationScore)}</span>`
      : "";
    const serviceScore = item.serviceScore != null
      ? `<span class="badge badge-active">서비스 반응 ${BL.score(item.serviceScore)}</span>`
      : "";
    const siteStats = item.siteRatingCount || item.favoriteCount || item.viewCount
      ? `<div class="site-reaction">사이트 평가 ${item.siteRatingAvg || "-"}점 · 찜 ${item.favoriteCount || 0}</div>`
      : `<div class="site-reaction muted">아직 내부 평가가 부족합니다.</div>`;
    const caution = item.cautionLevel
      ? `<span class="badge ${BL.cautionClass(item.cautionLevel)}">${BL.cautionLabel(item.cautionLevel)}</span>`
      : "";
    const favoriteAction = opts.favorite === false ? "" : `
            <button class="btn btn-ghost" type="button" data-favorite-product="${BL.escape(item.productId)}">찜하기</button>`;
    const feedbackActions = opts.feedback ? `
          <details class="recommend-feedback">
            <summary>이 추천이 맞지 않나요?</summary>
            <div class="feedback-actions" aria-label="추천 피드백">
              <button class="btn btn-ghost" type="button" data-rec-feedback="NOT_INTERESTED" data-product-id="${BL.escape(item.productId)}">추천 숨기기</button>
              <button class="btn btn-ghost" type="button" data-rec-feedback="LIKE" data-product-id="${BL.escape(item.productId)}">도움 됐어요</button>
              <button class="btn btn-ghost" type="button" data-rec-feedback="DISLIKE" data-product-id="${BL.escape(item.productId)}">맞지 않아요</button>
            </div>
          </details>` : "";
    return `
      <article class="card product-card" data-product-id="${BL.escape(item.productId)}" data-product-platform="${BL.escape(item.platform || "")}" data-has-image="${item.imageUrl ? "true" : "false"}">
        <a href="${detailUrl}" aria-label="${title} 상세 보기">
          ${BL.imageHtml(item)}
        </a>
        <div class="product-body">
          <a class="product-card-info" href="${detailUrl}" aria-label="${title} 상세 보기">
            <div class="product-meta"><span>${brand}</span><span>${BL.platformLabel(item.platform)}</span></div>
            <strong class="product-title">${title}</strong>
            <div class="product-card-badges">${score}${serviceScore}${caution}</div>
            ${reason}
            ${siteStats}
          </a>
          <div class="product-card-actions">
            <a class="btn btn-primary" href="${detailUrl}">자세히 보기</a>
            ${favoriteAction}
          </div>
          ${feedbackActions}
        </div>
      </article>`;
  };

  BL.favoriteProductIds = null;
  BL.favoriteLoadPromise = null;

  BL.loadFavoriteProductIds = async function (force) {
    if (!force && BL.favoriteProductIds) return BL.favoriteProductIds;
    if (!force && BL.favoriteLoadPromise) return BL.favoriteLoadPromise;
    BL.favoriteLoadPromise = BL.get("/api/members/me/favorites")
      .then(function (res) {
        const map = {};
        (res.data || []).forEach(function (product) {
          if (product.productId != null) map[String(product.productId)] = true;
        });
        BL.favoriteProductIds = map;
        return map;
      })
      .catch(function (e) {
        if (e.status === 401) {
          BL.favoriteProductIds = {};
          return BL.favoriteProductIds;
        }
        throw e;
      })
      .finally(function () {
        BL.favoriteLoadPromise = null;
      });
    return BL.favoriteLoadPromise;
  };

  BL.setFavoriteButtonState = function (button, favorited) {
    button.textContent = favorited ? "찜 해제" : "찜하기";
    button.classList.toggle("is-selected", favorited);
    button.setAttribute("aria-pressed", favorited ? "true" : "false");
    button.setAttribute("title", favorited ? "찜한 상품에서 제거" : "찜한 상품에 추가");
  };

  BL.updateFavoriteButtons = function (productId, favorited, root) {
    const target = root || document;
    target.querySelectorAll("[data-favorite-product]").forEach(function (button) {
      if (String(button.dataset.favoriteProduct) === String(productId)) {
        BL.setFavoriteButtonState(button, favorited);
      }
    });
  };

  BL.syncFavoriteButtons = async function (root) {
    const target = root || document;
    const buttons = target.querySelectorAll("[data-favorite-product]");
    if (!buttons.length) return;
    try {
      const favoriteMap = await BL.loadFavoriteProductIds(false);
      buttons.forEach(function (button) {
        BL.setFavoriteButtonState(button, !!favoriteMap[String(button.dataset.favoriteProduct)]);
      });
    } catch (e) {
      buttons.forEach(function (button) {
        BL.setFavoriteButtonState(button, false);
      });
    }
  };

  BL.handleFavoriteClick = async function (button) {
    const productId = button.dataset.favoriteProduct;
    if (!productId) return;
    button.disabled = true;
    try {
      const favoriteMap = await BL.loadFavoriteProductIds(false);
      const currentlyFavorited = button.getAttribute("aria-pressed") === "true" || !!favoriteMap[String(productId)];
      if (currentlyFavorited) {
        await BL.del("/api/products/" + productId + "/favorite");
        delete favoriteMap[String(productId)];
        BL.updateFavoriteButtons(productId, false);
      } else {
        await BL.post("/api/products/" + productId + "/favorite", {});
        favoriteMap[String(productId)] = true;
        BL.updateFavoriteButtons(productId, true);
      }
    } catch (e) {
      if (e.status === 401) {
        window.location.href = BL.url(BL.loginPath());
        return;
      }
      alert("찜 처리에 실패했습니다.");
    } finally {
      button.disabled = false;
    }
  };

  BL.handleRecommendationFeedbackClick = async function (button) {
    const productId = button.dataset.productId;
    const feedbackType = button.dataset.recFeedback;
    if (!productId || !feedbackType) return;
    button.disabled = true;
    try {
      await BL.post("/api/products/" + productId + "/recommendation-feedback", { feedbackType: feedbackType });
      const group = button.closest(".feedback-actions");
      if (group) {
        group.querySelectorAll("button").forEach(function (b) {
          b.classList.toggle("is-selected", b === button);
          b.setAttribute("aria-pressed", b === button ? "true" : "false");
        });
      }
      if (feedbackType === "NOT_INTERESTED") {
        const card = button.closest(".product-card");
        if (card) card.classList.add("is-hidden-recommendation");
      }
    } catch (e) {
      if (e.status === 401) {
        window.location.href = BL.url(BL.loginPath());
        return;
      }
      alert("추천 피드백 저장에 실패했습니다.");
    } finally {
      button.disabled = false;
    }
  };

  BL.setNotice = function (id, message, type) {
    const el = document.getElementById(id);
    if (!el) return;
    if (!message) {
      el.innerHTML = "";
      el.className = "";
      return;
    }
    el.className = "notice " + (type === "error" ? "notice-error" : type === "success" ? "notice-success" : "");
    el.textContent = message;
  };

  BL.loading = function (message) {
    return `<div class="loading">${BL.escape(message || "불러오는 중입니다.")}</div>`;
  };

  BL.empty = function (message) {
    return `<div class="empty">${BL.escape(message || "표시할 데이터가 없습니다.")}</div>`;
  };

  BL.requireLoginView = function (message) {
    const loginPath = BL.loginPath();
    return `
      <div class="notice">
        <strong>${BL.escape(message || "로그인이 필요합니다.")}</strong>
        <div style="margin-top:12px;"><a class="btn btn-primary" href="${BL.url(loginPath)}">로그인</a></div>
      </div>`;
  };

  BL.initNav = async function () {
    const loginLink = document.getElementById("nav-login");
    const logoutBtn = document.getElementById("nav-logout");
    const meLabel = document.getElementById("nav-me");
    const mypageLink = document.getElementById("nav-mypage");
    const adminLink = document.getElementById("nav-admin");
    try {
      const me = await BL.me();
      BL.currentMember = me;
      if (me) {
        if (loginLink) loginLink.style.display = "none";
        if (logoutBtn) logoutBtn.style.display = "inline-flex";
        if (mypageLink) mypageLink.style.display = "inline-flex";
        if (adminLink) adminLink.style.display = me.role === "ADMIN" ? "inline-flex" : "none";
        if (meLabel) {
          meLabel.textContent = (me.nickname || me.loginId) + (me.role === "ADMIN" ? " · ADMIN" : "");
          meLabel.style.display = "inline-flex";
        }
      } else {
        if (loginLink) loginLink.style.display = "inline-flex";
        if (logoutBtn) logoutBtn.style.display = "none";
        if (mypageLink) mypageLink.style.display = "none";
        if (adminLink) adminLink.style.display = "none";
        if (meLabel) meLabel.style.display = "none";
      }
    } catch (e) {
      if (loginLink) loginLink.style.display = "inline-flex";
      if (logoutBtn) logoutBtn.style.display = "none";
      if (mypageLink) mypageLink.style.display = "none";
      if (adminLink) adminLink.style.display = "none";
    }
  };

  BL.initActiveNav = function () {
    const path = window.location.pathname.replace(BL.contextPath(), "") || "/";
    let active = "home";
    if (path.indexOf("/admin") === 0) active = "admin";
    else if (path.indexOf("/recommend") === 0) active = "recommend";
    else if (path.indexOf("/mypage") === 0) active = "mypage";
    else if (path.indexOf("/products") === 0) active = "products";

    document.querySelectorAll("[data-nav]").forEach(function (link) {
      link.classList.toggle("is-active", link.dataset.nav === active);
      if (link.dataset.nav === active) {
        link.setAttribute("aria-current", "page");
      } else {
        link.removeAttribute("aria-current");
      }
    });
  };

  BL.initTabs = function () {
    document.querySelectorAll("[data-tab-target]").forEach(function (button) {
      button.addEventListener("click", function () {
        const tab = button.dataset.tabTarget;
        const root = button.closest(".mypage-layout") || document;
        root.querySelectorAll("[data-tab-target]").forEach(function (item) {
          const active = item.dataset.tabTarget === tab;
          item.classList.toggle("is-active", active);
          item.setAttribute("aria-pressed", active ? "true" : "false");
        });
        root.querySelectorAll("[data-tab-panel]").forEach(function (panel) {
          const active = panel.dataset.tabPanel === tab;
          panel.classList.toggle("active", active);
          panel.hidden = !active;
        });
      });
    });
    document.querySelectorAll("[data-tab-panel]").forEach(function (panel) {
      if (!panel.classList.contains("active")) {
        panel.hidden = true;
      }
    });
  };

  BL.initAdminSidebar = function () {
    const sidebar = document.querySelector(".admin-sidebar");
    if (!sidebar) return;
    const path = window.location.pathname.replace(BL.contextPath(), "") || "/";
    let active = "dashboard";
    if (path.indexOf("/admin/products") === 0) active = "products";
    else if (path.indexOf("/admin/comment-reports") === 0) active = "reports";
    else if (path.indexOf("/admin/comments") === 0) active = "comments";
    else if (path.indexOf("/admin/logs") === 0) active = "logs";
    sidebar.querySelectorAll("[data-admin-nav]").forEach(function (link) {
      const isActive = link.dataset.adminNav === active;
      link.classList.toggle("is-active", isActive);
      if (isActive) link.setAttribute("aria-current", "page");
      else link.removeAttribute("aria-current");
    });
  };

  document.addEventListener("DOMContentLoaded", function () {
    BL.initActiveNav();
    BL.initTabs();
    BL.initAdminSidebar();
    BL.initNav();
    const logoutBtn = document.getElementById("nav-logout");
    if (logoutBtn) logoutBtn.addEventListener("click", BL.logout);
  });

  window.BL = BL;
})(window);
