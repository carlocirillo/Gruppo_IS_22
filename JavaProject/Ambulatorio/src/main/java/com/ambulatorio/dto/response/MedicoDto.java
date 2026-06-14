package com.ambulatorio.DTO.response;

public record MedicoDto(
        Long id,
        String nome,
        String cognome,
        SpecializzazioneDto specializzazione
) {
}
