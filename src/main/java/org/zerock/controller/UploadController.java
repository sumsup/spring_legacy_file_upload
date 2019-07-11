package org.zerock.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.util.MediaUtils;
import org.zerock.util.UploadFileUtils;

@Controller
public class UploadController {
	
	private static final Logger log = LoggerFactory.getLogger(UploadController.class);

	// 업로드 경로를 servelt-context에 설정.
	// @Resource 로 bean 으로 만들어져 있는 값을 가져다 씀.
	@Resource(name="uploadPath")
	private String uploadPath; // 업로드 경로는 String 형식이다.
	
	@RequestMapping(value="/uploadForm", method=RequestMethod.GET)
	public void uploadForm() {
		
	}
	
	@RequestMapping(value="/uploadForm", method=RequestMethod.POST)
	public String uploadForm(MultipartFile file, Model model) throws Exception { // 파라미터 타입을 MultipartFile 로.
		
		log.info("originalName : " + file.getOriginalFilename());
		log.info("size : " + file.getSize());
		log.info("contentType : " + file.getContentType());
		
		String savedName = uploadFile(file.getOriginalFilename(), file.getBytes());
		
		model.addAttribute("savedName", savedName);
		
		return "uploadResult";
		
	}
	
	// 파일 업로드. (파일원래이름, 파일데이터).
	// 파일을 업로드 하고 업로드한 파일의 이름을 리턴.
	private String uploadFile(String originalName, byte[] fileData) throws Exception {
		
		UUID uid = UUID.randomUUID(); // 랜덤 문자열 생성.
		
		String savedName = uid.toString() + "_" + originalName;
		
		File target = new File(uploadPath, savedName); // (파일경로, 파일이름). 파일 인스턴스를 생성.
		//uploadPath 는 bean에 등록한 객체를 필드에서 @Resource로 불러온 것.
		
		FileCopyUtils.copy(fileData, target); // FileCopyUtils : 스프링에서 제공하는 클래스.
		
		return savedName;
		
	}
	
	@RequestMapping(value="/uploadAjax", method=RequestMethod.GET)
	public void uploadAjax() {
		
	}

	@ResponseBody // 이건 왜?
	@RequestMapping(value = "/uploadAjax", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	// @RequestMapping에서 produces 속성의 text/plain;charset=UTF-8 은 한글파일명을 전송하기 위함.
	public ResponseEntity<String> uploadAjax(MultipartFile file) throws Exception {
		
		log.info("originalName: "+ file.getOriginalFilename());
		log.info("size: " + file.getSize());
		log.info("contentType: "+ file.getContentType());
		
		return new ResponseEntity<>(UploadFileUtils.uploadFile(uploadPath, file.getOriginalFilename(),
				file.getBytes()), HttpStatus.CREATED);
		// HttpStatus 코드는 리소스가 정상적으로 생성되었다는 상태 코드.
		
	}
	
	@ResponseBody
	@RequestMapping("/displayFile")
	// 이미지 파일은 보여주고. 그 외 파일은 다운로드 해줌. (파일 이름).
	// 다운로드는? apache에서 제공하는 commons 라이브러리를 이용한다.
	// commons 라이브러리에 IOUtils.toByteArray 가 파일을 전달하는 역할을 한다.
	// 따라서 ResponseEntity가 전달하는 자료형은 byte array 형식이된다. => ResponseEntity<byte[]>
	public ResponseEntity<byte[]> displayFile(String fileName) throws Exception {
		// 스프링에서 String 파라미터는 query string에서 찾아서 가져옴.
		// localhost:8080/displayFile?fileName=??? 이면
		// 파라미터의 fileName을 쿼리스트링의 ???를 가져와서 저장.
		
		InputStream in = null;
		ResponseEntity<byte[]> entity = null;
		
		log.info("FILE NAME : " + fileName);
		
		try {
			
			String formatName = fileName.substring(fileName.lastIndexOf(".")+1); // 확장자.
			
			MediaType mType = MediaUtils.getMediaType(formatName);
			
			HttpHeaders headers = new HttpHeaders();
			
			in = new FileInputStream(uploadPath + fileName); // 인풋 인스턴스에 해당경로에 있는 파일을 불러오기.
			
			if(mType != null) { // 이미지 파일일때는?
				
				headers.setContentType(mType); // 헤더에 컨텐츠 타입을 셋팅해줌.
				
			} else { // 일반 파일일 경우엔?
				
				fileName = fileName.substring(fileName.indexOf("_")+1); // 앞의 UUID를 제거해 주겠다는 것.
				
				headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
				// APPLICATION_OCTET_STREAM은 다운로드용.
				// 브라우저는 이 content type을 보고 사용자에게 다운로드를 시켜줌.
				
				headers.add("Content-Disposition", "attachment; filename=\""+ // 이스케이프 문자열.
						new String(fileName.getBytes("UTF-8"), "ISO-8859-1")+"\"");
				// fileName.getBytes("UTF-8") : 파일 이름을 UTF-8로 설정. 한글 파일 이름을 깨지지 않도록 하기 위함.
								
			}
			
			entity = new ResponseEntity<byte[]>(IOUtils.toByteArray(in), headers, HttpStatus.CREATED);
			// IOUtils.toByteArray(in) 는 apache commons 파일을 읽어오는 라이브러리다.
			// ResponseEntity는 (바이트파일, 헤더, 상태코드).
						
		} catch (Exception e) {
			
			e.printStackTrace();
			entity = new ResponseEntity<byte[]>(HttpStatus.BAD_REQUEST);
			
		} finally {
			
			in.close();
			
		}
		
		return entity;
		
	}
	
	
	// 업로드한 파일을 삭제하는 함수. (파일이름)
	@ResponseBody
	@RequestMapping(value="/deleteFile", method=RequestMethod.POST)
	public ResponseEntity<String> deleteFile(String fileName) {
		
		log.info("delete file : " + fileName);
		
		String formatName = fileName.substring(fileName.lastIndexOf(".")+1);
		
		MediaType mType = MediaUtils.getMediaType(formatName);
		
		if(mType != null) { // 이미지 파일이면? 썸네일 표시를 지우고 원본 파일을 삭제.
			
			// 섬네일 파일의 가운데 s_ 를 삭제 해주는 작업.
			String front = fileName.substring(0,12);
			String end = fileName.substring(14);
			
			new File(uploadPath + (front+end).replace('/', File.separatorChar)).delete(); 
			// 원본 파일 객체를 찾아서 삭제.
			
		}
		
		new File(uploadPath + fileName.replace('/', File.separatorChar)).delete();
		// 썸네일이든, 일반 파일이든 찾아서 삭제.
		
		return new ResponseEntity<String>("deleted", HttpStatus.OK);
		// 삭제에 성공하면 "deleted" 문자열을 ajax response 로 전달.
		
	}

	
}