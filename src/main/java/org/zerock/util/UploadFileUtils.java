package org.zerock.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

public class UploadFileUtils {

	private static final Logger log = LoggerFactory.getLogger(UploadFileUtils.class);
	
	
	// 파일 업로드 작업 함수. (업로드 경로, 파일 원본이름, 파일 byte 데이터).
	public static String uploadFile(String uploadPath, String originalName, byte[] fileData) throws Exception {
		
		UUID uid = UUID.randomUUID(); // UUID 생성.
		
		String savedName = uid.toString() + "_" + originalName; // 원래 이름에 UUID 붙임.
		String savedPath = calcPath(uploadPath); // 업로드할 경로를 계산.
		
		File target = new File(uploadPath + savedPath, savedName); // 저장할 파일.
		
		FileCopyUtils.copy(fileData, target); // (in, out).
		
		String formatName = originalName.substring(originalName.lastIndexOf(".")+1);
		
		String uploadedFileName = null;
		
		if(MediaUtils.getMediaType(formatName) != null) {
			// MediaUtils 클래스의 getMediaType 메서드를 사용해서
			// 확장자를 검사한 결과가 null 이 아니면? GIF, JPEG, PNG 중 하나라는 것.
			// 즉, 이미지 파일이라는 것.
			// 이미지 파일 일때는? 썸네일을 제작한다.
			
			uploadedFileName = makeThumbnail(uploadPath, savedPath, savedName);
			// 썸네일을 제작하고 썸네일 이름을 리턴.
			
		} else {
			
			uploadedFileName = makeIcon(uploadPath, savedPath, savedName);
			// 경로의 File.separatorChar를 '/'로 바꿔주는 역할.
			
		}
		
		return uploadedFileName; // 업로드 완료된 파일 이름을 경로를 포함해서 리턴.
		
	}
	
	// 경로상의 \\ 문자열을 /로 바꿔주는 역할.
	private static String makeIcon(String uploadPath, String path, String fileName) throws Exception {
		
		String iconName = uploadPath + path + File.separator + fileName;
		
		log.info("iconName : " + iconName);
		
		return iconName.substring(uploadPath.length()).replace(File.separatorChar, '/');
		
	}
	
	// 폴더 경로 생성.
	private static String calcPath(String uploadPath) {
		
		Calendar cal = Calendar.getInstance();
		
		String yearPath = File.separator+cal.get(Calendar.YEAR); // File.separator는 / 기호를 의미한다.
		
		String monthPath = yearPath + File.separator + new DecimalFormat("00").format(cal.get(Calendar.MONTH)+1);
		// new DecimalFormat("00").format(숫자) : 숫자를 00 포맷 형식으로 변환.
		
		String datePath = monthPath + File.separator + new DecimalFormat("00").format(cal.get(Calendar.DATE));
		
		makeDir(uploadPath, yearPath, monthPath, datePath); // datePath는 year, month, date 값을 다 더한 최종 경로가 됨.
		
		return datePath;
		
	}
	
	// 생성된 경로를 전달받아 실제로 폴더를 만듬.
	private static void makeDir(String uploadPath, String... paths) {
		// 두번째 부터 받는 파라미터는 String 형식의 배열로 받는다.
		// 여기서는 위의 yearPath, monthPath, datePath.
				
		if(new File(uploadPath + paths[paths.length - 1]).exists()) {
			// paths.length -1 하면 마지막 값을 가져옴. 마지막 값은 datePath.
			// datePath는 최종경로니까 최종경로가 존재하면 그냥 return.
			
			return;
			
		}
		
		for (String path : paths) {
			
			File dirPath = new File(uploadPath + path);
			
			if(!dirPath.exists()) { // 폴더가 존재하지 않으면?
				
				dirPath.mkdir(); // 만든다.
				
			}
			
		}
		
	}
	
	// 썸네일 생성. (업로드 경로, 년/월/일 경로, 파일이름)
	private static String makeThumbnail(String uploadPath, String path, String fileName) throws Exception {
		
		BufferedImage sourceImg = ImageIO.read(new File(uploadPath + path, fileName));
		// BufferedImage는 메모리상에 로딩한 이미지임.
		
		BufferedImage destImg = Scalr.resize(sourceImg, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_TO_HEIGHT, 100);
		// Scalr 라이브러리를 이용해서 실제로 썸네일을 제작하는 것.
		
		String thumbnailName = uploadPath + path + File.separator + "s_" + fileName;
		// 전체 경로를 포함한 썸네일 이미지.
		
		File newFile = new File(thumbnailName); // 파일 인스턴스 생성.
		String formatName = fileName.substring(fileName.lastIndexOf(".")+1);
		// 파일 이름에서 맨뒤의 .에서 +1 한 값을 substring 한 것은? 그 파일의 확장자임.
		
		ImageIO.write(destImg, formatName.toUpperCase(), newFile);
		// 이미지 쓰기.
		// (이미지파일, 대문자로된 확장자, 파일 인스턴스);
		// 이미지 파일을 확장자 형식으로 파일 인스턴스에 복사해 넣는다.
		
		String cuttedThumbnailName = thumbnailName.substring(uploadPath.length()).replace(File.separatorChar, '/');
		
		log.info("thumbnailName : " + thumbnailName);
		log.info("CuttedThumbnailName : " + cuttedThumbnailName);
				
		return cuttedThumbnailName;
				
	}
	
}