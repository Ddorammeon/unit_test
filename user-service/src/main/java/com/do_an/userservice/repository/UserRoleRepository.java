package com.do_an.userservice.repository;

import com.do_an.userservice.entity.Role;
import com.do_an.userservice.entity.User;
import com.do_an.userservice.entity.UserRole;
import com.do_an.userservice.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    List<UserRole> findByUser(User user);

    List<UserRole> findByUserId(UUID userId);

    List<UserRole> findByRole(Role role);

    List<UserRole> findByRoleId(UUID roleId);

    Optional<UserRole> findByUserAndRole(User user, Role role);

    Optional<UserRole> findByUserIdAndRoleId(UUID userId, UUID roleId);

    boolean existsByUserAndRole(User user, Role role);

    boolean existsByUserIdAndRoleId(UUID userId, UUID roleId);

    void deleteByUserAndRole(User user, Role role);

    void deleteByUser(User user);

    void deleteByRole(Role role);

    @Query("SELECT ur.user FROM UserRole ur WHERE ur.role.roleName = :roleName")
    List<User> findUsersByRoleName(@Param("roleName") String roleName);

    @Query("SELECT ur.role FROM UserRole ur WHERE ur.user.id = :userId")
    List<Role> findRolesByUserId(@Param("userId") UUID userId);
}
