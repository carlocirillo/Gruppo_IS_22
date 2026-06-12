package com.ambulatorio.boundary;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AreaAmministratoreView {

    private JPanel GUIPanel;
    private JLabel lblTitolo;
    private JLabel lblFiltri;
    private JPanel filtriPanel;
    private JTextField dataInizioTxt;
    private JTextField dataFineTxt;
    private JButton btnElabora;
    private JLabel lblDataInizio;
    private JLabel lblDataFine;
    private JPanel elaborazionePanel;
    private JPanel framesPanel;
    private JLabel metricheTxt;
    private JPanel framePrenotazioniPanel;
    private JLabel lblPrenotazioni;
    private JLabel lblNumPrenotazioni;
    private JLabel lblAnnullamenti;
    private JLabel lblNumAnnullamenti;
    private JProgressBar progressBarSaturazione;
    private JLabel lblTassoSaturazione;
    private JPanel frameAnnullamentiPanel;
    private JPanel frameSaturazionePanel;
    private JLabel tabellaTxt;
    private JTable tblMetriche;
    private JScrollPane scrollPane;

    private void createUIComponents() {

        String[] attributi = {"Specializzazione Medica", "Numero Prenotazioni"};

        DefaultTableModel model = new DefaultTableModel(attributi, 0);
        tblMetriche.setModel(model);
    }

    public AreaAmministratoreView() {

        // Il panel associato al calcolo delle metriche deve essere inizialmente non visibile
        elaborazionePanel.setVisible(false);

        btnElabora.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                elaboraDati();
            }
        });
    }

    private void elaboraDati() {

        String testoInizio = dataInizioTxt.getText().trim();
        String testoFine = dataFineTxt.getText().trim();
        LocalDate dataCreazioneAmbulatorio = LocalDate.of(2015, 10, 10);

        String erroreInizio = isDataValida(testoInizio);
        if(erroreInizio != null){

            // In caso di errore mostra un messaggio personalizzato e blocca l'esecuzione
            JOptionPane.showMessageDialog(GUIPanel, "Errore nella data d'inizio: " + erroreInizio,
                    "ERRORE", JOptionPane.ERROR_MESSAGE);

            return;
        }

        String erroreFine = isDataValida(testoFine);
        if(erroreFine != null){

            JOptionPane.showMessageDialog(GUIPanel, "Errore nella data di fine: " + erroreFine,
                    "ERRORE", JOptionPane.ERROR_MESSAGE);

            return;
        }

        // Date valide ma occorre effettuare anche confronti cronologicoi
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate dataInizio = LocalDate.parse(testoInizio, formatter);
        LocalDate dataFine = LocalDate.parse(testoFine, formatter);

        // Verifica che dataInizio sia antecedente a dataFine
        if(dataInizio.isAfter(dataFine)){

            JOptionPane.showMessageDialog(GUIPanel, "La data d’inizio deve essere antecedente a quella di fine.",
                    "ERRORE", JOptionPane.ERROR_MESSAGE);

            return;
        }

        // Verifica che dataFine non sia antecedente a dataCreazioneAmbulatorio
        if(dataFine.isBefore(dataCreazioneAmbulatorio)){

            JOptionPane.showMessageDialog(GUIPanel,
                    "Impossibile consultare le statistiche dell’ambulatorio antecedenti alla sua creazione.",
                    "ERRORE", JOptionPane.ERROR_MESSAGE);

            return;
        }

        // Verifica che dataInizio non sia antecedente a dataCreazioneambulatorio, in quel caso parte dal limite storico
        if(dataInizio.isBefore(dataCreazioneAmbulatorio)){

            dataInizio = dataCreazioneAmbulatorio;
            dataInizioTxt.setText("10/10/2015");
        }

        // Panel dei risultati visibile
        elaborazionePanel.setVisible(true);

        // Aggiorna la grafica del panel principale
        GUIPanel.revalidate();
        GUIPanel.repaint();


    }

    private String isDataValida(String testoData) {

        // Verifica che la stringa non sia vuota
        if(testoData == null || testoData.trim().isEmpty()) return "Il campo della data non può essere vuoto.";

        // Verifica che la data rispetti il formato preposto dal sistema (GG/MM/AAAA)
        if(!testoData.matches("\\d{2}/\\d{2}/\\d{4}")) {

            return "Formato della data non valido o presenza di caratteri non consentiti (usa GG/MM/AAAA).";
        }

        /*
        Divide la stringa in modo da poter analizzare i numeri (parsing necessario al fine di effettuare contolli
        su interi)
        */
        String[] parti = testoData.split("/");
        int giorno = Integer.parseInt(parti[0]);
        int mese = Integer.parseInt(parti[1]);
        int anno = Integer.parseInt(parti[2]);

        // Verifica che la data esista
        if(giorno < 1 || giorno > 31 || mese < 1 || mese > 12) return "La data inserita non esiste.";

        // Controllo sui mesi di 30 giorni
        if((mese == 11 || mese == 4 || mese == 6 || mese == 9) && giorno > 30) return "Il mese inserito ha 30 giorni.";

        // Controllo specifico per Febbraio
        if(mese == 2){

            boolean isBisestile = false;
            if((anno % 4 == 0 && anno % 100 != 0) || (anno % 400 == 0)) isBisestile = true;

            if(isBisestile && giorno > 29) return "Data non valida, l'anno inserito è bisestile.";
            if(!isBisestile && giorno > 28) return "La data inserita non esiste.";
        }

        return null;
    }
}
