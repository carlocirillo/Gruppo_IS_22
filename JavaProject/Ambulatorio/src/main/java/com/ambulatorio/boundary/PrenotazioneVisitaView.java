package com.ambulatorio.boundary;

import com.ambulatorio.controller.CalendarioController;
import com.ambulatorio.controller.MedicoController;
import com.ambulatorio.controller.PrenotazioneController;
import com.ambulatorio.dto.response.FasciaOrariaDto;
import com.ambulatorio.dto.response.MedicoDto;
import com.ambulatorio.dto.response.SpecializzazioneDto;
import com.ambulatorio.entity.enums.StatoFascia;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;


public class PrenotazioneVisitaView extends JFrame {

    public JPanel contentPane;
    private JLabel lblTitolo;
    private JLabel lblSpecializzazione;
    private JLabel lblMedico;
    private JLabel lblFasce;
    private JLabel lblRiepilogo;
    private JComboBox<SpecializzazioneItem> cmbSpecializzazioni;
    private JComboBox<MedicoItem> cmbMedici;
    private JList<FasciaOrariaItem> listaFasce;
    private JTextArea txtRiepilogo;
    private JButton btnAggiornaFasce;
    private JButton btnConferma;

    private static final int MESI_DA_VISUALIZZARE = 3;
    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private DefaultListModel<FasciaOrariaItem> modelloFasce;

    private final MedicoController medicoController;
    private final CalendarioController calendarioController;
    private final PrenotazioneController prenotazioneController;
    private final Long idPazienteAutenticato;

    public PrenotazioneVisitaView(
            MedicoController medicoController,
            CalendarioController calendarioController,
            PrenotazioneController prenotazioneController,
            Long idPazienteAutenticato
    ) {
        this.medicoController = Objects.requireNonNull(medicoController, "Il MedicoController non può essere null");
        this.calendarioController = Objects.requireNonNull(calendarioController, "Il CalendarioController non può essere null");
        this.prenotazioneController = Objects.requireNonNull(prenotazioneController, "Il PrenotazioneController non può essere null");
        this.idPazienteAutenticato = idPazienteAutenticato;

        inizializzaFrame();
        inizializzaComponenti();
        collegaEventi();
        caricaSpecializzazioni();
    }

    private void inizializzaFrame() {
        setTitle("Prenota visita");
        setContentPane(contentPane);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(850, 600);
        setLocationRelativeTo(null);
    }

    private void inizializzaComponenti() {
        modelloFasce = new DefaultListModel<>();
        listaFasce.setModel(modelloFasce);
        listaFasce.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        txtRiepilogo.setEditable(false);
        txtRiepilogo.setLineWrap(true);
        txtRiepilogo.setWrapStyleWord(true);
        txtRiepilogo.setText("Seleziona una specializzazione, un medico e una fascia oraria disponibile.");

        cmbMedici.setEnabled(false);
        btnAggiornaFasce.setEnabled(false);
        btnConferma.setEnabled(false);
    }

