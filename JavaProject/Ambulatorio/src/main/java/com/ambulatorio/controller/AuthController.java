package com.ambulatorio.controller;

import com.ambulatorio.database.GestorePersistenza;

public class AuthController {
    private final GestorePersistenza gestorePersistenza;

    public AuthController(GestorePersistenza gestore) {
        this.gestorePersistenza = gestore;
    }
}
