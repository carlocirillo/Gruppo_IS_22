package com.ambulatorio.boundary;

import com.ambulatorio.controller.CalendarioController;
import com.ambulatorio.controller.MedicoController;
import com.ambulatorio.controller.NotificaController;
import com.ambulatorio.controller.PrenotazioneController;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.List;

/**
 * Boundary del caso d'uso PrenotaVisita.
 *
 * La schermata consente al paziente autenticato di scegliere specializzazione,
 * medico e fascia oraria disponibile, poi conferma la prenotazione.
 */
public class PrenotazioneVisitaView extends JFrame {

    public JPanel contentPane;

    private final Long idPazienteAutenticato;
    private final MedicoController medicoController;
    private final CalendarioController calendarioController;
    private final PrenotazioneController prenotazioneController;

    private JComboBox<MedicoController.SpecializzazioneInfo> cmbSpecializzazioni;
    private JComboBox<MedicoController.MedicoInfo> cmbMedici;
    private JList<CalendarioController.FasciaOrariaInfo> listaFasce;
    private DefaultListModel<CalendarioController.FasciaOrariaInfo> modelloFasce;
    private JTextArea txtRiepilogo;
    private JButton btnAggiornaFasce;
    private JButton btnConferma;

    /**
     * Costruttore utile solo per prove rapide se il login non passa ancora il paziente.
     * Nel flusso definitivo va usato PrenotazioneVisitaView(Long idPazienteAutenticato).
     */
    public PrenotazioneVisitaView() {
        this(1L);
    }

    public PrenotazioneVisitaView(Long idPazienteAutenticato) {
        this.idPazienteAutenticato = idPazienteAutenticato;
        this.medicoController = new MedicoController();
        this.calendarioController = new CalendarioController();
        this.prenotazioneController = new PrenotazioneController(
                calendarioController,
                new NotificaController()
        );

        inizializzaComponenti();
        inizializzaFrame();
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
        contentPane = new JPanel(new BorderLayout(12, 12));
        contentPane.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel titolo = new JLabel("Prenotazione visita");
        titolo.setFont(titolo.getFont().deriveFont(22f));
        contentPane.add(titolo, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints vincoli = new GridBagConstraints();
        vincoli.insets = new Insets(8, 8, 8, 8);
        vincoli.fill = GridBagConstraints.HORIZONTAL;

        cmbSpecializzazioni = new JComboBox<>();
        cmbMedici = new JComboBox<>();
        btnAggiornaFasce = new JButton("Mostra fasce disponibili");
        btnConferma = new JButton("Conferma prenotazione");

        modelloFasce = new DefaultListModel<>();
        listaFasce = new JList<>(modelloFasce);
        listaFasce.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        txtRiepilogo = new JTextArea(7, 40);
        txtRiepilogo.setEditable(false);
        txtRiepilogo.setLineWrap(true);
        txtRiepilogo.setWrapStyleWord(true);

        aggiungiRiga(formPanel, vincoli, 0, "Specializzazione", cmbSpecializzazioni);
        aggiungiRiga(formPanel, vincoli, 1, "Medico", cmbMedici);
        aggiungiRiga(formPanel, vincoli, 2, "Fasce orarie", new JScrollPane(listaFasce));

        vincoli.gridx = 1;
        vincoli.gridy = 3;
        formPanel.add(btnAggiornaFasce, vincoli);

        vincoli.gridy = 4;
        formPanel.add(btnConferma, vincoli);

        vincoli.gridx = 0;
        vincoli.gridy = 5;
        vincoli.gridwidth = 2;
        vincoli.weightx = 1;
        formPanel.add(new JScrollPane(txtRiepilogo), vincoli);

        contentPane.add(formPanel, BorderLayout.CENTER);
    }

    private void aggiungiRiga(JPanel pannello,
                              GridBagConstraints vincoli,
                              int riga,
                              String etichetta,
                              java.awt.Component componente) {
        vincoli.gridx = 0;
        vincoli.gridy = riga;
        vincoli.gridwidth = 1;
        vincoli.weightx = 0;
        pannello.add(new JLabel(etichetta), vincoli);

        vincoli.gridx = 1;
        vincoli.weightx = 1;
        pannello.add(componente, vincoli);
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

    public void mostraFasceOrarie(List<CalendarioController.FasciaOrariaInfo> fasce) {
        modelloFasce.clear();

        for (CalendarioController.FasciaOrariaInfo fascia : fasce) {
            modelloFasce.addElement(fascia);
        }
    }

    public CalendarioController.FasciaOrariaInfo leggiFasciaSelezionata() {
        return listaFasce.getSelectedValue();
    }

    public void mostraRiepilogo(PrenotazioneController.PrenotazioneInfo prenotazione) {
        txtRiepilogo.setText(
                "Prenotazione confermata.\n"
                        + "ID prenotazione: " + prenotazione.getId() + "\n"
                        + "Medico: " + prenotazione.getMedico() + "\n"
                        + "Data visita: " + prenotazione.getDataVisita() + "\n"
                        + "Orario: " + prenotazione.getOraInizio() + " - " + prenotazione.getOraFine() + "\n"
                        + "Stato: " + prenotazione.getStato()
        );
    }

    public void mostraMessaggio(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio);
    }

    private void caricaSpecializzazioni() {
        List<MedicoController.SpecializzazioneInfo> specializzazioni = medicoController.getAllSpecializzazioni();
        mostraSpecializzazioni(specializzazioni);

        if (specializzazioni.isEmpty()) {
            mostraMessaggio("Non sono presenti specializzazioni configurate nel sistema.");
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

        List<CalendarioController.FasciaOrariaInfo> fasce = calendarioController.getDisponibilitaPerMedico(
                medico.getId(),
                LocalDate.now(),
                LocalDate.now().plusMonths(2)
        );

        mostraFasceOrarie(fasce);

        if (fasce.isEmpty()) {
            mostraMessaggio("Non ci sono fasce orarie disponibili per il medico selezionato.");
        }
    }

    private void confermaPrenotazione() {
        if (idPazienteAutenticato == null) {
            mostraMessaggio("Impossibile procedere: paziente non autenticato.");
            return;
        }

        CalendarioController.FasciaOrariaInfo fascia = leggiFasciaSelezionata();

        if (fascia == null) {
            mostraMessaggio("Seleziona una fascia oraria prima di confermare la prenotazione.");
            return;
        }

        boolean disponibile = calendarioController.verificaDisponibilitaFascia(fascia.getId());

        if (!disponibile) {
            mostraMessaggio("La fascia oraria selezionata non è più disponibile. Scegli una nuova fascia.");
            caricaFasceDisponibili();
            return;
        }

        PrenotazioneController.PrenotazioneInfo prenotazione = prenotazioneController.effettuaPrenotazioneDettagli(
                idPazienteAutenticato,
                fascia.getId()
        );

        if (prenotazione == null) {
            mostraMessaggio("Prenotazione non completata. Verifica paziente e fascia oraria selezionata.");
            caricaFasceDisponibili();
            return;
        }

        mostraRiepilogo(prenotazione);
        mostraMessaggio("Prenotazione registrata correttamente. È stata inviata una notifica di conferma.");
        caricaFasceDisponibili();
    }
}
