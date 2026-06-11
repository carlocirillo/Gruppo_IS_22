package com.ambulatorio.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Medico extends Utente {

    @ManyToOne
    @JoinColumn(name = "specializzazione_id")
    private Specializzazione specializzazione;

}
