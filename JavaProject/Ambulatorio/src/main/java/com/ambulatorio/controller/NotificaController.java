package com.ambulatorio.controller;

public class NotificaController {
    private static NotificaController instance;

    private NotificaController() {}

    public static NotificaController getInstance() {
        if (instance == null) {
            instance = new NotificaController();
        }
        return instance;
    }
}
