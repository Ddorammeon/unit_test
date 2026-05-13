package com.do_an.userservice.controller;

import com.do_an.userservice.dto.response.FileUploadResponseDTO;
import com.do_an.userservice.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;

@RestController
@RequestMapping("/user-service/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileStorageService fileStorageService;

    /**
     * API 1: Upload một file
     * Client gọi API này TRƯỚC, nhận về URL,
     * sau đó mới gọi API nghiệp vụ (vd: update profile)
     */
    @PostMapping("/upload")
    //@PreAuthorize("isAuthenticated()") // Bất kỳ ai đăng nhập cũng có thể upload
    public ResponseEntity<FileUploadResponseDTO> uploadFile(@RequestParam("file") MultipartFile file) {

        // 1. Lưu file và lấy tên file duy nhất
        String fileName = fileStorageService.storeFile(file);

        // 2. Tạo URL để truy cập file này
        // (Nó sẽ trỏ đến API GET /api/v1/files/{fileName} ở dưới)
        String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/")
                .path(fileName)
                .toUriString();

        log.info("File đã được tải lên thành công: {}. URL truy cập: {}", fileName, fileUrl);

        // 3. Trả về thông tin cho client
        return ResponseEntity.ok(new FileUploadResponseDTO(
                fileName,
                fileUrl,
                file.getContentType(),
                file.getSize()
        ));
    }

    /**
     * API 2: Truy cập/Xem một file
     * Đây là API mà 'fileUrl' ở trên trỏ tới
     */
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {

        // 1. Lấy tài nguyên file từ service [cite: 23]
        Resource resource = fileStorageService.getFileUrl(fileName);

        // 2. Xác định Content-Type (MIME type)
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.warn("Không thể xác định loại tệp cho: {}", fileName);
        }
        if (contentType == null) {
            contentType = "application/octet-stream"; // Loại chung
        }

        // 3. Trả về file
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                // 'inline' để trình duyệt hiển thị ảnh, video thay vì tải về
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * API 3: Xóa một file
     * (Cần được bảo mật cẩn thận, ví dụ: chỉ Admin hoặc chủ sở hữu)
     */
    @DeleteMapping("/{fileName:.+}")
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')") // Tạm thời chỉ cho Admin
    public ResponseEntity<Void> deleteFile(@PathVariable String fileName) {
        boolean deleted = fileStorageService.deleteFile(fileName);

        if (deleted)  {
            log.info("File đã bị xoá");
            return ResponseEntity.noContent().build(); // HTTP 204
        } else {
            log.warn("Không thể xóa tệp (không tìm thấy): {}", fileName);
            return ResponseEntity.notFound().build(); // HTTP 404
        }
    }
}
