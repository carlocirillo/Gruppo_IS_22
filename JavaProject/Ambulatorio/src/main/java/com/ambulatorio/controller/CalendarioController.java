package com.ambulatorio.controller;

public class CalendarioController {
    private static CalendarioController instance;

    private CalendarioController() {}

    public static CalendarioController getInstance() {
        if (instance == null) {
            instance = new CalendarioController();
        }
        return instance;
    }
}
