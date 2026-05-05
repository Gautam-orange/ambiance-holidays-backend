package com.ambianceholidays.api.agent;

import com.ambianceholidays.api.agent.dto.AgentResponse;
import com.ambianceholidays.api.agent.dto.AgentUpdateRequest;
import com.ambianceholidays.api.notification.NotificationService;
import com.ambianceholidays.common.dto.ApiResponse;
import com.ambianceholidays.common.dto.PageMeta;
import com.ambianceholidays.domain.agent.*;
import com.ambianceholidays.domain.user.User;
import com.ambianceholidays.domain.user.UserRepository;
import com.ambianceholidays.exception.BusinessException;
import com.ambianceholidays.security.SecurityPrincipal;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/agents")
public class AdminAgentController {

    private final AgentRepository agentRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;

    public AdminAgentController(AgentRepository agentRepo, UserRepository userRepo,
            NotificationService notificationService) {
        this.agentRepo = agentRepo;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS')")
    public ApiResponse<List<AgentResponse>> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) AgentStatus status,
            @RequestParam(required = false) AgentTier tier,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Specification<Agent> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            preds.add(cb.isNull(root.get("deletedAt")));
            if (status != null) preds.add(cb.equal(root.get("status"), status));
            if (tier != null) preds.add(cb.equal(root.get("tier"), tier));
            if (!search.isBlank()) {
                String like = "%" + search.toLowerCase() + "%";
                preds.add(cb.or(
                        cb.like(cb.lower(root.get("companyName")), like),
                        cb.like(cb.lower(root.join("user").get("email")), like)));
            }
            return cb.and(preds.toArray(new Predicate[0]));
        };
        Page<Agent> pg = agentRepo.findAll(spec,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));

        List<AgentResponse> data = pg.getContent().stream().map(AgentResponse::from).toList();
        return ApiResponse.ok(data, PageMeta.of(page, size, pg.getTotalElements()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS')")
    public ApiResponse<AgentResponse> get(@PathVariable UUID id) {
        Agent agent = agentRepo.findById(id)
                .filter(a -> a.getDeletedAt() == null)
                .orElseThrow(() -> BusinessException.notFound("Agent"));
        return ApiResponse.ok(AgentResponse.from(agent));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<AgentResponse> approve(@PathVariable UUID id,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        Agent agent = agentRepo.findById(id)
                .filter(a -> a.getDeletedAt() == null)
                .orElseThrow(() -> BusinessException.notFound("Agent"));

        if (agent.getStatus() == AgentStatus.ACTIVE)
            throw BusinessException.conflict("ALREADY_ACTIVE", "Agent is already active");

        User actor = userRepo.findById(principal.getUserId()).orElseThrow();
        agent.setStatus(AgentStatus.ACTIVE);
        agent.setApprovedAt(Instant.now());
        agent.setApprovedBy(actor);

        // Activate the associated user account
        agent.getUser().setActive(true);

        Agent saved = agentRepo.save(agent);
        notificationService.sendAgentApproval(saved);
        return ApiResponse.ok(AgentResponse.from(saved));
    }

    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<AgentResponse> suspend(@PathVariable UUID id) {
        Agent agent = agentRepo.findById(id)
                .filter(a -> a.getDeletedAt() == null)
                .orElseThrow(() -> BusinessException.notFound("Agent"));

        agent.setStatus(AgentStatus.SUSPENDED);
        return ApiResponse.ok(AgentResponse.from(agentRepo.save(agent)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<AgentResponse> update(@PathVariable UUID id,
            @Valid @RequestBody AgentUpdateRequest req) {
        Agent agent = agentRepo.findById(id)
                .filter(a -> a.getDeletedAt() == null)
                .orElseThrow(() -> BusinessException.notFound("Agent"));

        if (req.tier() != null) agent.setTier(req.tier());
        if (req.markupPercent() != null) agent.setMarkupPercent(req.markupPercent());
        if (req.commissionRate() != null) agent.setCommissionRate(req.commissionRate());
        if (req.creditLimit() != null) agent.setCreditLimit(req.creditLimit());

        return ApiResponse.ok(AgentResponse.from(agentRepo.save(agent)));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS')")
    public ApiResponse<?> stats() {
        long total = agentRepo.count();
        long pending = agentRepo.countByStatusAndDeletedAtIsNull(AgentStatus.PENDING);
        long active = agentRepo.countByStatusAndDeletedAtIsNull(AgentStatus.ACTIVE);
        long suspended = agentRepo.countByStatusAndDeletedAtIsNull(AgentStatus.SUSPENDED);
        return ApiResponse.ok(java.util.Map.of(
                "total", total,
                "pending", pending,
                "active", active,
                "suspended", suspended));
    }
}
