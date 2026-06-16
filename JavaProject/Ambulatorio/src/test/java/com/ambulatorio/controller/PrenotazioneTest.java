package com.ambulatorio.controller;

import com.ambulatorio.database.GestorePersistenza;
import com.ambulatorio.entity.FasciaOraria;
import com.ambulatorio.entity.Paziente;
import com.ambulatorio.entity.enums.StatoFascia;
import com.ambulatorio.utils.JwtUtils;
import com.ambulatorio.utils.SessioneUtente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PrenotazioneTest {

    private GestorePersistenza gestorePersistenzaMock;
    private PrenotazioneController prenotazioneController;

    @BeforeEach
    public void setup() {
        gestorePersistenzaMock = mock(GestorePersistenza.class);
        prenotazioneController = new PrenotazioneController(gestorePersistenzaMock);

        String token = JwtUtils.generaToken("10", "mario.rossi@gmail.com", "PAZIENTE");
        SessioneUtente.getInstance().setToken(token);
    }

    @Test
    public void testCase1_PrenotazioneConsentita() {
        // 1. Pre-condizioni (Dati e Mocking)
        Paziente pazienteMock = new Paziente();
        pazienteMock.setId(10L);
        pazienteMock.setNome("Mario");
        pazienteMock.setCognome("Rossi");

        FasciaOraria fasciaMock = new FasciaOraria();
        fasciaMock.setId(2L);
        fasciaMock.setOraInizio(LocalTime.of(10, 0));
        fasciaMock.setOraFine(LocalTime.of(10, 30));
        fasciaMock.setData(LocalDate.now().plusDays(1));
        fasciaMock.setStato(StatoFascia.LIBERA);

        when(gestorePersistenzaMock.trovaPerId(Paziente.class, 10L)).thenReturn(pazienteMock);
        when(gestorePersistenzaMock.trovaPerId(FasciaOraria.class, 2L)).thenReturn(fasciaMock);
        when(gestorePersistenzaMock.salva(any())).thenReturn(true);

        // 2. Esecuzione
        assertDoesNotThrow(() -> prenotazioneController.effettuaPrenotazione(2L));

        // 3. Verifiche (Assert)
        assertEquals(StatoFascia.OCCUPATA, fasciaMock.getStato(),
                "La fascia oraria deve risultare OCCUPATA dopo la prenotazione.");
        verify(gestorePersistenzaMock, times(1)).aggiorna(fasciaMock);
        verify(gestorePersistenzaMock, atLeast(1)).salva(any());
    }

    @Test
    public void testCase2_MedicoNonSelezionato() {
        // 1. Pre-condizioni
        // Nessun mock necessario: la validazione avviene prima dell'accesso al DB

        // 2. Esecuzione e Verifica dell'Eccezione
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                prenotazioneController.effettuaPrenotazione(null));

        assertEquals("I parametri Paziente e Fascia Oraria non possono essere nulli", ex.getMessage());

        // 3. Verifica aggiuntiva
        verify(gestorePersistenzaMock, never()).aggiorna(any());
        verify(gestorePersistenzaMock, never()).salva(any());
    }

    @Test
    public void testCase3_FasciaOrariaNonSelezionata() {
        // 1. Pre-condizioni
        // Nessun mock necessario: la validazione avviene prima dell'accesso al DB

        // 2. Esecuzione e Verifica dell'Eccezione
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                prenotazioneController.effettuaPrenotazione(null));

        assertEquals("I parametri Paziente e Fascia Oraria non possono essere nulli", ex.getMessage());

        // 3. Verifica aggiuntiva
        verify(gestorePersistenzaMock, never()).aggiorna(any());
        verify(gestorePersistenzaMock, never()).salva(any());
    }

    @Test
    public void testCase4_FasciaOrariaNonDisponibile() {
        // 1. Pre-condizioni (Dati e Mocking)
        Paziente pazienteMock = new Paziente();
        pazienteMock.setId(10L);
        pazienteMock.setNome("Mario");
        pazienteMock.setCognome("Rossi");

        FasciaOraria fasciaOccupata = new FasciaOraria();
        fasciaOccupata.setId(3L);
        fasciaOccupata.setOraInizio(LocalTime.of(10, 0));
        fasciaOccupata.setOraFine(LocalTime.of(10, 30));
        fasciaOccupata.setData(LocalDate.now().plusDays(1));
        fasciaOccupata.setStato(StatoFascia.OCCUPATA);

        when(gestorePersistenzaMock.trovaPerId(Paziente.class, 10L)).thenReturn(pazienteMock);
        when(gestorePersistenzaMock.trovaPerId(FasciaOraria.class, 3L)).thenReturn(fasciaOccupata);

        // 2. Esecuzione e Verifica dell'Eccezione
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                prenotazioneController.effettuaPrenotazione(3L));

        assertEquals("La fascia oraria selezionata non è più disponibile", ex.getMessage());

        // 3. Verifica aggiuntiva
        assertEquals(StatoFascia.OCCUPATA, fasciaOccupata.getStato(),
                "La fascia deve restare OCCUPATA.");
        verify(gestorePersistenzaMock, never()).aggiorna(any());
        verify(gestorePersistenzaMock, never()).salva(any());
    }
}
