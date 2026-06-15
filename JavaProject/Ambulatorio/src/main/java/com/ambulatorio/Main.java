package com.ambulatorio;

import com.ambulatorio.boundary.AreaAmministratoreView;
import com.ambulatorio.boundary.DashboardStatisticheView;
import com.ambulatorio.boundary.MainPage;
import com.ambulatorio.controller.AuthController;
import com.ambulatorio.controller.CalendarioController;
import com.ambulatorio.controller.MedicoController;
import com.ambulatorio.controller.PrenotazioneController;
import com.ambulatorio.database.GestorePersistenza;
import com.ambulatorio.utils.InizializzatoreDatabase;
import com.ambulatorio.utils.Navigatore;

import javax.swing.*;

public class Main {
    public static void main() {

        // 1. --- INIZIALIZZAZIONE DATABASE ---
        GestorePersistenza gestore = new GestorePersistenza();

        try {
            InizializzatoreDatabase inizializzatore = new InizializzatoreDatabase(gestore);
            inizializzatore.popolaDatiDiTest();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. --- CREAZIONE CONTROLLER ---
        AuthController authController = new AuthController(gestore);
        MedicoController medicoController = new MedicoController(gestore);
        PrenotazioneController prenotazioneController = new PrenotazioneController(gestore);
        CalendarioController calendarioController = new CalendarioController(gestore);

        // 3. --- INIZIALIZZAZIONE NAVIGATORE E INTERFACCIA GRAFICA ---
        JFrame frame = new JFrame();
        Navigatore navigatore = new Navigatore(frame, authController, calendarioController, medicoController, prenotazioneController);

        frame.setTitle("Ambulatorio");
        frame.setContentPane(new AreaAmministratoreView(navigatore).contentPane);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        frame.setSize(900, 900);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