    private void collegaEventi() {
        cmbSpecializzazioni.addActionListener(e -> caricaMediciPerSpecializzazione());
        cmbMedici.addActionListener(e -> caricaFasceDisponibiliPerMedico());
        btnAggiornaFasce.addActionListener(e -> caricaFasceDisponibiliPerMedico());
        btnConferma.addActionListener(e -> confermaPrenotazione());

        listaFasce.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                aggiornaRiepilogoSelezione();
            }
        });
    }

    private void caricaSpecializzazioni() {
        try {
            List<SpecializzazioneDto> specializzazioni = medicoController.getAllSpecializzazioni();
            mostraSpecializzazioni(specializzazioni);

            boolean presenti = !specializzazioni.isEmpty();
            cmbSpecializzazioni.setEnabled(presenti);
            cmbMedici.setEnabled(false);
            btnAggiornaFasce.setEnabled(false);
            btnConferma.setEnabled(false);

            if (!presenti) {
                txtRiepilogo.setText("Non sono presenti specializzazioni configurate nel sistema.");
                mostraMessaggio("Non sono presenti specializzazioni configurate nel sistema.");
            }
        } catch (RuntimeException ex) {
            mostraErrore("Errore durante il caricamento delle specializzazioni", ex);
        }
    }

    private void caricaMediciPerSpecializzazione() {
        SpecializzazioneItem specializzazioneSelezionata = leggiSpecializzazioneSelezionata();

        pulisciMedici();
        pulisciFasce();

        if (specializzazioneSelezionata == null) {
            return;
        }

        try {
            List<MedicoDto> medici = medicoController.getMediciBySpecializzazione(
                    specializzazioneSelezionata.getDto().id()
            );

            mostraMedici(medici);
            cmbMedici.setEnabled(!medici.isEmpty());
            btnAggiornaFasce.setEnabled(!medici.isEmpty());

            if (medici.isEmpty()) {
                txtRiepilogo.setText("Non sono presenti medici per la specializzazione selezionata.");
                mostraMessaggio("Non sono presenti medici per la specializzazione selezionata.");
            } else {
                txtRiepilogo.setText("Seleziona un medico per visualizzare le fasce disponibili.");
            }
        } catch (RuntimeException ex) {
            mostraErrore("Errore durante il caricamento dei medici", ex);
        }
    }

    private void caricaFasceDisponibiliPerMedico() {
        MedicoItem medicoSelezionato = leggiMedicoSelezionato();
        pulisciFasce();

        if (medicoSelezionato == null) {
            return;
        }

        try {
            LocalDate dataInizio = LocalDate.now();
            LocalDate dataFine = dataInizio.plusMonths(MESI_DA_VISUALIZZARE);

            List<FasciaOrariaDto> fasce = calendarioController.getDisponibilitaPerMedico(
                    medicoSelezionato.getDto().id(),
                    dataInizio,
                    dataFine
            );

            mostraFasceOrarie(fasce);

            if (fasce.isEmpty()) {
                txtRiepilogo.setText("Non ci sono fasce orarie disponibili per il medico selezionato.");
                mostraMessaggio("Non ci sono fasce orarie disponibili per il medico selezionato.");
            } else {
                txtRiepilogo.setText("Seleziona una fascia oraria e conferma la prenotazione.");
            }
        } catch (RuntimeException ex) {
            mostraErrore("Errore durante il caricamento delle fasce disponibili", ex);
        }
    }

    private void confermaPrenotazione() {
        if (idPazienteAutenticato == null) {
            mostraMessaggio("Impossibile procedere: paziente non autenticato.");
            return;
        }

        FasciaOrariaItem fasciaSelezionata = leggiFasciaSelezionata();
        if (fasciaSelezionata == null) {
            mostraMessaggio("Seleziona una fascia oraria prima di confermare la prenotazione.");
            return;
        }

        try {
            StatoFascia statoCorrente = calendarioController.verificaDisponibilitaFascia(
                    fasciaSelezionata.getDto().id()
            );

            if (statoCorrente != StatoFascia.LIBERA) {
                mostraMessaggio("La fascia oraria selezionata non è più disponibile. Scegli una nuova fascia.");
                caricaFasceDisponibiliPerMedico();
                return;
            }

            prenotazioneController.effettuaPrenotazione(
                    idPazienteAutenticato,
                    fasciaSelezionata.getDto().id()
            );

            mostraRiepilogoPrenotazioneConfermata(fasciaSelezionata);
            mostraMessaggio("Prenotazione registrata correttamente. È stata inviata una notifica di conferma.");
            caricaFasceDisponibiliPerMedico();
        } catch (RuntimeException ex) {
            mostraErrore("Prenotazione non completata", ex);
            caricaFasceDisponibiliPerMedico();
        }
    }

    private void mostraSpecializzazioni(List<SpecializzazioneDto> specializzazioni) {
        DefaultComboBoxModel<SpecializzazioneItem> model = new DefaultComboBoxModel<>();

        for (SpecializzazioneDto specializzazione : specializzazioni) {
            model.addElement(new SpecializzazioneItem(specializzazione));
        }

        cmbSpecializzazioni.setModel(model);
    }

    private void mostraMedici(List<MedicoDto> medici) {
        DefaultComboBoxModel<MedicoItem> model = new DefaultComboBoxModel<>();

        for (MedicoDto medico : medici) {
            model.addElement(new MedicoItem(medico));
        }

        cmbMedici.setModel(model);
    }

    private void mostraFasceOrarie(List<FasciaOrariaDto> fasce) {
        modelloFasce.clear();

        for (FasciaOrariaDto fascia : fasce) {
            aggiungiFasciaAlModello(fascia);
        }

        btnConferma.setEnabled(false);
    }

    private void aggiungiFasciaAlModello(FasciaOrariaDto fascia) {
        modelloFasce.addElement(new FasciaOrariaItem(fascia));
    }

    private SpecializzazioneItem leggiSpecializzazioneSelezionata() {
        return (SpecializzazioneItem) cmbSpecializzazioni.getSelectedItem();
    }

    private MedicoItem leggiMedicoSelezionato() {
        return (MedicoItem) cmbMedici.getSelectedItem();
    }

    private FasciaOrariaItem leggiFasciaSelezionata() {
        return listaFasce.getSelectedValue();
    }

    private void pulisciMedici() {
        cmbMedici.setModel(new DefaultComboBoxModel<MedicoItem>());
        cmbMedici.setEnabled(false);
        btnAggiornaFasce.setEnabled(false);
    }

    private void pulisciFasce() {
        modelloFasce.clear();
        btnConferma.setEnabled(false);
    }

    private void aggiornaRiepilogoSelezione() {
        FasciaOrariaItem fascia = leggiFasciaSelezionata();
        MedicoItem medico = leggiMedicoSelezionato();

        btnConferma.setEnabled(fascia != null);

        if (fascia == null) {
            return;
        }

        String nomeMedico = medico != null ? medico.toString() : "Medico selezionato";

        txtRiepilogo.setText(
                "Riepilogo prenotazione da confermare:\n"
                        + "Paziente ID: " + idPazienteAutenticato + "\n"
                        + "Medico: " + nomeMedico + "\n"
                        + "Data visita: " + formattaData(fascia.getDto().data()) + "\n"
                        + "Orario: " + fascia.getDto().orainizio() + " - " + fascia.getDto().oraFine() + "\n"
                        + "Stato fascia: " + fascia.getDto().stato()
        );
    }

    private void mostraRiepilogoPrenotazioneConfermata(FasciaOrariaItem fascia) {
        MedicoItem medico = leggiMedicoSelezionato();
        String nomeMedico = medico != null ? medico.toString() : "Medico selezionato";

        txtRiepilogo.setText(
                "Prenotazione confermata.\n"
                        + "Paziente ID: " + idPazienteAutenticato + "\n"
                        + "Medico: " + nomeMedico + "\n"
                        + "Data visita: " + formattaData(fascia.getDto().data()) + "\n"
                        + "Orario: " + fascia.getDto().orainizio() + " - " + fascia.getDto().oraFine() + "\n"
                        + "Stato prenotazione: PRENOTATA"
        );
    }

    private String formattaData(LocalDate data) {
        if (data == null) {
            return "Data non disponibile";
        }

        return data.format(DATA_FORMATTER);
    }

    private void mostraMessaggio(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio);
    }

    private void mostraErrore(String titolo, RuntimeException ex) {
        String dettaglio = ex.getMessage() != null ? ex.getMessage() : "Errore non specificato";
        JOptionPane.showMessageDialog(this, titolo + ":\n" + dettaglio, "Errore", JOptionPane.ERROR_MESSAGE);
    }

    private static class SpecializzazioneItem {
        private final SpecializzazioneDto dto;

        private SpecializzazioneItem(SpecializzazioneDto dto) {
            this.dto = dto;
        }

        private SpecializzazioneDto getDto() {
            return dto;
        }

        @Override
        public String toString() {
            return dto.nomeSpecializzazione();
        }
    }

    private static class MedicoItem {
        private final MedicoDto dto;

        private MedicoItem(MedicoDto dto) {
            this.dto = dto;
        }

        private MedicoDto getDto() {
            return dto;
        }

        @Override
        public String toString() {
            return dto.cognome() + " " + dto.nome();
        }
    }

    private static class FasciaOrariaItem {
        private final FasciaOrariaDto dto;

        private FasciaOrariaItem(FasciaOrariaDto dto) {
            this.dto = dto;
        }

        private FasciaOrariaDto getDto() {
            return dto;
        }

        @Override
        public String toString() {
            String data = dto.data() != null ? dto.data().format(DATA_FORMATTER) : "Data non disponibile";
            return data + " | " + dto.orainizio() + " - " + dto.oraFine();
        }
    }
}
