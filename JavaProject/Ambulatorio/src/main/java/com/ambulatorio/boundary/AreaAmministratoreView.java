package com.ambulatorio.boundary;

import com.ambulatorio.utils.Navigatore;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AreaAmministratoreView {
    public JPanel contentPane;
    private JButton btnCreaProfilo;
    private JButton btnAmbulatorio;
    private JLabel lblTitolo;
    private JButton btnIndietro;


    public AreaAmministratoreView(Navigatore navigatore) {

        btnIndietro.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                navigatore.apriMainPage();
            }


        });
        btnAmbulatorio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                navigatore.apriDashboardStatistiche();
            }
        });
    }
}
