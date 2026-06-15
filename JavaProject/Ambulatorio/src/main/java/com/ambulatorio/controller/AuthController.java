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
import java.util.regex.Pattern;

public class AuthController {
    private final GestorePersistenza gestorePersistenza;
    private static final String EMAIL_REGEX = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public AuthController(GestorePersistenza gestore) {
        this.gestorePersistenza = gestore;
    }


    public void registrazionePaziente(NuovoPazienteDto datiPaziente) {
        if (datiPaziente == null || datiPaziente.email() == null || datiPaziente.password() == null) {
            throw new IllegalArgumentException("Dati paziente incompleti");
        }

        String email = datiPaziente.email().trim().toLowerCase();

        List<Paziente> esistenti = gestorePersistenza.cercaPerCampo(Paziente.class, "email",  email);

        if (!esistenti.isEmpty()) {
            throw new UtenteGiaRegistratoException("Esiste già un account con questa email.");
        }

        System.out.println("Password ricevuta: [" + datiPaziente.password() + "]");

        String passwordHash = PasswordUtils.generaPasswordHash(datiPaziente.password());

        Paziente nuovoPaziente = new Paziente();
        nuovoPaziente.setNome(datiPaziente.nome());
        nuovoPaziente.setCognome(datiPaziente.cognome());
        nuovoPaziente.setEmail(email);
        nuovoPaziente.setPasswordHash(passwordHash);
        nuovoPaziente.setRuolo(Ruolo.PAZIENTE);
        nuovoPaziente.setCodiceFiscale(datiPaziente.codiceFiscale());
        nuovoPaziente.setNumeroCellulare(datiPaziente.numeroCellulare());

        if (!gestorePersistenza.salva(nuovoPaziente)) {
            throw new RuntimeException("Errore interno durante il salvataggio del paziente.");
        }
    }

    public String login(CredenzialiAccessoDto dati) {

        if (dati == null || dati.email() == null || dati.password() == null) {
            throw new IllegalArgumentException("Email e password sono obbligatorie");
        }

        if (!EMAIL_PATTERN.matcher(dati.email()).matches()) {
            throw new IllegalArgumentException("Formato email non valido");
        }

        List<Utente> utenti = gestorePersistenza.cercaPerCampo(Utente.class,"email", dati.email());
        System.out.println("Trovati: " + utenti.size());
        if (!utenti.isEmpty()) {
            System.out.println("Email: " + utenti.getFirst().getEmail());
            System.out.println("Hash: " + utenti.getFirst().getPasswordHash());
            System.out.println("Ruolo: " + utenti.getFirst().getRuolo());
            System.out.println("Classe: " + utenti.getFirst().getClass().getName());
        }

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
