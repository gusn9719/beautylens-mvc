<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String contextPath = request.getContextPath();
    String pageTitle = (String) request.getAttribute("pageTitle");
    String activePage = (String) request.getAttribute("activePage");
    if (pageTitle == null || pageTitle.isBlank()) {
        pageTitle = "BeautyLens";
    }
    if (activePage == null) {
        activePage = "";
    }
%>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title><%= pageTitle %></title>
  <link rel="stylesheet" href="<%= contextPath %>/assets/css/beautylens.css">
</head>
<body data-context-path="<%= contextPath %>" data-active-page="<%= activePage %>">
<header class="site-header">
  <nav class="nav" aria-label="주요 메뉴">
    <a class="brand" href="<%= contextPath %>/">BeautyLens</a>
    <div class="nav-links">
      <a data-nav="home" class="<%= "home".equals(activePage) ? "is-active" : "" %>" href="<%= contextPath %>/">홈</a>
      <a data-nav="products" class="<%= "products".equals(activePage) ? "is-active" : "" %>" href="<%= contextPath %>/products">상품</a>
      <a data-nav="recommend" class="<%= "recommend".equals(activePage) ? "is-active" : "" %>" href="<%= contextPath %>/recommend">추천</a>
      <a id="nav-mypage" data-nav="mypage" class="<%= "mypage".equals(activePage) ? "is-active" : "" %>" href="<%= contextPath %>/mypage" style="display:none;">마이페이지</a>
      <a id="nav-admin" data-nav="admin" class="<%= "admin".equals(activePage) ? "is-active" : "" %>" href="<%= contextPath %>/admin" style="display:none;">관리자</a>
    </div>
    <div class="nav-actions">
      <span id="nav-me" class="badge" style="display:none;"></span>
      <a id="nav-login" class="primary-link" href="<%= contextPath %>/login">로그인</a>
      <button id="nav-logout" type="button" style="display:none;">로그아웃</button>
    </div>
  </nav>
</header>
