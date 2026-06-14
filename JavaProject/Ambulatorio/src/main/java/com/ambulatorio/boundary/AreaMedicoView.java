package com.ambulatorio.boundary;

import com.ambulatorio.controller.PrenotazioneController;
import com.ambulatorio.entity.Prenotazione;
import com.ambulatorio.entity.enums.StatoPrenotazione;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class AreaMedicoView {
    public JPanel contentPane;
    private JTable tblAgenda;
    private JScrollPane scrollPane;
    private JLabel lblTitolo;
    private JButton btnGiornoPrecedente;
    private JButton btnGiornoSuccessivo;
    private JLabel lblDataSelezionata;

    private final PrenotazioneController prenotazioneController;
    private List<Prenotazione> prenotazioniVisualizzate;
    private LocalDate dataVisualizzata;
    private long idMedicoCorrente;

    public AreaMedicoView(PrenotazioneController prenotazioneController, long idMedico) {
        this.idMedicoCorrente = idMedico;
        this.prenotazioneController = prenotazioneController;
        this.prenotazioniVisualizzate = new ArrayList<>();
        this.dataVisualizzata = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lblDataSelezionata.setText(dataVisualizzata.format(formatter));

        initTable();
        initNavigation();

        // Listener di selezione sulla tabella
        tblAgenda.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                gestisciSelezione();
            }
        });
    }

    private void initTable() {
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

    private void initNavigation() {
        btnGiornoPrecedente.addActionListener(e -> {
            LocalDate nuovaData = dataVisualizzata.minusDays(1);
            Date date = Date.from(nuovaData.atStartOfDay(ZoneId.systemDefault()).toInstant());
            mostraAgenda(idMedicoCorrente, date);
        });

        btnGiornoSuccessivo.addActionListener(e -> {
            LocalDate nuovaData = dataVisualizzata.plusDays(1);
            Date date = Date.from(nuovaData.atStartOfDay(ZoneId.systemDefault()).toInstant());
            mostraAgenda(idMedicoCorrente, date);
        });
    }

    public void mostraAgenda(long idMedico, Date data) {
        this.idMedicoCorrente = idMedico;
        this.dataVisualizzata = data.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        aggiornaInterfaccia();
    }

    private void aggiornaInterfaccia() {
        // Aggiorna la label della data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lblDataSelezionata.setText(dataVisualizzata.format(formatter));

        // Recupero prenotazioni reali dal Controller
        List<Prenotazione> tutteLePrenotazioni = prenotazioneController.getPrenotazioniMedico(idMedicoCorrente);

        // Filtraggio per la data correntemente visualizzata
        prenotazioniVisualizzate = tutteLePrenotazioni.stream()
                .filter(p -> p.getFasciaOraria().getData().equals(dataVisualizzata))
                .sorted((p1, p2) -> p1.getFasciaOraria().getOraInizio().compareTo(p2.getFasciaOraria().getOraInizio()))
                .collect(Collectors.toList());

        // Popolamento tabella
        DefaultTableModel model = (DefaultTableModel) tblAgenda.getModel();
        model.setRowCount(0);

        for (Prenotazione p : prenotazioniVisualizzate) {
            Object[] row = {
                    p.getFasciaOraria().getOraInizio().toString(),
                    p.getPaziente().getNome() + " " + p.getPaziente().getCognome(),
                    p.getStato()
            };
            model.addRow(row);
        }
    }

    private void gestisciSelezione() {
        int selectedRow = tblAgenda.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < prenotazioniVisualizzate.size()) {
            Prenotazione p = prenotazioniVisualizzate.get(selectedRow);
            LocalDateTime oraInizioVisita = LocalDateTime.of(p.getFasciaOraria().getData(), p.getFasciaOraria().getOraInizio());

            // Se la fascia oraria è già passata, apri il pop-up modale
            if (oraInizioVisita.isBefore(LocalDateTime.now())) {
                mostraPopUpAggiornamento(p);
            }
        }
    }

    private void mostraPopUpAggiornamento(Prenotazione p) {
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
            
            boolean successo = prenotazioneController.aggiornaStatoPrenotazione(p.getId(), nuovoStato, idMedicoCorrente);
            
            if (successo) {
                JOptionPane.showMessageDialog(dialog, "Stato aggiornato!", "Successo", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                aggiornaInterfaccia();
            } else {
                JOptionPane.showMessageDialog(dialog, "Errore aggiornamento.", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnAnnulla.addActionListener(e -> dialog.dispose());

        panelBottoni.add(btnConferma);
        panelBottoni.add(btnAnnulla);

        dialog.add(panelCentrale, BorderLayout.CENTER);
        dialog.add(panelBottoni, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(contentPane);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    public JPanel getContentPane() {
        return contentPane;
    }
}
