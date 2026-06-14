package com.ambulatorio.DTO.response;

import com.ambulatorio.entity.enums.StatoPrenotazione;

public record PrenotazioneDto(
        Long id,
        PazienteDto paziente,
        MedicoDto medico,
        FasciaOrariaDto fasciaOraria,
        StatoPrenotazione stato
) {
}
