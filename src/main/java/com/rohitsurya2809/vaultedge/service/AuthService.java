package com.rohitsurya2809.vaultedge.service;

import com.rohitsurya2809.vaultedge.dto.RegisterRequest;
import com.rohitsurya2809.vaultedge.dto.CustomerResponse;
import com.rohitsurya2809.vaultedge.dto.LoginRequest;
import com.rohitsurya2809.vaultedge.dto.LoginResponse;
import com.rohitsurya2809.vaultedge.exception.BadRequestException;
import com.rohitsurya2809.vaultedge.model.AuthUser;
import com.rohitsurya2809.vaultedge.model.Customer;
import com.rohitsurya2809.vaultedge.repository.AuthUserRepository;
import com.rohitsurya2809.vaultedge.repository.CustomerRepository;
import com.rohitsurya2809.vaultedge.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final CustomerRepository customerRepository;
    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(CustomerRepository customerRepository,
                       AuthUserRepository authUserRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager) {
        this.customerRepository = customerRepository;
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Register a new Customer and create corresponding AuthUser.
     */
    @Transactional
    public Customer register(RegisterRequest req) {
        // email uniqueness
        customerRepository.findByEmail(req.getEmail()).ifPresent(c -> {
            throw new BadRequestException("Email already exists");
        });

        Customer c = Customer.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword())) // keep hashed in customer as well
                .phone(req.getPhone())
                .address(req.getAddress())
                .build();

        Customer saved = customerRepository.save(c);

        // create AuthUser -> username = email (can change later)
        AuthUser au = AuthUser.builder()
                .id(UUID.randomUUID())
                .customer(saved)
                .username(saved.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .roles("ROLE_USER")
                .enabled(true)
                .build();

        authUserRepository.save(au);

        return saved;
    }

    /**
     * Authenticate credentials and return JWT
     */
    public LoginResponse login(LoginRequest req) {
    Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
    );

    AuthUser authUser = authUserRepository.findByUsername(req.getUsername())
            .orElseThrow(() -> new BadRequestException("Invalid user"));

    String token = jwtUtil.generateToken(authUser.getUsername(), authUser.getCustomer().getId());

    return LoginResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .expiresIn(jwtUtil.getRemainingSeconds(token))
            .build();
}

}
