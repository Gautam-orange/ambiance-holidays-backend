package com.ambianceholidays.domain.customer;

import com.ambianceholidays.common.domain.BaseEntity;
import com.ambianceholidays.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 30)
    private String whatsapp;

    @Column(length = 100)
    private String nationality;

    @Column(name = "passport_no", length = 50)
    private String passportNo;

    @Column(columnDefinition = "TEXT")
    private String address;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
