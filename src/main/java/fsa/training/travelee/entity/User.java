package fsa.training.travelee.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false, unique = true)
    private String email;
    private String phoneNumber;
    private String password;
    private String fullName;
    private String address;
    @Column(nullable = false)
    private String status;
    private String provider;      // GOOGLE, FORM
    private String providerId;    // ID Google
    @Column(name = "reset_password_token")
    private String resetPasswordToken;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.emailVerified = false;
        this.phoneVerified = false;

        if (this.provider == null || this.provider.isBlank()) {
            this.provider = "FORM";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SupportRequest> supportRequests = new HashSet<>();

//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<Booking> bookings = new HashSet<>();
//
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<PromotionUsage> promotionUsages = new HashSet<>();

}
