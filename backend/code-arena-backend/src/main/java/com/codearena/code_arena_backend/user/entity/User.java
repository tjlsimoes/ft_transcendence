package com.codearena.code_arena_backend.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    private String avatar;
    
    @Column(nullable = false)
    private Integer elo = 1000; // Starting ELO
    
    @Column(nullable = false)
    private Integer wins = 0;
    
    @Column(nullable = false)
    private Integer losses = 0;
    
    @Column(nullable = false)
    private Integer winStreak = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private League league = League.BRONZE;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.OFFLINE;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum League {
        BRONZE,   // 0-999
        SILVER,   // 1000-1999
        GOLD,     // 2000-2999
        MASTER,   // 3000-3999
        LEGEND    // Top 1%
    }
    
    public enum UserStatus {
        ONLINE,
        OFFLINE,
        IN_QUEUE,
        IN_DUEL
    }
}
