package com.ambulatorio.boundary;

import com.ambulatorio.controller.CalendarioController;
import com.ambulatorio.controller.MedicoController;
import com.ambulatorio.controller.PrenotazioneController;
import com.ambulatorio.dto.response.FasciaOrariaDto;
import com.ambulatorio.dto.response.MedicoDto;
import com.ambulatorio.dto.response.SpecializzazioneDto;
import com.ambulatorio.entity.enums.StatoFascia;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
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
import java.awt.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Boundary del caso d'uso PrenotaVisita.
 *
 * La view riceve i controller dal Main e non crea né controller né GestorePersistenza.
 * Lavora direttamente sui DTO restituiti dai controller, senza classi Item intermedie.
 */
public class PrenotazioneVisitaView extends JFrame {

    public JPanel contentPane;
    private JLabel lblTitolo;
    private JLabel lblSpecializzazione;
    private JLabel lblMedico;
    private JLabel lblFasce;
    private JLabel lblRiepilogo;
    private JComboBox<SpecializzazioneDto> cmbSpecializzazioni;
    private JComboBox<MedicoDto> cmbMedici;
    private JList<FasciaOrariaDto> listaFasce;
    private JTextArea txtRiepilogo;
    private JButton btnAggiornaFasce;
    private JButton btnConferma;

    private static final int MESI_DA_VISUALIZZARE = 3;
    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private DefaultListModel<FasciaOrariaDto> modelloFasce;

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

        configuraRendererSpecializzazioni();
        configuraRendererMedici();
        configuraRendererFasce();

        txtRiepilogo.setEditable(false);
        txtRiepilogo.setLineWrap(true);
        txtRiepilogo.setWrapStyleWord(true);
        txtRiepilogo.setText("Seleziona una specializzazione, un medico e una fascia oraria disponibile.");

