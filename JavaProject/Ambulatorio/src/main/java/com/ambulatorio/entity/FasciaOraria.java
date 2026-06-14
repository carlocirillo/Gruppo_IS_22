package com.ambulatorio.entity;

import com.ambulatorio.entity.enums.StatoFascia;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class FasciaOraria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate data;

    private LocalTime oraInizio;

    private LocalTime oraFine;

    private StatoFascia stato;

    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Medico medico;

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public String toString() {
        return oraInizio.format(formatter) + " - " + oraFine.format(formatter);
    }
}
