package com.ambulatorio.utils;

import com.ambulatorio.database.GestorePersistenza;
import com.ambulatorio.entity.*;
import com.ambulatorio.entity.enums.Ruolo;
import com.ambulatorio.entity.enums.StatoFascia;
import com.ambulatorio.entity.enums.StatoPrenotazione;

import java.time.LocalDate;
import java.time.LocalTime;

public class InizializzatoreDatabase {

    private final GestorePersistenza gestore;

    public InizializzatoreDatabase(GestorePersistenza gestore) {
        this.gestore = gestore;
    }

    public void popolaDatiDiTest() {

        // 1. Verifichiamo se i dati esistono già per evitare duplicati a ogni avvio
        long numeroMedici = gestore.conta(Medico.class);

        if (numeroMedici > 0) {
            System.out.println("Dati di test già presenti nel database");

        }
        else {


            System.out.println("--- Popolamento dati fittizi ---");

            // 2. Creazione Specializzazioni
            Specializzazione cardiologia = new Specializzazione();
            cardiologia.setNome("Cardiologia");
            gestore.salva(cardiologia);

            Specializzazione chirurgia = new Specializzazione();
            chirurgia.setNome("Chirurgia");
            gestore.salva(chirurgia);

            // 3. Creazione Medici
            Medico medico1 = new Medico();
            medico1.setNome("Mario");
            medico1.setCognome("Rossi");
            medico1.setEmail("mario.rossi@gmail.com");
            medico1.setPasswordHash(PasswordUtils.generaPasswordHash("mario"));
            medico1.setNumeroCellulare("3331234567");
            medico1.setSpecializzazione(cardiologia);
            medico1.setRuolo(Ruolo.MEDICO);
            gestore.salva(medico1);

            Medico medico2 = new Medico();
            medico2.setNome("Luigi");
            medico2.setCognome("Bianchi");
            medico2.setEmail("luigi.bianchi@gmail.com");
            medico2.setPasswordHash(PasswordUtils.generaPasswordHash("luigi"));
            medico2.setNumeroCellulare("3331234568");
            medico2.setSpecializzazione(chirurgia);
            medico2.setRuolo(Ruolo.MEDICO);
            gestore.salva(medico2);
        }
        long numeroPrenotazioni = gestore.conta(Prenotazione.class);
        if (numeroPrenotazioni == 0){

        // 4. Creazione Pazienti
        Paziente paziente1 = new Paziente();
        paziente1.setNome("Anna");
        paziente1.setCognome("Verdi");
        paziente1.setEmail("anna.verdi@gmail.com");
        paziente1.setPasswordHash(PasswordUtils.generaPasswordHash("anna"));
        paziente1.setCodiceFiscale("VRDNNA90A01H501Z");
        paziente1.setRuolo(Ruolo.PAZIENTE);
        gestore.salva(paziente1);

        Paziente paziente2 = new Paziente();
        paziente2.setNome("Giuseppe");
        paziente2.setCognome("Neri");
        paziente2.setEmail("giuseppe.neri@gmail.com");
        paziente2.setPasswordHash(PasswordUtils.generaPasswordHash("giuseppe"));
        paziente2.setCodiceFiscale("NREGRP85B02L219X");
        paziente2.setRuolo(Ruolo.PAZIENTE);
        gestore.salva(paziente2);

        // 4.5 Creazione Amministratore
        Amministratore amministratore1 = new Amministratore();
        amministratore1.setNome("Amministratore");
        amministratore1.setCognome("Amministratore");
        amministratore1.setEmail("admin@gmail.com");
        amministratore1.setPasswordHash(PasswordUtils.generaPasswordHash("admin"));
        amministratore1.setRuolo(Ruolo.AMMINISTRATORE);
        gestore.salva(amministratore1);

        // 5. Creazione Fasce Orarie e Prenotazioni
        
        // Fascia passata per Medico 1 (Mario Rossi) - Oggi, 2 ore fa
        FasciaOraria f1 = new FasciaOraria();
        Medico medico1 = gestore.cercaPerCampo(Medico.class, "email", "mario.rossi@gmail.com").getFirst();
        f1.setMedico(medico1);
        f1.setData(LocalDate.now());
        f1.setOraInizio(LocalTime.now().minusHours(2).withMinute(0).withSecond(0));
        f1.setOraFine(f1.getOraInizio().plusMinutes(30));
        f1.setStato(StatoFascia.OCCUPATA);
        gestore.salva(f1);

        Prenotazione pren1 = new Prenotazione();
        pren1.setPaziente(paziente1);
        pren1.setFasciaOraria(f1);
        pren1.setDataCreazione(LocalDate.now().minusDays(1));
        pren1.setStato(StatoPrenotazione.PRENOTATA);
        gestore.salva(pren1);

        // Fascia futura per Medico 1 (Mario Rossi) - Domani, ore 10:00
        FasciaOraria f2 = new FasciaOraria();
        f2.setMedico(medico1);
        f2.setData(LocalDate.now().plusDays(1));
        f2.setOraInizio(LocalTime.of(10, 0));
        f2.setOraFine(LocalTime.of(10, 30));
        f2.setStato(StatoFascia.OCCUPATA);
        gestore.salva(f2);

        Prenotazione pren2 = new Prenotazione();
        pren2.setPaziente(paziente2);
        pren2.setFasciaOraria(f2);
        pren2.setDataCreazione(LocalDate.now().minusDays(2));
        pren2.setStato(StatoPrenotazione.PRENOTATA);
        gestore.salva(pren2);

        // Fascia libera per Medico 2 (Luigi Bianchi) - Oggi, tra 1 ora
        FasciaOraria f3 = new FasciaOraria();
        Medico medico2 = gestore.cercaPerCampo(Medico.class, "email", "luigi.bianchi@gmail.com").getFirst();
        f3.setMedico(medico2);
        f3.setData(LocalDate.now());
        f3.setOraInizio(LocalTime.now().plusHours(1).withMinute(0).withSecond(0));
        f3.setOraFine(f3.getOraInizio().plusMinutes(30));
        f3.setStato(StatoFascia.LIBERA);
        gestore.salva(f3);
        }

        System.out.println("--- Dati fittizi inseriti con successo! ---");
    }
}
