package com.ambulatorio.DTO.response;

public record PazienteDto(
        Long id,
        String nome,
        String cognome,
        String codiceFiscale,
        String numeroCellulare
) {
}
