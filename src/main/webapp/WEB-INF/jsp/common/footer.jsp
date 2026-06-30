<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String footerContextPath = request.getContextPath();
%>
<footer class="footer">
  BeautyLens<br>
  <span>피부 타입과 리뷰 데이터를 바탕으로 더 나은 선택을 돕습니다.</span>
</footer>
<script src="<%= footerContextPath %>/assets/js/api.js"></script>
<script src="<%= footerContextPath %>/assets/js/face-camera.js"></script>
<script src="<%= footerContextPath %>/assets/js/ui.js"></script>
</body>
</html>
