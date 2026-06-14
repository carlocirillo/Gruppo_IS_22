package com.ambulatorio.boundary;

import com.ambulatorio.utils.Navigatore;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainPage {
    private JButton accediButton;
    private JButton registratiButton;
    public JPanel contentPane;

    public MainPage(Navigatore navigatore) {
        accediButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigatore.apriLogin();
            }
        });
        registratiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigatore.apriRegistrazione();
            }
        });
    }
}
