package com.ambulatorio.boundary;

import com.ambulatorio.controller.CalendarioController;
import com.ambulatorio.controller.MedicoController;
import com.ambulatorio.controller.NotificaController;
import com.ambulatorio.controller.PrenotazioneController;
import com.ambulatorio.database.GestorePersistenza;
import com.ambulatorio.entity.FasciaOraria;
import com.ambulatorio.entity.Medico;
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
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

/**
 * Boundary del caso d'uso PrenotaVisita.
 *
 * Questa classe è collegata al file PrenotazioneVisitaView.form creato con
 * IntelliJ Swing UI Designer. Nel file .java viene gestita solo la logica
 * degli eventi e il collegamento con i controller.
 */
public class PrenotazioneVisitaView extends JFrame {

    public JPanel contentPane;
    private JLabel lblTitolo;
    private JLabel lblSpecializzazione;
    private JLabel lblMedico;
    private JLabel lblFasce;
    private JLabel lblRiepilogo;
    private JComboBox cmbSpecializzazioni;
    private JComboBox cmbMedici;
    private JList listaFasce;
    private JTextArea txtRiepilogo;
    private JButton btnAggiornaFasce;
    private JButton btnConferma;

    private DefaultListModel<FasciaOrariaItem> modelloFasce;

    private final Long idPazienteAutenticato;
    private final MedicoController medicoController;
    private final PrenotazioneController prenotazioneController;
    private final GestorePersistenza gestorePersistenza;

    /**
     * Costruttore provvisorio utile finché il login non passa ancora il paziente autenticato.
     * Nel flusso definitivo va usato PrenotazioneVisitaView(Long idPazienteAutenticato).
     */
    public PrenotazioneVisitaView() {
        this(1L);
    }

    public PrenotazioneVisitaView(Long idPazienteAutenticato) {
        this.idPazienteAutenticato = idPazienteAutenticato;
        this.medicoController = MedicoController.getInstance();
        this.prenotazioneController = PrenotazioneController.getInstance();
        this.gestorePersistenza = new GestorePersistenza();

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

        cmbMedici.setEnabled(false);
        btnAggiornaFasce.setEnabled(false);
        btnConferma.setEnabled(false);
    }

    private void collegaEventi() {
        cmbSpecializzazioni.addActionListener(e -> caricaMedici());
        cmbMedici.addActionListener(e -> caricaFasceDisponibili());
        btnAggiornaFasce.addActionListener(e -> caricaFasceDisponibili());
        btnConferma.addActionListener(e -> confermaPrenotazione());
    }

    public void mostraSpecializzazioni(List<MedicoController.SpecializzazioneInfo> specializzazioni) {
        cmbSpecializzazioni.setModel(new DefaultComboBoxModel<>(
                specializzazioni.toArray(new MedicoController.SpecializzazioneInfo[0])
        ));
    }

    public MedicoController.SpecializzazioneInfo leggiSpecializzazioneSelezionata() {
        return (MedicoController.SpecializzazioneInfo) cmbSpecializzazioni.getSelectedItem();
    }

    public void mostraMedici(List<MedicoController.MedicoInfo> medici) {
        cmbMedici.setModel(new DefaultComboBoxModel<>(
                medici.toArray(new MedicoController.MedicoInfo[0])
        ));
    }

    public MedicoController.MedicoInfo leggiMedicoSelezionato() {
        return (MedicoController.MedicoInfo) cmbMedici.getSelectedItem();
    }

    public void mostraFasceOrarie(List<FasciaOrariaItem> fasce) {
        modelloFasce.clear();

        for (FasciaOrariaItem fascia : fasce) {
            modelloFasce.addElement(fascia);
        }

        btnConferma.setEnabled(!fasce.isEmpty());
    }

    public FasciaOrariaItem leggiFasciaSelezionata() {
        return (FasciaOrariaItem) listaFasce.getSelectedValue();
    }

