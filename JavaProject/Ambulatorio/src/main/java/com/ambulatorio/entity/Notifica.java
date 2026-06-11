package com.ambulatorio.entity;

import com.ambulatorio.entity.enums.TipoNotifica;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Notifica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoNotifica tipo;

    private String messaggio;

    private LocalDateTime dataInvio;

    // Molte notifiche vanno a un singolo Utente
    @ManyToOne
    @JoinColumn(name = "destinatario_id")
    private Paziente destinatario;
}