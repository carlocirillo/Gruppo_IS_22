package com.ambulatorio.controller;

import com.ambulatorio.DTO.response.StatisticheDTO;
import com.ambulatorio.boundary.AreaAmministratoreView;
import com.ambulatorio.entity.Paziente;
import com.ambulatorio.entity.Amministratore;
import com.ambulatorio.entity.Medico;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class PrenotazioneController {

    private CalendarioController calendarioController;
    private NotificaController notificaController;

    public PrenotazioneController(CalendarioController calendarioController, NotificaController notificaController){

        this.calendarioController = calendarioController;
        this.notificaController = notificaController;
    }

    public boolean setStatoPrenotazione(Long idPrenotazione, com.ambulatorio.entity.enums.StatoPrenotazione nuovoStato) {
        try {
            com.ambulatorio.database.GestorePersistenza gestore = new com.ambulatorio.database.GestorePersistenza();
            com.ambulatorio.entity.Prenotazione prenotazione = gestore.trovaPerId(com.ambulatorio.entity.Prenotazione.class, idPrenotazione);

            if (prenotazione != null) {
                prenotazione.setStato(nuovoStato);
                gestore.aggiorna(prenotazione);

                // Se la prenotazione viene annullata, la fascia oraria torna libera
                if (nuovoStato == com.ambulatorio.entity.enums.StatoPrenotazione.ANNULLATA) {
                    com.ambulatorio.entity.FasciaOraria fascia = prenotazione.getFasciaOraria();
                    if (fascia != null) {
                        fascia.setStato(com.ambulatorio.entity.enums.StatoFascia.LIBERA);
                        gestore.aggiorna(fascia);
                    }
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public java.util.List<com.ambulatorio.entity.Prenotazione> getPrenotazioniMedico(Long idMedico) {
        com.ambulatorio.database.GestorePersistenza gestore = new com.ambulatorio.database.GestorePersistenza();
        // Cerchiamo le prenotazioni filtrando per il medico della fascia oraria
        return gestore.cercaPerCampo(com.ambulatorio.entity.Prenotazione.class, "fasciaOraria.medico.id", idMedico);
    }
}
