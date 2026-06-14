package com.ambulatorio.boundary;

import com.ambulatorio.controller.PrenotazioneController;
import com.ambulatorio.dto.response.PrenotazioneDto;
import com.ambulatorio.entity.enums.StatoPrenotazione;
import com.ambulatorio.utils.Navigatore;

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
    private JButton btnIndietro;
    private JLabel lblDataSelezionata;

    private final Navigatore navigatore;
    private final PrenotazioneController prenotazioneController;
    private List<PrenotazioneDto> prenotazioniVisualizzate;
    private LocalDate dataVisualizzata;
    private long idMedicoCorrente;

    public AreaMedicoView(Navigatore navigatore, PrenotazioneController prenotazioneController, long idMedico) {
        this.navigatore = navigatore;
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
        btnIndietro.addActionListener(e -> navigatore.apriMainPage());

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
        List<PrenotazioneDto> tutteLePrenotazioni = prenotazioneController.getPrenotazioniMedico(idMedicoCorrente);

        // Filtraggio per la data correntemente visualizzata
        prenotazioniVisualizzate = tutteLePrenotazioni.stream()
                .filter(p -> p.fasciaOraria().data().equals(dataVisualizzata))
                .sorted((p1, p2) -> p1.fasciaOraria().orainizio().compareTo(p2.fasciaOraria().orainizio()))
                .collect(Collectors.toList());

        // Popolamento tabella
        DefaultTableModel model = (DefaultTableModel) tblAgenda.getModel();
        model.setRowCount(0);

        for (PrenotazioneDto p : prenotazioniVisualizzate) {
            Object[] row = {
                    p.fasciaOraria().orainizio() + " - " + p.fasciaOraria().oraFine(),
                    p.paziente().nome() + " " + p.paziente().cognome(),
                    p.stato()
            };
            model.addRow(row);
        }
    }

    private void gestisciSelezione() {
        int selectedRow = tblAgenda.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < prenotazioniVisualizzate.size()) {
            PrenotazioneDto p = prenotazioniVisualizzate.get(selectedRow);
            LocalDateTime oraInizioVisita = LocalDateTime.of(p.fasciaOraria().data(), p.fasciaOraria().orainizio());

            // Se la fascia oraria è già passata, apri il pop-up modale
            if (oraInizioVisita.isBefore(LocalDateTime.now())) {
                mostraPopUpAggiornamento(p);
            }
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
                prenotazioneController.aggiornaStatoPrenotazione(idMedicoCorrente, nuovoStato);
                JOptionPane.showMessageDialog(dialog, "Stato aggiornato!", "Successo", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                aggiornaInterfaccia();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Errore durante l'aggiornamento dei dati", "Errore", JOptionPane.ERROR_MESSAGE);
                // Il sistema si "blocca" nel senso che non chiude il dialog di errore e non aggiorna la tabella
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
