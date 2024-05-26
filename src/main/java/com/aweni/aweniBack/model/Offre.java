package com.aweni.aweniBack.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "offres")
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@RequiredArgsConstructor
public class Offre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(TemporalType.DATE)
    private String name;
    private Date datepubart;
    @Temporal(TemporalType.DATE)
    private Date datefinart;
    private String descriptionart;
    private String proposition;
    private String inventoryStatus;
    private String categor;
    private String image;
    private String Type;
}

