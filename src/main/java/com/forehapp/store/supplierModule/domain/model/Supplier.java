package com.forehapp.store.supplierModule.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "suppliers")
@Getter @Setter
@NoArgsConstructor
public class Supplier {

    @Id
    @Column(name = "supplier_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_name", nullable = false, length = 255)
    private String businessName;

    @Column(name = "tax_id", nullable = false, unique = true, length = 50)
    private String taxId;

    @Column(name = "contact_name", length = 150)
    private String contactName;

    @Column(length = 50)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;

    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    @Column(nullable = false, length = 50)
    private String status;

    @PrePersist
    public void prePersist() {
        if (registrationDate == null) registrationDate = LocalDate.now();
    }
}
