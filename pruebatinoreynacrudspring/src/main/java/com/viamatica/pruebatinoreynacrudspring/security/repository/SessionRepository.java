package com.viamatica.pruebatinoreynacrudspring.security.repository;

import com.viamatica.pruebatinoreynacrudspring.security.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {
}
