<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<style>
iframe {
	width: 0px;
	height: 0px;
	border: 0px
}
</style>
<title>File Upload</title>
</head>
<body>
	
	<form id='form1' action="uploadForm" method="post" enctype="multipart/form-data" 
		target='zeroFrame'> 
		<!-- target 속성 : 응답을 zeroFrame 에 표시함. -->
		<!-- 컨트롤러에서 이동을 uploadResult 페이지로 이동하게 함. 
			target을 아래의 iframe으로 설정 해놨기 때문에 새창을 iframe에서 염.-->
		
		<input type="file" name="file">
		<input type='submit'>
	</form>
	
	<iframe name='zeroFrame'></iframe>
	
	<script>
		function addFilePath(msg) {
			alert(msg);
			
			document.getElementById("form1").reset();
		}
	</script>
	
</body>
</html>