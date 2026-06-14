package com.ambulatorio.dto.response;

public record PazienteDto(
        Long id,
        String nome,
        String cognome,
        String codiceFiscale,
        String numeroCellulare
) {
}
