package com.do_an.userservice.service;

import com.do_an.userservice.exceptions.AppException;
import com.do_an.userservice.exceptions.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {
    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("Created upload directory: " + this.fileStorageLocation.toString());
        } catch (Exception ex) {
            throw new AppException(ErrorCode.UNAUTHORIZATION);
        }
    }

    /**
     * Lưu trữ một file và trả về tên file duy nhất.
     */
    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Không thể lưu tệp trống.");
        }
        try {
            // Tạo tên file ngẫu nhiên để tránh trùng lặp
            String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String newFilename = UUID.randomUUID().toString() + "." + extension;

            //Tạo đường dẫn chưá file ảnh trong thư mục
            Path destinationFile = this.fileStorageLocation.resolve(Paths.get(newFilename)).normalize().toAbsolutePath();

            //Kiểm tra đường dẫn chưá file ảnh có nằm ngoài thư mục cho phép
            if (!destinationFile.getParent().equals(this.fileStorageLocation.toAbsolutePath())) {
                throw new RuntimeException("Không thể lưu tệp ngoài thư mục hiện tại.");
            }

            //Copy file ảnh vào đường dẫn được chứa ảnh
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return newFilename; // Trả về tên file đã lưu
        } catch (IOException e) {
            throw new RuntimeException("Không lưu được tệp.", e);
        }
    }

    /**
     * Xóa một file dựa trên tên file.
     */
    public boolean deleteFile(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted: " + fileName);
                return true;
            }
            log.warn("File not found for deletion: " + fileName);
            return false;
        } catch (IOException ex) {
            log.info("Could not delete file " + fileName + ": " + ex.getMessage());
            // Có thể throw FileUploadException ở đây nếu muốn báo lỗi rõ ràng hơn
            return false;
        }
    }

    /**
     * Trả về đường dẫn hoàn chỉnh của file để truy cập qua URL (vd: /uploads/abc-xyz.jpg)
     */
    public Resource getFileUrl(String fileName) {
        try {
            //Nối đường dẫn của thư mục với tên file ảnh
            Path file = fileStorageLocation.resolve(fileName);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Không thể đọc tệp:" + fileName);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Không thể đọc tệp:" + fileName, e);
        }
    }

    /**
     * Trích xuất tên file từ một URL đầy đủ (nếu có).
     * Ví dụ: từ "/uploads/abc-xyz.jpg" -> "abc-xyz.jpg"
     */
    public String extractFileNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return null;
        }
        int lastSlashIndex = fileUrl.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < fileUrl.length() - 1) {
            return fileUrl.substring(lastSlashIndex + 1);
        }
        return fileUrl; // Hoặc ném lỗi nếu định dạng không đúng
    }
}