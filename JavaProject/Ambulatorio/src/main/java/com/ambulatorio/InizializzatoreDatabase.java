package com.ambulatorio;

import com.ambulatorio.database.GestorePersistenza;
import com.ambulatorio.entity.Medico;
import com.ambulatorio.entity.Specializzazione;

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
            return;
        }

        System.out.println("--- Popolamento dati fittizi ---");

        // 2. Creazione Dati
        Specializzazione cardiologia = new Specializzazione();
        cardiologia.setNome("Cardiologia");

        Specializzazione chirurgia = new Specializzazione();
        chirurgia.setNome("Chirurgia");

        Medico medico1 = new Medico();
        medico1.setNome("Mario");
        medico1.setCognome("Rossi");
        medico1.setEmail("mario.rossi@email.com");
        medico1.setPasswordHash("hashMario");
        medico1.setNumeroCellulare("3331234567");
        medico1.setSpecializzazione(cardiologia);

        Medico medico2 = new Medico();
        medico2.setNome("Luigi");
        medico2.setCognome("Bianchi");
        medico2.setEmail("luigi.bianchi@email.com");
        medico2.setPasswordHash("hashLuigi");
        medico2.setNumeroCellulare("3331234568");
        medico2.setSpecializzazione(chirurgia);

        // 3. Salvataggio
        gestore.salva(cardiologia);
        gestore.salva(medico1);
        gestore.salva(chirurgia);
        gestore.salva(medico2);

        System.out.println("--- Dati fittizi inseriti con successo! ---");
    }
}