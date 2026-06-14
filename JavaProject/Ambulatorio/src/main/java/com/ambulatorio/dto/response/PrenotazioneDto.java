package com.ambulatorio.dto.response;

import com.ambulatorio.entity.enums.StatoPrenotazione;

public record PrenotazioneDto(
        Long id,
        PazienteDto paziente,
        MedicoDto medico,
        FasciaOrariaDto fasciaOraria,
        StatoPrenotazione stato
) {
}
