package com.ambulatorio.controller;

import com.ambulatorio.database.GestorePersistenza;
import com.ambulatorio.dto.response.FasciaOrariaDto;
import com.ambulatorio.entity.FasciaOraria;
import com.ambulatorio.entity.Medico;
import com.ambulatorio.entity.Specializzazione;
import com.ambulatorio.entity.enums.StatoFascia;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class CalendarioController {

    private final GestorePersistenza gestorePersistenza;

    public CalendarioController(GestorePersistenza gestore) {
        this.gestorePersistenza = Objects.requireNonNull(gestore, "Il gestore della persistenza non può essere null");
    }

    /**
     * Crea una nuova fascia oraria libera per il medico indicato.
     */
    public boolean gestisciFasceOrarie(Long idMedico, LocalDate data, LocalTime oraInizio, LocalTime oraFine) {
        if (idMedico == null || data == null || oraInizio == null || oraFine == null) {
            throw new IllegalArgumentException("Medico, data, ora di inizio e ora di fine sono obbligatori");
        }

        if (!oraFine.isAfter(oraInizio)) {
            throw new IllegalArgumentException("L'ora di fine deve essere successiva all'ora di inizio");
        }

        Medico medico = gestorePersistenza.trovaPerId(Medico.class, idMedico);
        if (medico == null) {
            throw new EntityNotFoundException("Nessun medico trovato con ID: " + idMedico);
        }

        FasciaOraria fasciaOraria = new FasciaOraria();
        fasciaOraria.setMedico(medico);
        fasciaOraria.setData(data);
        fasciaOraria.setOraInizio(oraInizio);
        fasciaOraria.setOraFine(oraFine);
        fasciaOraria.setStato(StatoFascia.LIBERA);

        return gestorePersistenza.salva(fasciaOraria);
    }

    /**
     * Restituisce le fasce libere di un medico nel periodo indicato.
     */
    public List<FasciaOrariaDto> getDisponibilitaPerMedico(Long idMedico, LocalDate dataInizio, LocalDate dataFine) {
        if (idMedico == null) {
            return new ArrayList<>();
        }

        Medico medico = gestorePersistenza.trovaPerId(Medico.class, idMedico);
        if (medico == null) {
            return new ArrayList<>();
        }

        List<FasciaOraria> fasce = gestorePersistenza.cercaPerCampo(
                FasciaOraria.class,
                "medico",
                medico
        );

        return filtraOrdinaEConvertiFasceDisponibili(fasce, dataInizio, dataFine);
    }

    /**
     * Restituisce le fasce libere dei medici appartenenti a una specializzazione.
     */
    public List<FasciaOrariaDto> getDisponibilitaPerSpecializzazione(Long idSpec, LocalDate dataInizio, LocalDate dataFine) {
        if (idSpec == null) {
            return new ArrayList<>();
        }

        Specializzazione specializzazione = gestorePersistenza.trovaPerId(Specializzazione.class, idSpec);
        if (specializzazione == null) {
            return new ArrayList<>();
        }

        List<Medico> medici = gestorePersistenza.cercaPerCampo(
                Medico.class,
                "specializzazione",
                specializzazione
        );

        List<FasciaOrariaDto> disponibilita = new ArrayList<>();

        for (Medico medico : medici) {
            disponibilita.addAll(getDisponibilitaPerMedico(medico.getId(), dataInizio, dataFine));
        }

        disponibilita.sort(
                Comparator.comparing(FasciaOrariaDto::data)
                        .thenComparing(FasciaOrariaDto::orainizio)
        );

        return disponibilita;
    }

    /**
     * Restituisce lo stato corrente della fascia, utile prima della conferma.
     */
    public StatoFascia verificaDisponibilitaFascia(Long idFasciaOraria) {
        if (idFasciaOraria == null) {
            throw new IllegalArgumentException("L'id della fascia oraria non può essere null");
        }

        FasciaOraria fasciaOraria = gestorePersistenza.trovaPerId(FasciaOraria.class, idFasciaOraria);
        if (fasciaOraria == null) {
            throw new EntityNotFoundException("Nessuna fascia oraria trovata con ID: " + idFasciaOraria);
        }

        return fasciaOraria.getStato();
    }

    private List<FasciaOrariaDto> filtraOrdinaEConvertiFasceDisponibili(
            List<FasciaOraria> fasce,
            LocalDate dataInizio,
            LocalDate dataFine
    ) {
        List<FasciaOrariaDto> fasceDisponibili = new ArrayList<>();

        for (FasciaOraria fascia : fasce) {
            if (isDisponibileNelPeriodo(fascia, dataInizio, dataFine)) {
                fasceDisponibili.add(toFasciaOrariaDto(fascia));
            }
        }

        fasceDisponibili.sort(
                Comparator.comparing(FasciaOrariaDto::data)
                        .thenComparing(FasciaOrariaDto::orainizio)
        );

        return fasceDisponibili;
    }

    private boolean isDisponibileNelPeriodo(FasciaOraria fascia, LocalDate dataInizio, LocalDate dataFine) {
        if (fascia == null || fascia.getStato() != StatoFascia.LIBERA) {
            return false;
        }

        if (fascia.getData() == null || fascia.getOraInizio() == null || fascia.getOraFine() == null) {
            return false;
        }

        if (dataInizio != null && fascia.getData().isBefore(dataInizio)) {
            return false;
        }

        if (dataFine != null && fascia.getData().isAfter(dataFine)) {
            return false;
        }

        LocalDate oggi = LocalDate.now();
        LocalTime oraAttuale = LocalTime.now();

        return !fascia.getData().isBefore(oggi)
                && (!fascia.getData().isEqual(oggi) || !fascia.getOraInizio().isBefore(oraAttuale));
    }

    private FasciaOrariaDto toFasciaOrariaDto(FasciaOraria fascia) {
        return new FasciaOrariaDto(
                fascia.getId(),
                fascia.getOraInizio(),
                fascia.getOraFine(),
                fascia.getData(),
                fascia.getStato()
        );
    }
}
