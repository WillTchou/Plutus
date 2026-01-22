package com.project.plutus.user.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(unique = true, nullable = false)
    private UUID id;
    @Column(nullable = false)
    @NotBlank
    private String firstName;
    @Column(nullable = false)
    @NotBlank
    private String lastName;
    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    @Column(nullable = false, unique = true)
    @NotBlank
    @Email(message = "Email address is not valid",
            regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$",
            flags = Pattern.Flag.CASE_INSENSITIVE)
    private String email;
    @Column(nullable = false, unique = true)
    @NotBlank
    @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters")
    private String username;
    @Column(nullable = false)
    @NotBlank
    private String password;
    @Column
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;
    @Column
    @Enumerated(EnumType.STRING)
    private KycState kycState = KycState.NOT_VERIFIED;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }
}
