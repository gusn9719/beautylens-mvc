<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="kr.ac.kopo.member.vo.MemberVO" %>
<%
    MemberVO loginMember = (MemberVO) request.getAttribute("loginMember");
    boolean isLoggedIn = loginMember != null;
    boolean isAdmin = isLoggedIn && "ADMIN".equals(loginMember.getRole());
    String nickname = isLoggedIn ? loginMember.getNickname() : null;
%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>BeautyLens - 댓글 테스트</title>
<style>
  body { font-family: 'Nanum Gothic', sans-serif; background: #f5f5f5; margin: 0; }
  .header { background: #e91e8c; color: white; padding: 14px 24px; display: flex; justify-content: space-between; align-items: center; }
  .header h1 { margin: 0; font-size: 18px; }
  .container { max-width: 800px; margin: 24px auto; padding: 0 16px; }
  .card { background: white; border-radius: 8px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,.08); margin-bottom: 16px; }
  .card h2 { margin: 0 0 16px; font-size: 16px; border-bottom: 2px solid #eee; padding-bottom: 8px; }
  .product-select { display: flex; gap: 8px; align-items: center; margin-bottom: 12px; }
  .product-select input { flex: 1; padding: 8px 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; }
  .btn { padding: 8px 18px; border-radius: 6px; border: none; cursor: pointer; font-size: 14px; }
  .btn-primary { background: #e91e8c; color: white; }
  .btn-primary:hover { background: #c2185b; }
  .btn-del { background: #e53935; color: white; font-size: 12px; padding: 3px 10px; }
  .btn-del:hover { background: #b71c1c; }
  textarea { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; resize: vertical; box-sizing: border-box; }
  .comment-list { list-style: none; margin: 0; padding: 0; }
  .comment-item { padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
  .comment-item:last-child { border-bottom: none; }
  .comment-meta { font-size: 12px; color: #888; margin-bottom: 4px; display: flex; justify-content: space-between; }
  .comment-content { font-size: 14px; color: #333; }
  .auth-info { font-size: 13px; color: rgba(255,255,255,.85); }
  .info-box { background: #fff3cd; border: 1px solid #ffc107; border-radius: 6px; padding: 10px 14px; font-size: 13px; margin-bottom: 12px; }
  #status { font-size: 13px; color: #e91e8c; margin-top: 6px; min-height: 18px; }
</style>
</head>
<body>
<div class="header">
  <h1>BeautyLens 댓글 테스트</h1>
  <div class="auth-info">
    <% if (isLoggedIn) { %>
      <%= nickname %> 님<% if (isAdmin) { %> (관리자)<% } %>
      &nbsp;|&nbsp;<a href="/beautylens-mvc/" style="color:rgba(255,255,255,.85);">홈</a>
      &nbsp;|&nbsp;<a href="#" style="color:rgba(255,255,255,.85);" onclick="logout()">로그아웃</a>
    <% } else { %>
      <span>미로그인 — 댓글 작성 불가</span>
    <% } %>
  </div>
</div>

<div class="container">
  <div class="card">
    <h2>상품 선택</h2>
    <div class="product-select">
      <input type="number" id="productId" placeholder="productId 입력 (예: 730)" value="730" min="1" />
      <button class="btn btn-primary" onclick="loadComments()">댓글 불러오기</button>
    </div>
    <div style="font-size:12px;color:#999;">추천 productId: 730 (무신사), 1446 (올리브영), 743 (올리브영 지성 1위)</div>
  </div>

  <% if (!isLoggedIn) { %>
  <div class="info-box">
    댓글을 작성하려면 로그인이 필요합니다.
    <a href="#" onclick="tryLogin()">로그인 (test01/1234)</a>
  </div>
  <% } %>

  <% if (isLoggedIn) { %>
  <div class="card">
    <h2>댓글 작성</h2>
    <textarea id="content" rows="3" maxlength="1000" placeholder="의견을 남겨주세요 (최대 1000자)"></textarea>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-top:8px;">
      <span id="charCount" style="font-size:12px;color:#999;">0 / 1000</span>
      <button class="btn btn-primary" onclick="postComment()">등록</button>
    </div>
    <div id="status"></div>
  </div>
  <% } %>

  <div class="card">
    <h2>댓글 목록 <span id="commentCount" style="font-size:13px;color:#aaa;"></span></h2>
    <ul class="comment-list" id="commentList">
      <li style="color:#aaa;text-align:center;padding:20px;">상품을 선택하면 댓글이 표시됩니다.</li>
    </ul>
  </div>
</div>

<script>
var BASE = '/beautylens-mvc';
var isAdmin = <%= isAdmin %>;
var isLoggedIn = <%= isLoggedIn %>;

document.getElementById('content') && document.getElementById('content').addEventListener('input', function() {
  document.getElementById('charCount').textContent = this.value.length + ' / 1000';
});

function loadComments() {
  var pid = document.getElementById('productId').value;
  if (!pid) return;
  fetch(BASE + '/api/products/' + pid + '/comments', { credentials: 'include' })
    .then(r => r.json())
    .then(d => {
      var list = document.getElementById('commentList');
      var cnt = document.getElementById('commentCount');
      if (!d.success || !d.data || d.data.length === 0) {
        list.innerHTML = '<li style="color:#aaa;text-align:center;padding:20px;">댓글이 없습니다.</li>';
        cnt.textContent = '(0건)';
        return;
      }
      cnt.textContent = '(' + d.data.length + '건)';
      list.innerHTML = d.data.map(function(c) {
        var del = isAdmin
          ? '<button class="btn btn-del" onclick="deleteComment(' + c.commentId + ')">삭제</button>'
          : '';
        return '<li class="comment-item">' +
          '<div class="comment-meta"><span>' + (c.nickname||'') + ' · ' + (c.createdAt||'') + '</span>' + del + '</div>' +
          '<div class="comment-content">' + escHtml(c.content) + '</div></li>';
      }).join('');
    });
}

function postComment() {
  var pid = document.getElementById('productId').value;
  var content = document.getElementById('content').value.trim();
  if (!pid) { setStatus('상품 ID를 입력해주세요.'); return; }
  if (!content) { setStatus('내용을 입력해주세요.'); return; }
  fetch(BASE + '/api/products/' + pid + '/comments', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ content: content })
  }).then(r => r.json()).then(d => {
    if (d.success) {
      document.getElementById('content').value = '';
      document.getElementById('charCount').textContent = '0 / 1000';
      setStatus('등록 완료!');
      loadComments();
    } else { setStatus('오류: ' + d.message); }
  });
}

function deleteComment(cid) {
  if (!confirm('댓글을 삭제하시겠습니까?')) return;
  fetch(BASE + '/api/admin/comments/' + cid, { method: 'DELETE', credentials: 'include' })
    .then(r => r.json()).then(d => {
      if (d.success) { loadComments(); }
      else { alert('삭제 실패: ' + d.message); }
    });
}

function setStatus(msg) {
  var el = document.getElementById('status');
  if (el) { el.textContent = msg; setTimeout(function(){ el.textContent=''; }, 3000); }
}

function escHtml(s) {
  return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

function tryLogin() {
  fetch(BASE + '/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ loginId: 'test01', password: '1234' })
  }).then(r => r.json()).then(d => {
    if (d.success) location.reload();
    else alert('로그인 실패: ' + d.message);
  });
}

function logout() {
  fetch(BASE + '/api/auth/logout', { method: 'POST', credentials: 'include' })
    .then(() => location.reload());
}

window.onload = function() { loadComments(); };
</script>
</body>
</html>
