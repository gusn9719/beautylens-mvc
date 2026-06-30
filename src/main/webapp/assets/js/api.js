(function (window) {
  const BL = window.BL || {};

  BL.contextPath = function () {
    const fromBody = document.body ? document.body.dataset.contextPath : "";
    if (fromBody) return fromBody;
    const parts = window.location.pathname.split("/").filter(Boolean);
    return parts.length ? "/" + parts[0] : "";
  };

  BL.url = function (path) {
    return BL.contextPath() + path;
  };

  BL.api = async function (path, options) {
    const init = Object.assign({
      credentials: "include",
      headers: {}
    }, options || {});

    if (init.body && typeof init.body !== "string") {
      init.headers["Content-Type"] = "application/json; charset=utf-8";
      init.body = JSON.stringify(init.body);
    }

    const response = await fetch(BL.url(path), init);
    const text = await response.text();
    let payload = null;
    if (text) {
      try {
        payload = JSON.parse(text);
      } catch (e) {
        payload = { success: false, message: text, data: null };
      }
    }

    if (!response.ok) {
      const error = new Error(payload && payload.message ? payload.message : "요청을 처리하지 못했습니다.");
      error.status = response.status;
      error.payload = payload;
      throw error;
    }
    return payload;
  };

  BL.get = function (path) {
    return BL.api(path, { method: "GET" });
  };

  BL.post = function (path, body) {
    return BL.api(path, { method: "POST", body: body });
  };

  BL.put = function (path, body) {
    return BL.api(path, { method: "PUT", body: body });
  };

  BL.del = function (path) {
    return BL.api(path, { method: "DELETE" });
  };

  BL.me = async function () {
    try {
      const res = await BL.get("/api/members/me");
      return res.data;
    } catch (e) {
      if (e.status === 401) return null;
      throw e;
    }
  };

  BL.logout = async function () {
    await BL.post("/api/auth/logout", {});
    window.location.href = BL.url("/");
  };

  window.BL = BL;
})(window);
