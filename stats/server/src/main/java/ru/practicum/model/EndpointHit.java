package ru.practicum.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EndpointHit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String app; // название сервиса

    @Column(nullable = false)
    private String uri; // страница, куда зашли

    @Column(nullable = false)
    private String ip; // конкретный чел

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
}

