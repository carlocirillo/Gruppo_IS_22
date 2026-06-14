package com.ambulatorio.boundary;

import com.ambulatorio.controller.AuthController;
import com.ambulatorio.dto.request.CredenzialiAccessoDto;
import com.ambulatorio.exceptions.CredenzialiNonValideException;
import com.ambulatorio.utils.JwtUtils;
import com.ambulatorio.utils.Navigatore;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginView {
    public JPanel contentPane;
    private AuthController authController;

    private JTextField textField1;
    private JPasswordField passwordField1;
    private JButton annullaButton;
    private JButton accediButton;

    public LoginView(Navigatore navigatore, AuthController authController) {
        this.authController = authController;

        annullaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigatore.apriMainPage();
            }
        });
        accediButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email =  textField1.getText();
                String password = new String(passwordField1.getPassword());

                if (email.trim().isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(contentPane,
                            "Per favore, compila tutti i campi.",
                            "Errore di inserimento",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                CredenzialiAccessoDto dati =  new CredenzialiAccessoDto(email, password);

                try {
                    String token =  authController.login(dati);
                    JOptionPane.showMessageDialog(contentPane, "Login effettuato con successo!");
                    switch (JwtUtils.estraiRuolo(token).toUpperCase()) {
                        case "PAZIENTE":
                            navigatore.apriAreaPaziente();
                            break;
                        case "MEDICO":
                            navigatore.apriAreaMedico();
                            break;
                        case "AMMINISTRATORE":
                            navigatore.apriAreaAmministratore();
                            break;
                        default:
                            throw new CredenzialiNonValideException("Token JWT corrotto");
                    }
                } catch (CredenzialiNonValideException credException) {
                    JOptionPane.showMessageDialog(contentPane,credException.getMessage(), "Errore di accesso", JOptionPane.ERROR_MESSAGE);
                }

            }
        });
    }


}
