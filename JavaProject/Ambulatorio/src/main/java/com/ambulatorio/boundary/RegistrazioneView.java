package com.ambulatorio.boundary;

import com.ambulatorio.controller.AuthController;
import com.ambulatorio.utils.Navigatore;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegistrazioneView {
    public JPanel contentPane;
    private AuthController authController;

    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JPasswordField passwordField1;
    private JTextField textField4;
    private JTextField textField5;
    private JButton registratiButton;
    private JButton annullaButton;

    public RegistrazioneView(Navigatore navigatore, AuthController authController) {
        annullaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigatore.apriMainPage();
            }
        });
    }
}