    public void mostraMessaggio(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio);
    }

    private void caricaSpecializzazioni() {
        List<MedicoController.SpecializzazioneInfo> specializzazioni = medicoController.getAllSpecializzazioni();
        mostraSpecializzazioni(specializzazioni);

        if (specializzazioni.isEmpty()) {
            mostraMessaggio("Non sono presenti specializzazioni configurate nel sistema.");
            cmbMedici.setEnabled(false);
            btnAggiornaFasce.setEnabled(false);
        } else {
            cmbMedici.setEnabled(true);
            btnAggiornaFasce.setEnabled(true);
        }
    }

    private void caricaMedici() {
        MedicoController.SpecializzazioneInfo specializzazione = leggiSpecializzazioneSelezionata();

        if (specializzazione == null) {
            mostraMedici(List.of());
            mostraFasceOrarie(List.of());
            return;
        }

        List<MedicoController.MedicoInfo> medici = medicoController.getMediciBySpecializzazione(
                specializzazione.getId()
        );

        mostraMedici(medici);
        mostraFasceOrarie(List.of());
        txtRiepilogo.setText("");

        if (medici.isEmpty()) {
            mostraMessaggio("Non sono presenti medici per la specializzazione selezionata.");
        }
    }

    private void caricaFasceDisponibili() {
        MedicoController.MedicoInfo medico = leggiMedicoSelezionato();

        if (medico == null) {
            mostraFasceOrarie(List.of());
            return;
        }

        List<FasciaOrariaItem> fasce = getFasceLiberePerMedico(medico.getId());
        mostraFasceOrarie(fasce);
        txtRiepilogo.setText("");

        if (fasce.isEmpty()) {
            mostraMessaggio("Non ci sono fasce orarie disponibili per il medico selezionato.");
        }
    }

    private List<FasciaOrariaItem> getFasceLiberePerMedico(Long idMedico) {
        Medico medico = gestorePersistenza.trovaPerId(Medico.class, idMedico);

        if (medico == null) {
            return List.of();
        }

        List<FasciaOraria> fasce = gestorePersistenza.cercaPerCampo(
                FasciaOraria.class,
                "medico",
                medico
        );

        return fasce.stream()
                .filter(fascia -> fascia.getStato() == StatoFascia.LIBERA)
                .filter(fascia -> fascia.getData() == null || !fascia.getData().isBefore(LocalDate.now()))
                .sorted(Comparator.comparing(FasciaOraria::getData)
                        .thenComparing(FasciaOraria::getOraInizio))
                .map(this::toFasciaOrariaItem)
                .toList();
    }

    private FasciaOrariaItem toFasciaOrariaItem(FasciaOraria fascia) {
        String medico = "";

        if (fascia.getMedico() != null) {
            medico = fascia.getMedico().getCognome() + " " + fascia.getMedico().getNome();
        }

        return new FasciaOrariaItem(
                fascia.getId(),
                fascia.getData(),
                fascia.getOraInizio(),
                fascia.getOraFine(),
                medico
        );
    }

    private void confermaPrenotazione() {
        if (idPazienteAutenticato == null) {
            mostraMessaggio("Impossibile procedere: paziente non autenticato.");
            return;
        }

        FasciaOrariaItem fascia = leggiFasciaSelezionata();

        if (fascia == null) {
            mostraMessaggio("Seleziona una fascia oraria prima di confermare la prenotazione.");
            return;
        }

        boolean disponibile = prenotazioneController.verificaDisponibilitaFascia(fascia.getId());

        if (!disponibile) {
            mostraMessaggio("La fascia oraria selezionata non è più disponibile. Scegli una nuova fascia.");
            caricaFasceDisponibili();
            return;
        }

        boolean prenotazioneEffettuata = prenotazioneController.effettuaPrenotazione(
                idPazienteAutenticato,
                fascia.getId()
        );

        if (!prenotazioneEffettuata) {
            mostraMessaggio("Prenotazione non completata. Verifica paziente e fascia oraria selezionata.");
            caricaFasceDisponibili();
            return;
        }

        mostraRiepilogo(fascia);
        mostraMessaggio("Prenotazione registrata correttamente. È stata inviata una notifica di conferma.");
        caricaFasceDisponibili();
    }

    private void mostraRiepilogo(FasciaOrariaItem fascia) {
        txtRiepilogo.setText(
                "Prenotazione confermata.\n"
                        + "Paziente ID: " + idPazienteAutenticato + "\n"
                        + "Medico: " + fascia.getMedico() + "\n"
                        + "Data visita: " + fascia.getData() + "\n"
                        + "Orario: " + fascia.getOraInizio() + " - " + fascia.getOraFine() + "\n"
                        + "Stato: PRENOTATA"
        );
    }

    private static class FasciaOrariaItem {
        private final Long id;
        private final LocalDate data;
        private final LocalTime oraInizio;
        private final LocalTime oraFine;
        private final String medico;

        private FasciaOrariaItem(Long id, LocalDate data, LocalTime oraInizio, LocalTime oraFine, String medico) {
            this.id = id;
            this.data = data;
            this.oraInizio = oraInizio;
            this.oraFine = oraFine;
            this.medico = medico;
        }

        private Long getId() {
            return id;
        }

        private LocalDate getData() {
            return data;
        }

        private LocalTime getOraInizio() {
            return oraInizio;
        }

        private LocalTime getOraFine() {
            return oraFine;
        }

        private String getMedico() {
            return medico;
        }

        @Override
        public String toString() {
            return data + " | " + oraInizio + " - " + oraFine + " | " + medico;
        }
    }
}
