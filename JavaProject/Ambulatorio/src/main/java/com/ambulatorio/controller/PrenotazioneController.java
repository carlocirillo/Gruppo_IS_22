package com.ambulatorio.controller;

import com.ambulatorio.dto.response.StatisticheDto;
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

    /*
     * Metodo aggiunto per il caso d'uso PrenotaVisita.
     * Verifica se la fascia oraria selezionata esiste ed è libera.
     */
    public boolean verificaDisponibilitaFascia(Long idFasciaOraria) {
        try {
            if (idFasciaOraria == null) {
                return false;
            }

            com.ambulatorio.database.GestorePersistenza gestore =
                    new com.ambulatorio.database.GestorePersistenza();

            com.ambulatorio.entity.FasciaOraria fasciaOraria =
                    gestore.trovaPerId(com.ambulatorio.entity.FasciaOraria.class, idFasciaOraria);

            return fasciaOraria != null &&
                    fasciaOraria.getStato() == com.ambulatorio.entity.enums.StatoFascia.LIBERA;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * Metodo principale del caso d'uso PrenotaVisita.
     * Crea una prenotazione, associa paziente e fascia oraria,
     * cambia lo stato della fascia in OCCUPATA e genera una notifica.
     */
    public boolean effettuaPrenotazione(Long idPaziente, Long idFasciaOraria) {
        try {
            if (idPaziente == null || idFasciaOraria == null) {
                return false;
            }

            com.ambulatorio.database.GestorePersistenza gestore =
                    new com.ambulatorio.database.GestorePersistenza();

            com.ambulatorio.entity.Paziente paziente =
                    gestore.trovaPerId(com.ambulatorio.entity.Paziente.class, idPaziente);

            com.ambulatorio.entity.FasciaOraria fasciaOraria =
                    gestore.trovaPerId(com.ambulatorio.entity.FasciaOraria.class, idFasciaOraria);

            if (paziente == null || fasciaOraria == null) {
                return false;
            }

            if (fasciaOraria.getStato() != com.ambulatorio.entity.enums.StatoFascia.LIBERA) {
                return false;
            }

            com.ambulatorio.entity.Prenotazione prenotazione =
                    new com.ambulatorio.entity.Prenotazione();

            prenotazione.setPaziente(paziente);
            prenotazione.setFasciaOraria(fasciaOraria);
            prenotazione.setDataCreazione(LocalDate.now());
            prenotazione.setStato(com.ambulatorio.entity.enums.StatoPrenotazione.PRENOTATA);

            boolean prenotazioneSalvata = gestore.salva(prenotazione);

            if (!prenotazioneSalvata) {
                return false;
            }

            fasciaOraria.setStato(com.ambulatorio.entity.enums.StatoFascia.OCCUPATA);
            gestore.aggiorna(fasciaOraria);

            inviaNotificaConferma(prenotazione, gestore);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * Metodo privato usato da effettuaPrenotazione.
     * Crea la notifica di conferma per il paziente.
     */
    private void inviaNotificaConferma(
            com.ambulatorio.entity.Prenotazione prenotazione,
            com.ambulatorio.database.GestorePersistenza gestore
    ) {
        if (prenotazione == null || prenotazione.getPaziente() == null) {
            return;
        }

        com.ambulatorio.entity.Notifica notifica =
                new com.ambulatorio.entity.Notifica();

        notifica.setDestinatario(prenotazione.getPaziente());
        notifica.setMessaggio("Prenotazione confermata.");
        notifica.setDataInvio(java.time.LocalDateTime.now());

        /*
         * Se questa riga dà errore, apri TipoNotifica.java
         * e controlla il nome esatto del valore enum.
         */
        notifica.setTipo(com.ambulatorio.entity.enums.TipoNotifica.CONFERMA);

        gestore.salva(notifica);
    }

    /*
     * Metodo aggiunto per recuperare le prenotazioni di un paziente.
     * Serve per lo storico prenotazioni del caso d'uso PrenotaVisita.
     */
    public java.util.List<com.ambulatorio.entity.Prenotazione> getPrenotazioniPaziente(Long idPaziente) {
        com.ambulatorio.database.GestorePersistenza gestore =
                new com.ambulatorio.database.GestorePersistenza();

        return gestore.cercaPerCampo(
                com.ambulatorio.entity.Prenotazione.class,
                "paziente.id",
                idPaziente
        );
    }
}