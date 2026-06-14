package com.ambulatorio.dto.request;

public record NuovoPazienteDto(
        String nome,
        String cognome,
        String email,
        String password,
        String codiceFiscale,
        String numeroCellulare
        ){}
