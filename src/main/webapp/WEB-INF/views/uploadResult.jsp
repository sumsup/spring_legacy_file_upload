<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="false" %>

<script>
	
	var result = '${savedName}'; // 모델에서 savedName 을 불러옴.
	
	parent.addFilePath(result); // parent : iframe에서 상위 페이지의 함수에 접근.
	
</script>
