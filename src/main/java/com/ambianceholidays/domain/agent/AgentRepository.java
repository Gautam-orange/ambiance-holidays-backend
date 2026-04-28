package com.ambianceholidays.domain.agent;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentRepository extends JpaRepository<Agent, UUID>, JpaSpecificationExecutor<Agent> {

    Optional<Agent> findByUserIdAndDeletedAtIsNull(UUID userId);

    boolean existsByUserIdAndDeletedAtIsNull(UUID userId);

    Page<Agent> findByStatusAndDeletedAtIsNull(AgentStatus status, Pageable pageable);

    long countByStatusAndDeletedAtIsNull(AgentStatus status);
}
