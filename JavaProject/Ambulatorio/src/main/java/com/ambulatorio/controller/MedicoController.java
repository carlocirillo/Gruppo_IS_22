package com.ambulatorio.controller;

import com.ambulatorio.database.GestorePersistenza;
import com.ambulatorio.entity.Medico;
import com.ambulatorio.entity.Specializzazione;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Controller responsabile del recupero delle specializzazioni e dei medici.
 *
 * È usato dal caso d'uso PrenotaVisita per permettere al paziente di scegliere
 * prima la specializzazione e poi il medico desiderato.
 */
public class MedicoController {

    private final GestorePersistenza gestorePersistenza;

    public MedicoController() {
        this(new GestorePersistenza());
    }

    public MedicoController(GestorePersistenza gestorePersistenza) {
        this.gestorePersistenza = gestorePersistenza;
    }

    public List<SpecializzazioneInfo> getAllSpecializzazioni() {
        List<Specializzazione> specializzazioni = gestorePersistenza.cercaPerCampi(
                Specializzazione.class,
                Map.of()
        );

        return specializzazioni.stream()
                .sorted(Comparator.comparing(Specializzazione::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(this::toSpecializzazioneInfo)
                .toList();
    }

    public List<MedicoInfo> getAllMedici() {
        List<Medico> medici = gestorePersistenza.cercaPerCampi(Medico.class, Map.of());

        return medici.stream()
                .sorted(Comparator.comparing(Medico::getCognome, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Medico::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(this::toMedicoInfo)
                .toList();
    }

    public List<MedicoInfo> getMediciBySpecializzazione(Long idSpecializzazione) {
        if (idSpecializzazione == null) {
            return new ArrayList<>();
        }

        Specializzazione specializzazione = gestorePersistenza.trovaPerId(
                Specializzazione.class,
                idSpecializzazione
        );

        if (specializzazione == null) {
            return new ArrayList<>();
        }

        List<Medico> medici = gestorePersistenza.cercaPerCampo(
                Medico.class,
                "specializzazione",
                specializzazione
        );

        return medici.stream()
                .sorted(Comparator.comparing(Medico::getCognome, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Medico::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(this::toMedicoInfo)
                .toList();
    }

    private SpecializzazioneInfo toSpecializzazioneInfo(Specializzazione specializzazione) {
        return new SpecializzazioneInfo(
                specializzazione.getId(),
                specializzazione.getNome()
        );
    }

    private MedicoInfo toMedicoInfo(Medico medico) {
        String nomeSpecializzazione = "";
        Long idSpecializzazione = null;

        if (medico.getSpecializzazione() != null) {
            idSpecializzazione = medico.getSpecializzazione().getId();
            nomeSpecializzazione = medico.getSpecializzazione().getNome();
        }

        return new MedicoInfo(
                medico.getId(),
                medico.getNome(),
                medico.getCognome(),
                idSpecializzazione,
                nomeSpecializzazione
        );
    }

    public static class SpecializzazioneInfo {
        private final Long id;
        private final String nome;

        public SpecializzazioneInfo(Long id, String nome) {
            this.id = id;
            this.nome = nome;
        }

        public Long getId() {
            return id;
        }

        public String getNome() {
            return nome;
        }

        @Override
        public String toString() {
            return nome;
        }
    }

    public static class MedicoInfo {
        private final Long id;
        private final String nome;
        private final String cognome;
        private final Long idSpecializzazione;
        private final String nomeSpecializzazione;

        public MedicoInfo(Long id, String nome, String cognome, Long idSpecializzazione, String nomeSpecializzazione) {
            this.id = id;
            this.nome = nome;
            this.cognome = cognome;
            this.idSpecializzazione = idSpecializzazione;
            this.nomeSpecializzazione = nomeSpecializzazione;
        }

        public Long getId() {
            return id;
        }

        public String getNome() {
            return nome;
        }

        public String getCognome() {
            return cognome;
        }

        public Long getIdSpecializzazione() {
            return idSpecializzazione;
        }

        public String getNomeSpecializzazione() {
            return nomeSpecializzazione;
        }

        @Override
        public String toString() {
            return cognome + " " + nome + " - " + nomeSpecializzazione;
        }
    }
}
