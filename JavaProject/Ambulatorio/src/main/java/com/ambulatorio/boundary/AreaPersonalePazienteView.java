package com.ambulatorio.boundary;

import com.ambulatorio.controller.CalendarioController;
import com.ambulatorio.controller.MedicoController;
import com.ambulatorio.controller.PrenotazioneController;
import com.ambulatorio.dto.response.PrenotazioneDto;
import com.ambulatorio.utils.Navigatore;
import com.ambulatorio.utils.SessioneUtente;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class AreaPersonalePazienteView {

    public JPanel contentPane;
    private JLabel lblTitolo;
    private JLabel lblInfo;
    private JButton btnPrenotaVisita;
    private JButton btnStoricoPrenotazioni;
    private JButton btnLogout;
    private JTable tblStoricoPrenotazioni;
    private JScrollPane scrollStorico;

    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter ORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final Navigatore navigatore;
    private final MedicoController medicoController;
    private final CalendarioController calendarioController;
    private final PrenotazioneController prenotazioneController;
    private final Long idPaziente;

    public AreaPersonalePazienteView(
            Navigatore navigatore,
            MedicoController medicoController,
            CalendarioController calendarioController,
            PrenotazioneController prenotazioneController
    ) {
        this.navigatore = navigatore;
        this.medicoController = medicoController;
        this.calendarioController = calendarioController;
        this.prenotazioneController = prenotazioneController;
        this.idPaziente = SessioneUtente.getInstance().getIdUtente();

        inizializzaComponenti();
        collegaEventi();
        caricaStoricoPrenotazioniSilenzioso();
    }


    private void inizializzaComponenti() {
        lblInfo.setText("Scegli una funzione dell'area personale paziente.");

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Data", "Orario", "Medico", "Specializzazione", "Stato"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblStoricoPrenotazioni.setModel(model);
        tblStoricoPrenotazioni.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblStoricoPrenotazioni.setAutoCreateRowSorter(true);
    }

    private void collegaEventi() {
        btnPrenotaVisita.addActionListener(e -> navigatore.apriAreaPrenotaVisita());

        btnStoricoPrenotazioni.addActionListener(e -> caricaStoricoPrenotazioni());

        btnLogout.addActionListener(e -> {
            SessioneUtente.getInstance().logout(); // Distrugge il token in memoria
            navigatore.apriMainPage();             // Torna alla schermata iniziale
        });
    }


    private void caricaStoricoPrenotazioni() {
        Long idPaziente = leggiIdPazienteAutenticato();

        if (idPaziente == null) {
            mostraMessaggio("Impossibile caricare lo storico: paziente non autenticato.");
            return;
        }

        try {
            List<PrenotazioneDto> prenotazioni = prenotazioneController.getPrenotazioniPaziente(idPaziente);
            mostraStoricoPrenotazioni(prenotazioni);

            if (prenotazioni.isEmpty()) {
                lblInfo.setText("Non sono presenti prenotazioni nello storico.");
                mostraMessaggio("Non sono presenti prenotazioni nello storico.");
            } else {
                lblInfo.setText("Storico prenotazioni caricato correttamente.");
            }
        } catch (RuntimeException ex) {
            mostraErrore("Errore durante il caricamento dello storico prenotazioni", ex);
        }
    }

    private void caricaStoricoPrenotazioniSilenzioso() {
        Long idPaziente = leggiIdPazienteAutenticato();

        if (idPaziente == null) {
            return;
        }

        try {
            List<PrenotazioneDto> prenotazioni = prenotazioneController.getPrenotazioniPaziente(idPaziente);
            mostraStoricoPrenotazioni(prenotazioni);
            lblInfo.setText("Storico prenotazioni aggiornato.");
        } catch (RuntimeException ignored) {
            // Aggiornamento automatico non bloccante.
        }
    }

    private void mostraStoricoPrenotazioni(List<PrenotazioneDto> prenotazioni) {
        DefaultTableModel model = (DefaultTableModel) tblStoricoPrenotazioni.getModel();
        model.setRowCount(0);

        List<PrenotazioneDto> prenotazioniOrdinate = new ArrayList<>(prenotazioni);
        prenotazioniOrdinate.sort(
                Comparator.comparing(this::dataPrenotazioneSicura, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(this::oraInizioSicura, Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed()
        );

        for (PrenotazioneDto prenotazione : prenotazioniOrdinate) {
            model.addRow(new Object[]{
                    prenotazione.id(),
                    formattaData(dataPrenotazioneSicura(prenotazione)),
                    formattaOrario(prenotazione),
                    formattaMedico(prenotazione),
                    formattaSpecializzazione(prenotazione),
                    prenotazione.stato()
            });
        }
    }

    private Long leggiIdPazienteAutenticato() {
        Long idDaSessione = SessioneUtente.getInstance().getIdUtente();

        if (idDaSessione != null) {
            return idDaSessione;
        }

        return idPaziente;
    }

    private LocalDate dataPrenotazioneSicura(PrenotazioneDto prenotazione) {
        if (prenotazione == null || prenotazione.fasciaOraria() == null) {
            return null;
        }

        return prenotazione.fasciaOraria().data();
    }

    private LocalTime oraInizioSicura(PrenotazioneDto prenotazione) {
        if (prenotazione == null || prenotazione.fasciaOraria() == null) {
            return null;
        }

        return prenotazione.fasciaOraria().orainizio();
    }

    private String formattaData(LocalDate data) {
        if (data == null) {
            return "-";
        }

        return data.format(DATA_FORMATTER);
    }

    private String formattaOrario(PrenotazioneDto prenotazione) {
        if (prenotazione == null || prenotazione.fasciaOraria() == null) {
            return "-";
        }

        return prenotazione.fasciaOraria().orainizio().format(ORA_FORMATTER) + " - " +
                prenotazione.fasciaOraria().oraFine().format(ORA_FORMATTER);
    }

    private String formattaMedico(PrenotazioneDto prenotazione) {
        if (prenotazione == null || prenotazione.medico() == null) {
            return "-";
        }

        return prenotazione.medico().cognome() + " " + prenotazione.medico().nome();
    }

    private String formattaSpecializzazione(PrenotazioneDto prenotazione) {
        if (prenotazione == null
                || prenotazione.medico() == null
                || prenotazione.medico().specializzazione() == null) {
            return "-";
        }

        return prenotazione.medico().specializzazione().nomeSpecializzazione();
    }

    private void mostraMessaggio(String messaggio) {
        JOptionPane.showMessageDialog(contentPane, messaggio);
    }

    private void mostraErrore(String titolo, RuntimeException ex) {
        String dettaglio = ex.getMessage() != null ? ex.getMessage() : "Errore non specificato";
        JOptionPane.showMessageDialog(contentPane, titolo + ":\n" + dettaglio, "Errore", JOptionPane.ERROR_MESSAGE);
    }
}
