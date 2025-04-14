package com.example.english.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class FileUploadService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String uploadAudioFile(MultipartFile file) throws IOException {
        // Lấy tên tệp và xác định đường dẫn lưu tệp
        String fileName = file.getOriginalFilename();
        String filePath = uploadDir + File.separator + fileName;

        // Tạo thư mục nếu chưa tồn tại
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File dest = new File(filePath);
        file.transferTo(dest);

        return fileName;
    }
}
