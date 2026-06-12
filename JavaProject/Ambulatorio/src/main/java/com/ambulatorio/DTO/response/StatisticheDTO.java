package com.ambulatorio.DTO.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

public record StatisticheDTO(Instant dataCreazione, LocalDate dataInizio, LocalDate dataFine,
                             Map<Long, Integer> prenotazioniTotaliPerMedico,
                             Map<Long, Integer> prenotazioniTotaliPerSpec,
                             Map<LocalDate, Integer> prenotazioniTotaliPerGiorno,
                             Map<String, Integer> prenotazioniPerStato,
                             int pazientiUniciPrenotazioni, float mediaPrenotazioniPaziente,
                             float anticipoMedioGiorniPrenotazione) {

}
