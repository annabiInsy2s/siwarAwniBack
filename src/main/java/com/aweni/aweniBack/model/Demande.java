package com.aweni.aweniBack.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "demandes")
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@RequiredArgsConstructor
public class Demande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

}
