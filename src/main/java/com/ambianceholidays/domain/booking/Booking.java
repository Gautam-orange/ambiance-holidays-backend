package com.ambianceholidays.domain.booking;

import com.ambianceholidays.domain.agent.Agent;
import com.ambianceholidays.domain.customer.Customer;
import com.ambianceholidays.domain.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@SQLRestriction("deleted_at IS NULL")
public class Booking {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private Agent agent;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "subtotal_cents", nullable = false)
    private int subtotalCents = 0;

    @Column(name = "markup_cents", nullable = false)
    private int markupCents = 0;

    @Column(name = "commission_cents", nullable = false)
    private int commissionCents = 0;

    @Column(name = "vat_cents", nullable = false)
    private int vatCents = 0;

    @Column(name = "total_cents", nullable = false)
    private int totalCents = 0;

    @Column(name = "vat_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal vatRate = new BigDecimal("15.00");

    @Column(name = "markup_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal markupRate = BigDecimal.ZERO;

    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate = BigDecimal.ZERO;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by_id")
    private User cancelledBy;

    @Column(name = "cancellation_fee_cents", nullable = false)
    private int cancellationFeeCents = 0;

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "is_enquiry", nullable = false)
    private boolean isEnquiry = false;

    @Column(name = "enquiry_converted_at")
    private Instant enquiryConvertedAt;

    @Column(name = "enquiry_declined_at")
    private Instant enquiryDeclinedAt;

    @Column(name = "cancelled_by_type", length = 10)
    private String cancelledByType;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingItem> items = new ArrayList<>();

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public Agent getAgent() { return agent; }
    public void setAgent(Agent agent) { this.agent = agent; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public int getSubtotalCents() { return subtotalCents; }
    public void setSubtotalCents(int subtotalCents) { this.subtotalCents = subtotalCents; }
    public int getMarkupCents() { return markupCents; }
    public void setMarkupCents(int markupCents) { this.markupCents = markupCents; }
    public int getCommissionCents() { return commissionCents; }
    public void setCommissionCents(int commissionCents) { this.commissionCents = commissionCents; }
    public int getVatCents() { return vatCents; }
    public void setVatCents(int vatCents) { this.vatCents = vatCents; }
    public int getTotalCents() { return totalCents; }
    public void setTotalCents(int totalCents) { this.totalCents = totalCents; }
    public BigDecimal getVatRate() { return vatRate; }
    public void setVatRate(BigDecimal vatRate) { this.vatRate = vatRate; }
    public BigDecimal getMarkupRate() { return markupRate; }
    public void setMarkupRate(BigDecimal markupRate) { this.markupRate = markupRate; }
    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }
    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public Instant getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }
    public User getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(User cancelledBy) { this.cancelledBy = cancelledBy; }
    public int getCancellationFeeCents() { return cancellationFeeCents; }
    public void setCancellationFeeCents(int cancellationFeeCents) { this.cancellationFeeCents = cancellationFeeCents; }
    public LocalDate getServiceDate() { return serviceDate; }
    public void setServiceDate(LocalDate serviceDate) { this.serviceDate = serviceDate; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
    public List<BookingItem> getItems() { return items; }
    public boolean isEnquiry() { return isEnquiry; }
    public void setEnquiry(boolean enquiry) { isEnquiry = enquiry; }
    public Instant getEnquiryConvertedAt() { return enquiryConvertedAt; }
    public void setEnquiryConvertedAt(Instant enquiryConvertedAt) { this.enquiryConvertedAt = enquiryConvertedAt; }
    public Instant getEnquiryDeclinedAt() { return enquiryDeclinedAt; }
    public void setEnquiryDeclinedAt(Instant enquiryDeclinedAt) { this.enquiryDeclinedAt = enquiryDeclinedAt; }
    public String getCancelledByType() { return cancelledByType; }
    public void setCancelledByType(String cancelledByType) { this.cancelledByType = cancelledByType; }
}
