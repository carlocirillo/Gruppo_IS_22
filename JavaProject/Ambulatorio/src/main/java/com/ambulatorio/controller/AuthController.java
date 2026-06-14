package com.ambulatorio.controller;

import com.ambulatorio.database.GestorePersistenza;
import com.ambulatorio.dto.request.CredenzialiAccessoDto;
import com.ambulatorio.dto.request.NuovoPazienteDto;
import com.ambulatorio.entity.Paziente;
import com.ambulatorio.entity.Utente;
import com.ambulatorio.entity.enums.Ruolo;
import com.ambulatorio.exceptions.CredenzialiNonValideException;
import com.ambulatorio.exceptions.UtenteGiaRegistratoException;
import com.ambulatorio.utils.JwtUtils;
import com.ambulatorio.utils.PasswordUtils;

import java.util.List;

public class AuthController {
    private final GestorePersistenza gestorePersistenza;

    public AuthController(GestorePersistenza gestore) {
        this.gestorePersistenza = gestore;
    }


    public void registrazionePaziente(NuovoPazienteDto datiPaziente) {
        if (datiPaziente == null || datiPaziente.email() == null || datiPaziente.password() == null) {
            throw new IllegalArgumentException("Dati paziente incompleti");
        }

        List<Paziente> esistenti = gestorePersistenza.cercaPerCampo(Paziente.class, "email",  datiPaziente.email());

        if (!esistenti.isEmpty()) {
            throw new UtenteGiaRegistratoException("Esiste già un account con questa email.");
        }

        String passwordHash = PasswordUtils.generaPasswordHash(datiPaziente.password());

        Paziente nuovoPaziente = new Paziente();
        nuovoPaziente.setNome(datiPaziente.nome());
        nuovoPaziente.setCognome(datiPaziente.cognome());
        nuovoPaziente.setEmail(datiPaziente.email());
        nuovoPaziente.setPasswordHash(passwordHash);
        nuovoPaziente.setRuolo(Ruolo.PAZIENTE);

        if (!gestorePersistenza.salva(nuovoPaziente)) {
            throw new RuntimeException("Errore interno durante il salvataggio del paziente.");
        }
    }

    public String login(CredenzialiAccessoDto dati) {

        if (dati == null || dati.email() == null || dati.password() == null) {
            throw new IllegalArgumentException("Email e password sono obbligatorie");
        }

        List<Utente> utenti = gestorePersistenza.cercaPerCampo(Utente.class,"email", dati.email());

        if (utenti.isEmpty()) {
            throw new CredenzialiNonValideException("Email o password non valida.");
        }

        String passwordHash = utenti.getFirst().getPasswordHash();
        if (!PasswordUtils.verifica(dati.password(), passwordHash)) {
            throw new CredenzialiNonValideException("Email o password non valida.");
        }


        Utente utenteAutenticato = utenti.getFirst();

        // Deleghiamo la creazione del JWT a una classe di utilità apposita
        String token = JwtUtils.generaToken(
                utenteAutenticato.getId().toString(),
                utenteAutenticato.getEmail(),
                utenteAutenticato.getRuolo().toString()
        );

        return token;
    }
}