        cmbMedici.setEnabled(false);
        btnAggiornaFasce.setEnabled(false);
        btnConferma.setEnabled(false);
    }

    private void configuraRendererSpecializzazioni() {
        cmbSpecializzazioni.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof SpecializzazioneDto specializzazione) {
                    setText(specializzazione.nomeSpecializzazione());
                } else {
                    setText("");
                }

                return this;
            }
        });
    }

    private void configuraRendererMedici() {
        cmbMedici.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof MedicoDto medico) {
                    setText(formattaMedico(medico));
                } else {
                    setText("");
                }

                return this;
            }
        });
    }

    private void configuraRendererFasce() {
        listaFasce.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof FasciaOrariaDto fascia) {
                    setText(formattaFascia(fascia));
                } else {
                    setText("");
                }

                return this;
            }
        });
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
        SpecializzazioneDto specializzazioneSelezionata = leggiSpecializzazioneSelezionata();

        pulisciMedici();
        pulisciFasce();

        if (specializzazioneSelezionata == null) {
            return;
        }

        try {
            List<MedicoDto> medici = medicoController.getMediciBySpecializzazione(
                    specializzazioneSelezionata.id()
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
        MedicoDto medicoSelezionato = leggiMedicoSelezionato();
        pulisciFasce();

        if (medicoSelezionato == null) {
            return;
        }

        try {
            LocalDate dataInizio = LocalDate.now();
            LocalDate dataFine = dataInizio.plusMonths(MESI_DA_VISUALIZZARE);

            List<FasciaOrariaDto> fasce = calendarioController.getDisponibilitaPerMedico(
                    medicoSelezionato.id(),
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

        FasciaOrariaDto fasciaSelezionata = leggiFasciaSelezionata();
        if (fasciaSelezionata == null) {
            mostraMessaggio("Seleziona una fascia oraria prima di confermare la prenotazione.");
            return;
        }

        try {
            StatoFascia statoCorrente = calendarioController.verificaDisponibilitaFascia(
                    fasciaSelezionata.id()
            );

            if (statoCorrente != StatoFascia.LIBERA) {
                mostraMessaggio("La fascia oraria selezionata non è più disponibile. Scegli una nuova fascia.");
                caricaFasceDisponibiliPerMedico();
                return;
            }

            prenotazioneController.effettuaPrenotazione(
                    idPazienteAutenticato,
                    fasciaSelezionata.id()
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
        DefaultComboBoxModel<SpecializzazioneDto> model = new DefaultComboBoxModel<>();

        for (SpecializzazioneDto specializzazione : specializzazioni) {
            model.addElement(specializzazione);
        }

        cmbSpecializzazioni.setModel(model);
    }

    private void mostraMedici(List<MedicoDto> medici) {
        DefaultComboBoxModel<MedicoDto> model = new DefaultComboBoxModel<>();

        for (MedicoDto medico : medici) {
            model.addElement(medico);
        }

        cmbMedici.setModel(model);
    }

    private void mostraFasceOrarie(List<FasciaOrariaDto> fasce) {
        modelloFasce.clear();

        for (FasciaOrariaDto fascia : fasce) {
            modelloFasce.addElement(fascia);
        }

        btnConferma.setEnabled(false);
    }

    private SpecializzazioneDto leggiSpecializzazioneSelezionata() {
        Object selezionato = cmbSpecializzazioni.getSelectedItem();

        if (selezionato instanceof SpecializzazioneDto specializzazione) {
            return specializzazione;
        }

        return null;
    }

    private MedicoDto leggiMedicoSelezionato() {
        Object selezionato = cmbMedici.getSelectedItem();

        if (selezionato instanceof MedicoDto medico) {
            return medico;
        }

        return null;
    }

    private FasciaOrariaDto leggiFasciaSelezionata() {
        return listaFasce.getSelectedValue();
    }

    private void pulisciMedici() {
        cmbMedici.setModel(new DefaultComboBoxModel<>());
        cmbMedici.setEnabled(false);
        btnAggiornaFasce.setEnabled(false);
    }

    private void pulisciFasce() {
        modelloFasce.clear();
        btnConferma.setEnabled(false);
    }

    private void aggiornaRiepilogoSelezione() {
        FasciaOrariaDto fascia = leggiFasciaSelezionata();
        MedicoDto medico = leggiMedicoSelezionato();

        btnConferma.setEnabled(fascia != null);

        if (fascia == null) {
            return;
        }

        String nomeMedico = medico != null ? formattaMedico(medico) : "Medico selezionato";

        txtRiepilogo.setText(
                "Riepilogo prenotazione da confermare:\n"
                        + "Paziente ID: " + idPazienteAutenticato + "\n"
                        + "Medico: " + nomeMedico + "\n"
                        + "Data visita: " + formattaData(fascia.data()) + "\n"
                        + "Orario: " + fascia.orainizio() + " - " + fascia.oraFine() + "\n"
                        + "Stato fascia: " + fascia.stato()
        );
    }

    private void mostraRiepilogoPrenotazioneConfermata(FasciaOrariaDto fascia) {
        MedicoDto medico = leggiMedicoSelezionato();
        String nomeMedico = medico != null ? formattaMedico(medico) : "Medico selezionato";

        txtRiepilogo.setText(
                "Prenotazione confermata.\n"
                        + "Paziente ID: " + idPazienteAutenticato + "\n"
                        + "Medico: " + nomeMedico + "\n"
                        + "Data visita: " + formattaData(fascia.data()) + "\n"
                        + "Orario: " + fascia.orainizio() + " - " + fascia.oraFine() + "\n"
                        + "Stato prenotazione: PRENOTATA"
        );
    }

    private String formattaMedico(MedicoDto medico) {
        if (medico == null) {
            return "";
        }

        return medico.cognome() + " " + medico.nome();
    }

    private String formattaFascia(FasciaOrariaDto fascia) {
        if (fascia == null) {
            return "";
        }

        return formattaData(fascia.data()) + " | " + fascia.orainizio() + " - " + fascia.oraFine();
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
}
