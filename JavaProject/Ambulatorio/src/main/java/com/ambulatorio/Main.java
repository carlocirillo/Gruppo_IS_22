package com.ambulatorio;

import com.ambulatorio.boundary.AreaAmministratoreView;
import com.ambulatorio.boundary.MainPage;
import com.ambulatorio.database.GestorePersistenza;
import com.ambulatorio.database.JpaUtil;

import javax.swing.*;

public class Main {
    public static void main() {
        JFrame frame = new JFrame();
        frame.setTitle("Ambulatorio");
        frame.setContentPane(new AreaAmministratoreView().contentPane);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        frame.setSize(900, 900);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        GestorePersistenza gestore = new GestorePersistenza();

        try {
            InizializzatoreDatabase inizializzatore = new InizializzatoreDatabase(gestore);
            inizializzatore.popolaDatiDiTest();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Chiusura delle connessioni...");
            JpaUtil.getInstance().chiudi()  ;
        }
    }
}
