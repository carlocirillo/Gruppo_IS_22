package com.ambulatorio.entity;

import com.ambulatorio.entity.enums.StatoPrenotazione;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Prenotazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dataCreazione;

    @Enumerated(EnumType.STRING)
    private StatoPrenotazione stato;

    // Molte prenotazioni appartengono a un Paziente
    @ManyToOne
    @JoinColumn(name = "paziente_id")
    private Paziente paziente;

    // Una prenotazione occupa esattamente una Fascia Oraria
    @OneToOne
    @JoinColumn(name = "fascia_oraria_id")
    private FasciaOraria fasciaOraria;
}
