package com.ambulatorio.boundary;

import com.ambulatorio.controller.AuthController;
import com.ambulatorio.dto.request.NuovoPazienteDto;
import com.ambulatorio.exceptions.CredenzialiNonValideException;
import com.ambulatorio.exceptions.UtenteGiaRegistratoException;
import com.ambulatorio.utils.Navigatore;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

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
        registratiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NuovoPazienteDto nuovoPazienteDto = new NuovoPazienteDto(
                        textField1.getText(),
                        textField2.getText(),
                        textField3.getText(),
                        Arrays.toString(passwordField1.getPassword()),
                        textField5.getText(),
                        textField4.getText()
                );
                try {
                    authController.registrazionePaziente(nuovoPazienteDto);
                    navigatore.apriMainPage();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(contentPane,ex.getMessage(), "Errore di registrazione", JOptionPane.ERROR_MESSAGE);
                }
                }
            }
        );
    }
}
