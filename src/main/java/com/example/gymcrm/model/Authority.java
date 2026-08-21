package com.example.gymcrm.model;

import jakarta.persistence.*;

@Entity
@Table(name = "authorities",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_authority",
                columnNames = {"user_id", "authority"}))
public class Authority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Role authority;

    public Authority() {}

    public Authority(User user, Role authority) {
        this.user = user;
        this.authority = authority;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Role getAuthority() { return authority; }
    public void setAuthority(Role authority) { this.authority = authority; }
}