package com.do_an.userservice.repository;

import com.do_an.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByPhone(String phone);
    
    Optional<User> findByEmail(String email);
    
    // Tìm user theo trạng thái active
    List<User> findAllByIsActive(boolean isActive);
    
    // Tìm user theo fullname (chứa chuỗi)
    List<User> findAllByFullnameContainingIgnoreCase(String fullname);
    
    // Tìm user theo email (chứa chuỗi)
    List<User> findAllByEmailContainingIgnoreCase(String email);
    
    // Sắp xếp theo thời gian tạo giảm dần
    List<User> findAllByOrderByCreateAtDesc();
    
    // Sắp xếp theo thời gian tạo tăng dần
    List<User> findAllByOrderByCreateAtAsc();
}
