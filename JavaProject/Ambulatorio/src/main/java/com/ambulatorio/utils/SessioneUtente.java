package com.ambulatorio.utils;

public class SessioneUtente {
    private static SessioneUtente instance;

    private String token;

    private SessioneUtente() {}

    // Metodo per ottenere l'istanza globale
    public static SessioneUtente getInstance() {
        if (instance == null) {
            instance = new SessioneUtente();
        }
        return instance;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public Long getIdUtente() {
        if (token == null) return null;
        return Long.parseLong(JwtUtils.estraiRuolo(token));
    }

    public String getRuoloUtente() {
        if (token == null) return null;
        return JwtUtils.estraiRuolo(token);
    }

    // Pulisce la sessione al momento del logout
    public void logout() {
        this.token = null;
    }
}