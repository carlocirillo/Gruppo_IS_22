package com.ambulatorio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public class Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String cognome;

    private String email;

    private String passwordHash;

    private String numeroCellulare;

    @Override
    public String toString() {
        return "Utente{" +
                "id = " + id +
                ", nome='" + nome + '\'' +
                ", cognome='" + cognome + '\'' +
                "}";
    }
}
