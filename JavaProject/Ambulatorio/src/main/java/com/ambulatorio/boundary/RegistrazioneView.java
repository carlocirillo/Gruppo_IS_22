package com.ambulatorio.boundary;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegistrazioneView {
    public JPanel contentPane;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JPasswordField passwordField1;
    private JTextField textField4;
    private JTextField textField5;
    private JButton registratiButton;
    private JButton annullaButton;

    public RegistrazioneView() {
        annullaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                apriMainPage();
            }
        });
    }

    private void apriMainPage() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(contentPane);
        MainPage mainPage = new MainPage();
        frame.setContentPane(mainPage.contentPane);

        frame.revalidate();
        frame.repaint();
    }
}
