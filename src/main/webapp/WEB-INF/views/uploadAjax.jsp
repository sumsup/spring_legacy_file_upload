<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>upload Ajax</title>
<style>
	.fileDrop {
		width: 100%;
		height: 200px;
		border: 1px dotted blue;
	}
	small {
		margin-left: 3px;
		font-weight: bold;
		color: gray;
	}
</style>
</head>
<body>
	
	<h3>Ajax File Upload</h3>
	<div class='fileDrop'></div>
	<div class='uploadedList'></div>
	
	<script src='https://code.jquery.com/jquery-3.4.1.min.js'></script>
	<script>
	
		$('.fileDrop').on('dragenter dragover' , function(event) {
			// 사진 파일을 drag & drop 하면 사진 파일이 새창에서 열림.
			// 기본 이벤트를 prevent 해준 것.
			event.preventDefault();
			
			
		});
		
		$('.fileDrop').on('drop', function(event) {
			event.preventDefault();
			
			var files = event.originalEvent.dataTransfer.files;
			// 제이쿼리를 쓰는 경우. originalEvent 로 원래 이벤트를 가져 와야 함.
			// dataTransfer는 전달된 데이터. files는 전달된 데이터 안의 files들을 가지고 옴.
			
			var file = files[0];
			
			// console.log(file); // 파일에 대한 정보를 담고 있다.
			
			var formData = new FormData();
			
			formData.append("file", file);
			
			$.ajax({
				
				url: '/uploadAjax',
				data: formData,
				dataType: 'text',
				processData: false, // 데이터를 일반적인 query string으로 변환할 것인지를 결정.
				contentType: false, 
				// 기본값은 'application / x-www-form-urlencoded'. 파일의 경우는 multipart/form-data 이므로.
				// false로 지정.
				type: 'POST',
				success: function(data) { // 파일 전송이 성공했을 때.
					
					var str = '';
					
					console.log(data);
					console.log(checkImageType(data));
					
					if(checkImageType(data)) { // 이미지 파일이면?
						
						// 이미지 태그로 추가.
						str = '<div>'
							+ '<a href="displayFile?fileName='+ getRidOfThumbnailMark(data)+'">'
							+ '<img src="displayFile?fileName=' + data + '"/>'
							+ getRidOfThumbnailMark(data) + '</a>'
							+ '<small data-src='+data+'>X</small></div>';
						
					} else { // 데이터 파일이면?
						
						// 이름만 보여주기.
						str = '<div><a href="displayFile?fileName='+data+'">'
							+ getOriginalName(data)
							+ '</a>'
							+ '<small data-src='+data+'>X</small></div>';
						
					}
					
					$('.uploadedList').append(str);

				}
				
			});
			
		});
		
		$('.uploadedList').on('click', 'small', function(event){
			
			var that = $(this); 
			// if문 안에서의 this는 uploadedList 가 아니므로 this를 that 변수에 저장해서
			// 현재 위치를 기억해놓는다.
			
			$.ajax({
				url : 'deleteFile',
				type : 'post',
				data : {fileName:$(this).attr('data-src')}, // 파일이름은 data 속성값에서 가져온다.
				dataType : 'text',
				success : function(result) {
					
					if(result === 'deleted') {
						
						alert('deleted!');
						that.parent('div').remove(); // uploadedList div를 삭제한다.
						
					}

				}
				
			});
			
		});
		
		// 이미지 파일 여부 검사.
		function checkImageType(fileName) {
			
			var pattern = /jpg$|gif$|png$|jpeg$/i; // 정규 표현식.
			
			return fileName.match(pattern); // 패턴을 매칭 시켜서 맞으면 true. 아니면 false.
			
		}
		
		// UUID를 제거한 원래 파일 이름을 가져옴.
		function getOriginalName(fileName) {
			
			if(checkImageType(fileName)) { // 이미지 파일이면? 
				
				return;
			
			}
			
			var idx = fileName.indexOf('_') + 1;
			
			return fileName.substr(idx);
			
		}
		
		// 썸네일이 아닌 원본 파일의 이름을 가져오는 작업.
		function getRidOfThumbnailMark(fileName) {
			
			if(!checkImageType(fileName)) { // 이미지 파일이 아니면 return.
				return;
			}
			
			var front = fileName.substr(0,12); // 썸네일 파일 이름의 앞부분.
			var end = fileName.substr(14); // 썸네일 파일 이름의 뒷부분.
			
			return front + end; // 앞부분과 뒷부분을 합치면 원본 파일 이름이 됨.
			
		}
		
	</script>
	
</body>
</html>