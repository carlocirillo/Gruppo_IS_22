package com.ambulatorio.dto.response;

public record MedicoDto(
        Long id,
        String nome,
        String cognome,
        SpecializzazioneDto specializzazione
) {
}
