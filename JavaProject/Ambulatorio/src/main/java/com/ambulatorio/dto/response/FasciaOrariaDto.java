package com.ambulatorio.dto.response;

import com.ambulatorio.entity.enums.StatoFascia;

import java.time.LocalDate;
import java.time.LocalTime;

public record FasciaOrariaDto(
        Long id,
        LocalTime orainizio,
        LocalTime oraFine,
        LocalDate data,
        StatoFascia stato
) {
}
