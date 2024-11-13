package com.myproject.crispysystem.users.repository;

import com.myproject.crispysystem.users.model.UserAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserAuditRepository extends JpaRepository<UserAudit, UUID> {
}
