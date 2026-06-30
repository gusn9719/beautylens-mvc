<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  String adminUri = request.getRequestURI();
  String adminContext = request.getContextPath();
%>
<aside class="admin-sidebar" aria-label="관리자 메뉴">
  <div class="admin-sidebar-title">
    <strong>운영 콘솔</strong>
    <span>BeautyLens 관리</span>
  </div>
  <nav class="admin-nav">
    <a class="admin-nav-link <%= adminUri.endsWith("/admin") ? "is-active" : "" %>" data-admin-nav="dashboard" href="<%= adminContext %>/admin">대시보드</a>
    <a class="admin-nav-link <%= adminUri.contains("/admin/products") ? "is-active" : "" %>" data-admin-nav="products" href="<%= adminContext %>/admin/products">상품 관리</a>
    <a class="admin-nav-link <%= adminUri.contains("/admin/comments") ? "is-active" : "" %>" data-admin-nav="comments" href="<%= adminContext %>/admin/comments">댓글 관리</a>
    <a class="admin-nav-link <%= adminUri.contains("/admin/comment-reports") ? "is-active" : "" %>" data-admin-nav="reports" href="<%= adminContext %>/admin/comment-reports">신고 관리</a>
    <a class="admin-nav-link" data-admin-nav="recommendation" href="<%= adminContext %>/admin/products#recommendation">추천 운영</a>
    <a class="admin-nav-link <%= adminUri.contains("/admin/logs") ? "is-active" : "" %>" data-admin-nav="logs" href="<%= adminContext %>/admin/logs">운영 로그</a>
  </nav>
</aside>
