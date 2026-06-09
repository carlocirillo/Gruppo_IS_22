package com.ambulatorio.boundary;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainPage {
    private JButton accediButton;
    private JButton registratiButton;
    public JPanel contentPane;

    public MainPage() {
        accediButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                apriSchermataLogin();
            }
        });
        registratiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                apriSchermataRegistrazione();
            }
        });
    }

    private void apriSchermataLogin() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(contentPane);
        LoginView loginView = new LoginView();
        frame.setContentPane(loginView.contentPane);

        frame.revalidate();
        frame.repaint();
    }

    private void apriSchermataRegistrazione() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(contentPane);
        RegistrazioneView registrazioneView = new RegistrazioneView();
        frame.setContentPane(registrazioneView.contentPane);

        frame.revalidate();
        frame.repaint();
    }
}
