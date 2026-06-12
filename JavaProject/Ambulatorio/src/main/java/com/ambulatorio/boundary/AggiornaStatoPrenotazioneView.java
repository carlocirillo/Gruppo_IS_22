package com.ambulatorio.boundary;

import com.ambulatorio.controller.PrenotazioneController;
import com.ambulatorio.entity.Prenotazione;
import com.ambulatorio.entity.enums.StatoPrenotazione;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AggiornaStatoPrenotazioneView extends JFrame {
    private JTable tblPrenotazioni;
    private PrenotazioneController controller;
    private Long idMedicoLoggato;

    public AggiornaStatoPrenotazioneView(PrenotazioneController controller, Long idMedico) {
        this.controller = controller;
        this.idMedicoLoggato = idMedico;

        setTitle("Gestione Stato Prenotazioni");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Titolo informativo
        JLabel lblInfo = new JLabel("Seleziona una prenotazione per modificarne lo stato");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 12));
        mainPanel.add(lblInfo, BorderLayout.NORTH);

        // Tabella
        tblPrenotazioni = new JTable();
        updateTable();
        mainPanel.add(new JScrollPane(tblPrenotazioni), BorderLayout.CENTER);

        // Listener per il click sulla riga
        tblPrenotazioni.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tblPrenotazioni.getSelectedRow();
                if (selectedRow != -1) {
                    mostraPopupSelezioneStato(selectedRow);
                }
            }
        });

        add(mainPanel);
    }

    private void mostraPopupSelezioneStato(int row) {
        Long idPrenotazione = (Long) tblPrenotazioni.getValueAt(row, 0);
        String paziente = (String) tblPrenotazioni.getValueAt(row, 1);
        StatoPrenotazione statoAttuale = (StatoPrenotazione) tblPrenotazioni.getValueAt(row, 4);

        // Creazione del pannello per il popup
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Modifica stato per: " + paziente));
        panel.add(new JLabel("Stato attuale: " + statoAttuale));
        panel.add(new JLabel("Seleziona nuovo stato:"));

        JComboBox<StatoPrenotazione> comboStati = new JComboBox<>(StatoPrenotazione.values());
        comboStati.setSelectedItem(statoAttuale);
        panel.add(comboStati);

        Object[] options = {"Conferma", "Annulla"};
        int result = JOptionPane.showOptionDialog(this, panel, 
                "Aggiorna Stato Prenotazione",
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        if (result == JOptionPane.YES_OPTION) {
            StatoPrenotazione nuovoStato = (StatoPrenotazione) comboStati.getSelectedItem();
            boolean successo = controller.setStatoPrenotazione(idPrenotazione, nuovoStato);
            
            if (successo) {
                JOptionPane.showMessageDialog(this, "stato modificato");
                updateTable();
            } else {
                JOptionPane.showMessageDialog(this, "Errore di salvataggio", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        // Deseleziona la riga dopo l'operazione
        tblPrenotazioni.clearSelection();
    }

    private void updateTable() {
        String[] columns = {"ID", "Paziente", "Data", "Ora", "Stato Attuale"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<Prenotazione> prenotazioni = controller.getPrenotazioniMedico(idMedicoLoggato);
        for (Prenotazione p : prenotazioni) {
            model.addRow(new Object[]{
                    p.getId(),
                    p.getPaziente().getNome() + " " + p.getPaziente().getCognome(),
                    p.getFasciaOraria().getData(),
                    p.getFasciaOraria().getOraInizio(),
                    p.getStato()
            });
        }
        tblPrenotazioni.setModel(model);
    }

    private void cambiaStato(StatoPrenotazione nuovoStato) {
        int selectedRow = tblPrenotazioni.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleziona una prenotazione dalla tabella.");
            return;
        }

        // Popup di conferma con tasti personalizzati
        Object[] options = {"Conferma", "Annulla"};
        int scelta = JOptionPane.showOptionDialog(this, 
            "Sei sicuro di voler cambiare lo stato in: " + nuovoStato + "?", 
            "Conferma Cambio Stato", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);

        if (scelta == JOptionPane.YES_OPTION) {
            Long idPrenotazione = (Long) tblPrenotazioni.getValueAt(selectedRow, 0);
            boolean successo = controller.setStatoPrenotazione(idPrenotazione, nuovoStato);
            
            if (successo) {
                JOptionPane.showMessageDialog(this, "stato modificato");
                updateTable();
            } else {
                JOptionPane.showMessageDialog(this, "Errore di salvataggio", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
        // Se la scelta è NO_OPTION o il popup viene chiuso, non succede nulla (annullato)
    }
}
