package com.ambulatorio.controller;

import com.ambulatorio.database.GestorePersistenza;
import com.ambulatorio.entity.*;
import com.ambulatorio.entity.enums.StatoFascia;
import com.ambulatorio.entity.enums.StatoPrenotazione;

import java.util.List;

public class PrenotazioneController {

    private CalendarioController calendarioController;
    private NotificaController notificaController;

    public PrenotazioneController(CalendarioController calendarioController, NotificaController notificaController){

        this.calendarioController = calendarioController;
        this.notificaController = notificaController;
    }

    public boolean setStatoPrenotazione(Long idPrenotazione, StatoPrenotazione nuovoStato) {
        try {
            GestorePersistenza gestore = new GestorePersistenza();
            Prenotazione prenotazione = gestore.trovaPerId(Prenotazione.class, idPrenotazione);

            if (prenotazione != null) {
                prenotazione.setStato(nuovoStato);
                gestore.aggiorna(prenotazione);

                // Se la prenotazione viene annullata, la fascia oraria torna libera
                if (nuovoStato == StatoPrenotazione.ANNULLATA) {
                    FasciaOraria fascia = prenotazione.getFasciaOraria();
                    if (fascia != null) {
                        fascia.setStato(StatoFascia.LIBERA);
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

    public List<Prenotazione> getPrenotazioniMedico(Long idMedico) {
        GestorePersistenza gestore = new GestorePersistenza();
        // Cerchiamo le prenotazioni filtrando per il medico della fascia oraria
        return gestore.cercaPerCampo(Prenotazione.class, "fasciaOraria.medico.id", idMedico);
    }
}
