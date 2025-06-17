package com.example.be.domain;

import com.example.be.domain.enums.LoginType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private UUID userId;


    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    private String provider;

    private String providerId;

    private LocalDateTime createDate;

    private String resolve;

    @Column(columnDefinition = "int default 0")
    private int todayStudyTime;

    @Column(columnDefinition = "int default 0")
    private int totalStudyTime;

    @Column(columnDefinition = "int default 0")
    private int goalStudyTime;


    //Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DDay> dDays;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Participant participant;

}
