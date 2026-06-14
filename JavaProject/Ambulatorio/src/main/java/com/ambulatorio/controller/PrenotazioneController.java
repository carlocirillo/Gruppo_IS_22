package com.ambulatorio.controller;

import com.ambulatorio.database.GestorePersistenza;
import com.ambulatorio.dto.response.*;
import com.ambulatorio.dto.response.StatisticheDto;
import com.ambulatorio.entity.*;
import com.ambulatorio.entity.enums.StatoPrenotazione;
import com.ambulatorio.entity.enums.StatoFascia;
import com.ambulatorio.entity.enums.TipoNotifica;
import jakarta.persistence.EntityNotFoundException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

public class PrenotazioneController {
    private final CalendarioController calendarioController;
    private final NotificaController notificaController;
    private final GestorePersistenza gestorePersistenza;

    private PrenotazioneController(GestorePersistenza gestore, CalendarioController calendarioController, NotificaController notificaController){
        this.calendarioController = calendarioController;
        this.notificaController = notificaController;
        this.gestorePersistenza = gestore;
    }

    public void aggiornaStatoPrenotazione(long idMedico, long idPrenotazione, StatoPrenotazione nuovoStato) {
        // Validazione dello stato: deve essere EFFETTUATA o NON_PRESENTATO
        if (nuovoStato != StatoPrenotazione.EFFETTUATA && nuovoStato != StatoPrenotazione.NON_PRESENTATO) {
            throw new IllegalArgumentException("Stato prenotazione non valido");
        }

        Prenotazione prenotazione = gestorePersistenza.trovaPerId(Prenotazione.class, idPrenotazione);
        if (prenotazione == null) {
            throw new EntityNotFoundException("Impossibile trovare la prenotazione con ID: " + idPrenotazione);
        }

        FasciaOraria fascia = prenotazione.getFasciaOraria();
        if (fascia == null || fascia.getMedico() == null || fascia.getMedico().getId() != idMedico) {
            throw new SecurityException("Il medico non è autorizzato a modificare questa prenotazione.");
        }

        prenotazione.setStato(nuovoStato);

        gestorePersistenza.aggiorna(prenotazione);
    }

    public List<PrenotazioneDto> getPrenotazioniMedico(Long idMedico) {
        List<Prenotazione> prenotazioni = gestorePersistenza.cercaPerCampo(Prenotazione.class, "fasciaOraria.medico.id",  idMedico);

        List<PrenotazioneDto> prenotazioniDto = new ArrayList<>();

        for (Prenotazione pren : prenotazioni) {
            PrenotazioneDto prenDto = new PrenotazioneDto(
                    pren.getId(),
                    new PazienteDto(
                            pren.getPaziente().getId(),
                            pren.getPaziente().getNome(),
                            pren.getPaziente().getCognome(),
                            pren.getPaziente().getCodiceFiscale(),
                            pren.getPaziente().getNumeroCellulare()
                    ),
                    new MedicoDto(
                            pren.getFasciaOraria().getMedico().getId(),
                            pren.getFasciaOraria().getMedico().getNome(),
                            pren.getFasciaOraria().getMedico().getCognome(),
                            new SpecializzazioneDto(
                                    pren.getFasciaOraria().getMedico().getSpecializzazione().getId(),
                                    pren.getFasciaOraria().getMedico().getSpecializzazione().getNome()
                            )
                    ),
                    new FasciaOrariaDto(
                            pren.getFasciaOraria().getId(),
                            pren.getFasciaOraria().getOraInizio(),
                            pren.getFasciaOraria().getOraFine(),
                            pren.getFasciaOraria().getData(),
                            pren.getFasciaOraria().getStato()
                    ),
                    pren.getStato()
            );
            prenotazioniDto.add(prenDto);
        }
        return prenotazioniDto;
    }

    /*
     * Metodo helper aggiunto per il caso d'uso PrenotaVisita.
     * Verifica se la fascia oraria selezionata esiste ed è libera.
     */
    private boolean verificaDisponibilitaFascia(Long idFasciaOraria) {
        try {
            if (idFasciaOraria == null) {
                return false;
            }

            FasciaOraria fasciaOraria = gestorePersistenza.trovaPerId(FasciaOraria.class, idFasciaOraria);

            return (fasciaOraria != null && fasciaOraria.getStato() == StatoFascia.LIBERA);

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
    public void effettuaPrenotazione(Long idPaziente, Long idFasciaOraria) {
        if (idPaziente == null || idFasciaOraria == null) {
            throw new IllegalArgumentException("Paziente o fascia oraria inesistente");
        }

        Paziente paziente = gestorePersistenza.trovaPerId(Paziente.class, idPaziente);

        if (!verificaDisponibilitaFascia(idFasciaOraria)) {
            throw new IllegalArgumentException("La fascia oraria selezionata è già associata ad una prenotazione");
        }

        FasciaOraria fasciaOraria = gestorePersistenza.trovaPerId(FasciaOraria.class, idFasciaOraria);

        Prenotazione prenotazione = new Prenotazione();
        prenotazione.setPaziente(paziente);
        prenotazione.setFasciaOraria(fasciaOraria);
        prenotazione.setDataCreazione(LocalDate.now());
        prenotazione.setStato(StatoPrenotazione.PRENOTATA);

        if (!gestorePersistenza.salva(prenotazione)) {
            throw new RuntimeException("Salvataggio della prenotazione nel database fallito");
        }

        fasciaOraria.setStato(StatoFascia.OCCUPATA);
        gestorePersistenza.aggiorna(fasciaOraria);

        inviaNotificaConferma(prenotazione);

    }

    /*
     * Metodo privato usato da effettuaPrenotazione.
     * Crea la notifica di conferma per il paziente.
     */

    private void inviaNotificaConferma(Prenotazione prenotazione) {
        if (prenotazione == null || prenotazione.getPaziente() == null) {
            return;
        }

        Notifica notifica = new Notifica();

        notifica.setDestinatario(prenotazione.getPaziente());
        notifica.setMessaggio("Prenotazione confermata.");
        notifica.setDataInvio(java.time.LocalDateTime.now());

        notifica.setTipo(TipoNotifica.CONFERMA);

        gestorePersistenza.salva(notifica);
    }

    /*
     * Metodo aggiunto per recuperare le prenotazioni di un paziente.
     * Serve per lo storico prenotazioni del caso d'uso PrenotaVisita.
     *

    public List<Prenotazione> getPrenotazioniPaziente(Long idPaziente) {

        return gestore.cercaPerCampo(
                com.ambulatorio.entity.Prenotazione.class,
                "paziente.id",
                idPaziente
        );
    }
    */


    public StatisticheDto calcolaReportStatistiche(LocalDate dataInizio, LocalDate dataFine){

        String jpqlPrenotazioni = "SELECT p FROM Prenotazione p WHERE p.data BETWEEN :inizio AND :fine";

        Map<String, Object> parametri = new HashMap<>();
        parametri.put("inizio", dataInizio);
        parametri.put("fine", dataFine);

        List<Prenotazione> listaPrenotazioni = gestorePersistenza.eseguiQueryCustom(
                Prenotazione.class,
                jpqlPrenotazioni,
                parametri
        );

        StatisticheDto statisticheDto = new StatisticheDto(
                Instant.now(),
                dataInizio,
                dataFine,
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                0,
                0,
                0);

        /* COSTRUISCI LE STATISTICHE
        *
        *
        *
         */

        return statisticheDto;
    }
}