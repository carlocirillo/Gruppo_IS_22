package com.ambulatorio.controller;

import com.ambulatorio.DTO.response.StatisticheDTO;
import com.ambulatorio.boundary.AreaAmministratoreView;
import com.ambulatorio.entity.Paziente;
import com.ambulatorio.entity.Amministratore;
import com.ambulatorio.entity.Medico;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class PrenotazioneController {

    private CalendarioController calendarioController;
    private NotificaController notificaController;

    public PrenotazioneController(CalendarioController calendarioController, NotificaController notificaController){

        this.calendarioController = calendarioController;
        this.notificaController = notificaController;
    }

    /*public StatisticheDTO calcolaReportStatistiche(LocalDate dataInizio, LocalDate dataFine) {

        Map<String, Object> filtraPeriodo = new HashMap<>();


    }*/
}
