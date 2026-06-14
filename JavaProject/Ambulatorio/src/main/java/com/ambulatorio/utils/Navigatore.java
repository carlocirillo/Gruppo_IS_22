package com.ambulatorio.utils;

import com.ambulatorio.boundary.*;
import com.ambulatorio.controller.AuthController;
import com.ambulatorio.controller.CalendarioController;
import com.ambulatorio.controller.MedicoController;
import com.ambulatorio.controller.PrenotazioneController;

import javax.swing.*;

public class Navigatore {
    private JFrame frame;
    private AuthController authController;
    private CalendarioController calendarioController;
    private MedicoController medicoController;
    private PrenotazioneController prenotazioneController;

    public Navigatore(
            JFrame frame,
            AuthController ac,
            CalendarioController cc,
            MedicoController mc,
            PrenotazioneController pc
            ) {
        this.frame = frame;
        this.authController = ac;
        this.calendarioController = cc;
        this.medicoController = mc;
        this.prenotazioneController = pc;
    }

    public void apriMainPage() {
        MainPage view = new MainPage(this);
        cambiaSchermata(view.contentPane);
    }

    public void apriLogin() {
        LoginView view = new LoginView(this, authController);
        cambiaSchermata(view.contentPane);
    }

    public void apriRegistrazione() {
        RegistrazioneView view = new RegistrazioneView(this, authController);
        cambiaSchermata(view.contentPane);
    }

    public void apriAreaPaziente() {
        //RegistrazioneView view = new RegistrazioneView(this, authController);
        //cambiaSchermata(view.contentPane);
    }

    public void apriAreaMedico() {
        AreaMedicoView view = new AreaMedicoView(prenotazioneController, 1); //TODO: cambiare l'1
        cambiaSchermata(view.contentPane);
    }

    public void apriAreaAmministratore() {
        AreaAmministratoreView view = new AreaAmministratoreView(prenotazioneController);
        cambiaSchermata(view.contentPane);
    }

    private void cambiaSchermata(JPanel nuovoPannello) {
        frame.setContentPane(nuovoPannello);
        frame.revalidate();
        frame.repaint();
    }
}
