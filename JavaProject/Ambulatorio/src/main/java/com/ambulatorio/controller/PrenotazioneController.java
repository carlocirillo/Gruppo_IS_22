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

    /*
     * Metodo aggiunto per il caso d'uso PrenotaVisita.
     * Controlla se la fascia oraria selezionata esiste ed è libera.
     */
    public boolean verificaDisponibilitaFascia(Long idFasciaOraria) {
        try {
            if (idFasciaOraria == null) {
                return false;
            }

            GestorePersistenza gestore = new GestorePersistenza();
            FasciaOraria fasciaOraria = gestore.trovaPerId(FasciaOraria.class, idFasciaOraria);

            return fasciaOraria != null && fasciaOraria.getStato() == StatoFascia.LIBERA;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * Metodo principale del caso d'uso PrenotaVisita.
     * Crea una nuova prenotazione, la associa al paziente,
     * occupa la fascia oraria e genera la notifica di conferma.
     */
    public boolean effettuaPrenotazione(Long idPaziente, Long idFasciaOraria) {
        try {
            if (idPaziente == null || idFasciaOraria == null) {
                return false;
            }

            GestorePersistenza gestore = new GestorePersistenza();

            Paziente paziente = gestore.trovaPerId(Paziente.class, idPaziente);
            FasciaOraria fasciaOraria = gestore.trovaPerId(FasciaOraria.class, idFasciaOraria);

            if (paziente == null || fasciaOraria == null) {
                return false;
            }

            if (fasciaOraria.getStato() != StatoFascia.LIBERA) {
                return false;
            }

            Prenotazione prenotazione = new Prenotazione();
            prenotazione.setPaziente(paziente);
            prenotazione.setFasciaOraria(fasciaOraria);
            prenotazione.setDataCreazione(LocalDate.now());
            prenotazione.setStato(StatoPrenotazione.PRENOTATA);

            boolean prenotazioneSalvata = gestore.salva(prenotazione);

            if (!prenotazioneSalvata) {
                return false;
            }

            fasciaOraria.setStato(StatoFascia.OCCUPATA);
            gestore.aggiorna(fasciaOraria);

            inviaNotificaConferma(prenotazione, gestore);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /*
     * Metodo privato usato dal caso d'uso PrenotaVisita.
     * Per ora la notifica viene creata qui, perché NotificaController nel progetto è ancora vuoto.
     */
    private void inviaNotificaConferma(Prenotazione prenotazione, GestorePersistenza gestore) {
        if (prenotazione == null || prenotazione.getPaziente() == null) {
            return;
        }

        Notifica notifica = new Notifica();
        notifica.setTipo(TipoNotifica.CONFERMA);
        notifica.setDestinatario(prenotazione.getPaziente());
        notifica.setDataInvio(LocalDateTime.now());
        notifica.setMessaggio("Prenotazione confermata.");

        gestore.salva(notifica);
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
