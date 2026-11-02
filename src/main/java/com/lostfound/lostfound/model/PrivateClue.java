package com.lostfound.lostfound.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "private_clues")
public class PrivateClue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @NotBlank(message = "Clue description is required")
    @Column(length = 2000, nullable = false)
    private String clueDescription;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public PrivateClue() {
    }

    public PrivateClue(Item item, String clueDescription) {
        this.item = item;
        this.clueDescription = clueDescription;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getClueDescription() {
        return clueDescription;
    }

    public void setClueDescription(String clueDescription) {
        this.clueDescription = clueDescription;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
