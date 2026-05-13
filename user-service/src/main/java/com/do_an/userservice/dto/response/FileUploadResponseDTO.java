package com.do_an.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadResponseDTO {
    private String fileName; // Tên file đã lưu (vd: uuid.jpg)
    private String fileUrl; // Đường dẫn đầy đủ để truy cập file (vd: /api/v1/files/uuid.jpg)
    private String fileType; // Kiểu file (vd: image/jpeg)
    private long size; // Kích thước (bytes)
}
