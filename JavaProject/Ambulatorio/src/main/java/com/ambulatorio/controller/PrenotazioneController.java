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
    private final GestorePersistenza gestorePersistenza;

    public PrenotazioneController(GestorePersistenza gestore){
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
     * Metodo principale del caso d'uso PrenotaVisita.
     * Crea una prenotazione, associa paziente e fascia oraria,
     * cambia lo stato della fascia in OCCUPATA e genera una notifica.
     */
    public void effettuaPrenotazione(Long idPaziente, Long idFasciaOraria) {
        // 1. Validazione input base
        if (idPaziente == null || idFasciaOraria == null) {
            throw new IllegalArgumentException("I parametri Paziente e Fascia Oraria non possono essere nulli");
        }

        // 2. Recupero entità e validazione
        Paziente paziente = gestorePersistenza.trovaPerId(Paziente.class, idPaziente);
        if (paziente == null) {
            throw new EntityNotFoundException("Nessun paziente trovato con ID: " + idPaziente);
        }

        FasciaOraria fasciaOraria = gestorePersistenza.trovaPerId(FasciaOraria.class, idFasciaOraria);
        if (fasciaOraria == null) {
            throw new EntityNotFoundException("Nessuna fascia oraria trovata con ID: " + idFasciaOraria);
        }

        // 3. Validazione regole di business
        if (fasciaOraria.getStato() != StatoFascia.LIBERA) {
            throw new IllegalStateException("La fascia oraria selezionata non è più disponibile");
        }

        // 4. Preparazione modifiche in memoria
        fasciaOraria.setStato(StatoFascia.OCCUPATA);

        Prenotazione prenotazione = new Prenotazione();
        prenotazione.setPaziente(paziente);
        prenotazione.setFasciaOraria(fasciaOraria);
        prenotazione.setDataCreazione(LocalDate.now());
        prenotazione.setStato(StatoPrenotazione.PRENOTATA);

        // 5. Esecuzione operazioni su DB
        gestorePersistenza.aggiorna(fasciaOraria);
        if (!gestorePersistenza.salva(prenotazione)) {
            throw new RuntimeException("Errore durante il salvataggio della prenotazione");
        }

        // 6. Invio notifica
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
     */
    public List<PrenotazioneDto> getPrenotazioniPaziente(Long idPaziente) {
        List<Prenotazione> prenotazioni = gestorePersistenza.cercaPerCampo(
                Prenotazione.class,
                "paziente.id",
                idPaziente
        );

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

        StatisticheDto reportVuoto = new StatisticheDto(
                Instant.now(),
                dataInizio,
                dataFine,
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                0,
                0
        );

        if(!listaPrenotazioni.isEmpty()){

            Map<Long, Integer> perMedico = new HashMap<>();
            Map<Long, Integer> perSpec = new HashMap<>();
            Map<LocalDate, Integer> perGiorno = new HashMap<>();
            Map<String, Integer> perStato = new HashMap<>();

            Set<Long> pazientiUnici = new HashSet<>();

            for(Prenotazione p : listaPrenotazioni){

                StatoPrenotazione stato = p.getStato();
                perStato.put(stato.toString(), perStato.getOrDefault(stato.toString(), 0) + 1);

                LocalDate giorno = p.getDataPrenotazione();
                perGiorno.put(giorno, perGiorno.getOrDefault(giorno, 0) + 1);

                FasciaOraria f = p.getFasciaOraria();
                Medico m = f.getMedico();
                perMedico.put(m.getId(), perMedico.getOrDefault(m.getId(), 0)+ 1);

                Specializzazione s = m.getSpecializzazione();
                perSpec.put(s.getId(), perSpec.getOrDefault(s.getId(), 0) + 1);

                pazientiUnici.add(p.getPaziente().getId());
            }

            int countPazientiUnici = pazientiUnici.size();
            float mediaPazienti = (float) listaPrenotazioni.size() / countPazientiUnici;

            return new StatisticheDto(Instant.now(), dataInizio, dataFine, perMedico,
                    perSpec, perGiorno, perStato, countPazientiUnici, mediaPazienti);
        }

        return reportVuoto;
    }
}