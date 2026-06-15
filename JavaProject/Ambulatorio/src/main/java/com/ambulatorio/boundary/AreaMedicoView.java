package com.ambulatorio.boundary;

import com.ambulatorio.controller.PrenotazioneController;
import com.ambulatorio.dto.response.PrenotazioneDto;
import com.ambulatorio.entity.enums.StatoPrenotazione;
import com.ambulatorio.exceptions.PersistenzaException;
import com.ambulatorio.utils.Navigatore;
import com.ambulatorio.utils.SessioneUtente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AreaMedicoView {
    public JPanel contentPane;
    private JTable tblAgenda;
    private JScrollPane scrollPane;
    private JLabel lblTitolo;
    private JButton btnGiornoPrecedente;
    private JButton btnGiornoSuccessivo;
    private JButton btnIndietro;
    private JLabel lblDataSelezionata;

    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Navigatore navigatore;
    private final PrenotazioneController prenotazioneController;
    private List<PrenotazioneDto> prenotazioniVisualizzate;
    private LocalDate dataVisualizzata;
    private final long idMedicoCorrente;

    public AreaMedicoView(Navigatore navigatore, PrenotazioneController prenotazioneController) {
        this.navigatore = navigatore;
        this.prenotazioneController = prenotazioneController;
        this.idMedicoCorrente = SessioneUtente.getInstance().getIdUtente();
        this.prenotazioniVisualizzate = new ArrayList<>();
        this.dataVisualizzata = LocalDate.now();

        inizializzaTabella();
        inizializzaNavigazione();

        // Carica i dati immediatamente all'apertura della schermata
        aggiornaInterfaccia();

        // Listener di selezione sulla tabella
        tblAgenda.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                gestisciSelezione();
            }
        });
    }

    private void inizializzaTabella() {
        String[] columns = {"Ora", "Paziente", "Stato"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblAgenda.setModel(model);
        tblAgenda.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void inizializzaNavigazione() {
        btnIndietro.addActionListener(e -> navigatore.apriMainPage());

        btnGiornoPrecedente.addActionListener(e -> cambiaDataVisualizzata(dataVisualizzata.minusDays(1)));
        btnGiornoSuccessivo.addActionListener(e -> cambiaDataVisualizzata(dataVisualizzata.plusDays(1)));
    }

    private void cambiaDataVisualizzata(LocalDate nuovaData) {
        this.dataVisualizzata = nuovaData;
        aggiornaInterfaccia();
    }

    private void aggiornaInterfaccia() {
        DateTimeFormatter ORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

        // Aggiorna la label della data
        lblDataSelezionata.setText(dataVisualizzata.format(DATA_FORMATTER));

        // Recupero prenotazioni dal Controller
        System.out.println("idMedicoCorrente = " + idMedicoCorrente);
        List<PrenotazioneDto> tutteLePrenotazioni = prenotazioneController.getPrenotazioniMedico(idMedicoCorrente);

        // Filtraggio per la data correntemente visualizzata e ordinamento per orario
        prenotazioniVisualizzate = tutteLePrenotazioni.stream()
                .filter(p -> p.fasciaOraria().data().equals(dataVisualizzata))
                .sorted((p1, p2) -> p1.fasciaOraria().orainizio().compareTo(p2.fasciaOraria().orainizio()))
                .collect(Collectors.toList());

        // Popolamento tabella
        DefaultTableModel model = (DefaultTableModel) tblAgenda.getModel();
        model.setRowCount(0);

        for (PrenotazioneDto p : prenotazioniVisualizzate) {
            Object[] row = {
                    p.fasciaOraria().orainizio().format(ORA_FORMATTER) + " - " + p.fasciaOraria().oraFine().format(ORA_FORMATTER),
                    p.paziente().nome() + " " + p.paziente().cognome(),
                    p.stato()
            };
            model.addRow(row);
        }
    }

    private void gestisciSelezione() {
        int selectedRow = tblAgenda.getSelectedRow();

        // Se non c'è nessuna riga selezionata (es. dopo aver pulito la selezione), esci
        if (selectedRow < 0 || selectedRow >= prenotazioniVisualizzate.size()) {
            return;
        }

        PrenotazioneDto p = prenotazioniVisualizzate.get(selectedRow);

        // BLOCCO STATI FINALI: Se la visita è già stata gestita, ignoriamo il click
        if (p.stato() == StatoPrenotazione.EFFETTUATA || p.stato() == StatoPrenotazione.NON_PRESENTATO) {
            tblAgenda.clearSelection();
            return;
        }

        LocalDateTime oraInizioVisita = LocalDateTime.of(p.fasciaOraria().data(), p.fasciaOraria().orainizio());

        // Se lo stato è PRENOTATA e la fascia oraria è già passata, apri il pop-up modale
        if (oraInizioVisita.isBefore(LocalDateTime.now())) {
            mostraPopUpAggiornamento(p);
        } else {
            // Rimuove la selezione se l'orario non è ancora passato per evitare blocchi
            tblAgenda.clearSelection();
        }
    }

    private void mostraPopUpAggiornamento(PrenotazioneDto p) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(contentPane), "Aggiorna Stato Visita", true);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel panelCentrale = new JPanel(new FlowLayout());
        panelCentrale.add(new JLabel("Nuovo stato:"));

        JComboBox<StatoPrenotazione> comboStato = new JComboBox<>();
        comboStato.addItem(StatoPrenotazione.EFFETTUATA);
        comboStato.addItem(StatoPrenotazione.NON_PRESENTATO);
        panelCentrale.add(comboStato);

        JPanel panelBottoni = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnConferma = new JButton("Conferma");
        JButton btnAnnulla = new JButton("Annulla");

        btnConferma.addActionListener(e -> {
            StatoPrenotazione nuovoStato = (StatoPrenotazione) comboStato.getSelectedItem();

            try {
                prenotazioneController.aggiornaStatoPrenotazione(p.id(), nuovoStato);

                JOptionPane.showMessageDialog(dialog, "Stato aggiornato con successo!", "Successo", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                tblAgenda.clearSelection(); // Rimuove la selezione dopo la modifica
                aggiornaInterfaccia();
            } catch (PersistenzaException ex) {
                // Messaggio specifico richiesto dal Sequence Diagram per errore di persistenza
                JOptionPane.showMessageDialog(dialog, "Errore di salvataggio, riprovare", "Errore", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                // Altri errori generici
                JOptionPane.showMessageDialog(dialog, "Errore di salvataggio, riprovare", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnAnnulla.addActionListener(e -> {
            dialog.dispose();
            tblAgenda.clearSelection(); // Rimuove la selezione se si annulla
        });

        panelBottoni.add(btnConferma);
        panelBottoni.add(btnAnnulla);

        dialog.add(panelCentrale, BorderLayout.CENTER);
        dialog.add(panelBottoni, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(contentPane);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }
}