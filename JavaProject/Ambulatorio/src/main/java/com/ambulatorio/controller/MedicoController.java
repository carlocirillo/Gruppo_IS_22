package com.ambulatorio.controller;

import com.ambulatorio.database.GestorePersistenza;
import com.ambulatorio.dto.response.MedicoDto;
import com.ambulatorio.dto.response.SpecializzazioneDto;
import com.ambulatorio.entity.Medico;
import com.ambulatorio.entity.Specializzazione;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Controller responsabile del recupero delle specializzazioni e dei medici.
 *
 * È usato dal caso d'uso PrenotaVisita per permettere al paziente di scegliere
 * prima la specializzazione e poi il medico desiderato.
 */
public class MedicoController {
    private final GestorePersistenza gestorePersistenza;

    public MedicoController(GestorePersistenza gestore) {
        this.gestorePersistenza = gestore;
    }

    public List<SpecializzazioneDto> getAllSpecializzazioni() {
        List<Specializzazione> specializzazioni = gestorePersistenza.cercaTutti(Specializzazione.class);

        List<SpecializzazioneDto> specializzazioniDto = new ArrayList<SpecializzazioneDto>();

        for (Specializzazione spec : specializzazioni) {
            SpecializzazioneDto specDto = new SpecializzazioneDto(
                    spec.getId(),
                    spec.getNome()
            );
            specializzazioniDto.add(specDto);
        }

        return specializzazioniDto;
    }

    public List<MedicoDto> getAllMedici() {
        List<Medico> medici = gestorePersistenza.cercaTutti(Medico.class);

        List<MedicoDto> mediciDto = new ArrayList<MedicoDto>();

        for (Medico medico : medici) {
            MedicoDto medicoDto = new MedicoDto(
                    medico.getId(),
                    medico.getNome(),
                    medico.getCognome(),
                    new SpecializzazioneDto(
                            medico.getSpecializzazione().getId(),
                            medico.getSpecializzazione().getNome()
                    )
            );
            mediciDto.add(medicoDto);
        }

        return mediciDto;
    }

    public List<MedicoDto> getMediciBySpecializzazione(Long idSpecializzazione) {
        if (idSpecializzazione == null) {
            return new ArrayList<>();
        }

        Specializzazione specializzazione = gestorePersistenza.trovaPerId(Specializzazione.class, idSpecializzazione);

        if (specializzazione == null) {
            return new ArrayList<>();
        }

        List<Medico> medici = gestorePersistenza.cercaPerCampo(Medico.class,"specializzazione", specializzazione);

        List<MedicoDto> mediciDto = new ArrayList<MedicoDto>();

        for (Medico medico : medici) {
            MedicoDto medicoDto = new MedicoDto(
                    medico.getId(),
                    medico.getNome(),
                    medico.getCognome(),
                    new SpecializzazioneDto(
                            medico.getSpecializzazione().getId(),
                            medico.getSpecializzazione().getNome()
                    )
            );
            mediciDto.add(medicoDto);
        }

        return mediciDto;
    }
}
