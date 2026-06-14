package com.ambulatorio.dto.request;

public record NuovoMedicoDto(
        String nome,
        String cognome,
        Long idSpecializzazione,
        String email,
        String password
) {
}
