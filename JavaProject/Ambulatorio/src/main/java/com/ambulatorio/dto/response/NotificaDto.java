package com.ambulatorio.dto.response;

import com.ambulatorio.entity.enums.TipoNotifica;

import java.time.Instant;

public record NotificaDto(
        Long id,
        PrenotazioneDto prenotazione,
        TipoNotifica tipo,
        String messaggio,
        Instant dataInvio
) {
}
