package com.ambulatorio.controller;

import com.ambulatorio.database.GestorePersistenza;
import com.ambulatorio.dto.request.CredenzialiAccessoDto;
import com.ambulatorio.entity.Utente;
import com.ambulatorio.entity.enums.Ruolo;
import com.ambulatorio.exceptions.CredenzialiNonValideException;
import com.ambulatorio.utils.PasswordUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LoginTest {

    private GestorePersistenza gestorePersistenzaMock;
    private AuthController authController;

    @BeforeEach
    public void setup() {
        gestorePersistenzaMock = mock(GestorePersistenza.class);

        authController = new AuthController(gestorePersistenzaMock);

    }

    @Test
    public void testCase1_AccessoConsentito() {
        // 1. Pre-condizioni (Dati e Mocking)
        CredenzialiAccessoDto input = new CredenzialiAccessoDto("carlo.cirillo04@gmail.com", "c4r10.2004");

        Utente utenteMock = new Utente();
        utenteMock.setId(1L);
        utenteMock.setEmail("carlo.cirillo04@gmail.com");
        utenteMock.setPasswordHash(PasswordUtils.generaPasswordHash("c4r10.2004"));
        utenteMock.setRuolo(Ruolo.PAZIENTE);

        when(gestorePersistenzaMock.cercaPerCampo(Utente.class, "email", "carlo.cirillo04@gmail.com"))
                .thenReturn(List.of(utenteMock));

        // 2. Esecuzione
        String token = authController.login(input);

        // 3. Verifiche (Assert)
        assertNotNull(token, "Il token JWT non dovrebbe essere nullo in caso di login valido.");
        assertFalse(token.isEmpty(), "Il token JWT non dovrebbe essere vuoto.");
    }

    @Test
    public void testCase2_FormatoEmailNonValido() {
        // 1. Pre-condizioni
        CredenzialiAccessoDto input = new CredenzialiAccessoDto("carlo", "c4r10.2004");

        // 2. Esecuzione e Verifica dell'Eccezione
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            authController.login(input);
        });

        // 3. Verifichiamo che il messaggio sia esattamente quello della Regex
        assertEquals("Formato email non valido", ex.getMessage());

        // 4. Verifica aggiuntiva
        // Ci assicuriamo che, essendo fallita la validazione, il controller non abbia mai cambiato il database
        verify(gestorePersistenzaMock, never()).cercaPerCampo(any(), anyString(), anyString());
    }

    @Test
    public void testCase3_UtenteNonRegistrato() {
        // 1. Pre-condizioni
        CredenzialiAccessoDto input = new CredenzialiAccessoDto("pierino@gmail.com", "pierino2000");

        // Il database restituisce una lista vuota
        when(gestorePersistenzaMock.cercaPerCampo(Utente.class, "email", "pierino@gmail.com"))
                .thenReturn(Collections.emptyList());

        // 2. Esecuzione e Verifica dell'eccezione
        CredenzialiNonValideException ex = assertThrows(CredenzialiNonValideException.class, () -> {
            authController.login(input);
        });

        assertEquals("Email o password non valida.", ex.getMessage());
    }

    @Test
    public void testCase4_E_6_CampiVuotiONull() {
        CredenzialiAccessoDto inputEmailNulla = new CredenzialiAccessoDto(null, "pierino2000");
        CredenzialiAccessoDto inputPasswordNulla = new CredenzialiAccessoDto("carlo.cirillo04@gmail.com", null);

        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> {
            authController.login(inputEmailNulla);
        });
        assertEquals("Email e password sono obbligatorie", ex1.getMessage());

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> {
            authController.login(inputPasswordNulla);
        });
        assertEquals("Email e password sono obbligatorie", ex2.getMessage());
    }

    @Test
    public void testCase5_PasswordNonCorrispondente() {
        // 1. Pre-condizioni
        CredenzialiAccessoDto input = new CredenzialiAccessoDto("carlo.cirillo04@gmail.com", "pierino2000");

        Utente utenteMock = new Utente();
        utenteMock.setId(1L);
        utenteMock.setEmail("carlo.cirillo04@gmail.com");
        utenteMock.setPasswordHash(PasswordUtils.generaPasswordHash("c4r10.2004")); // Password corretta registrata
        utenteMock.setRuolo(Ruolo.PAZIENTE);

        when(gestorePersistenzaMock.cercaPerCampo(Utente.class, "email", "carlo.cirillo04@gmail.com"))
                .thenReturn(List.of(utenteMock));

        // 2. Esecuzione e Verifica (la password "pierino2000" fallirà il confronto)
        CredenzialiNonValideException ex = assertThrows(CredenzialiNonValideException.class, () -> {
            authController.login(input);
        });

        assertEquals("Email o password non valida.", ex.getMessage());
    }
}
