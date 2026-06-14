package com.ambulatorio.boundary;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginView {
    public JPanel contentPane;
    private JTextField textField1;
    private JPasswordField passwordField1;
    private JButton annullaButton;
    private JButton accediButton;

    public LoginView() {
        annullaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                apriMainPage();
            }
        });
        accediButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }

    private void apriMainPage() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(contentPane);
        MainPage mainPage = new MainPage();
        frame.setContentPane(mainPage.contentPane);

        frame.revalidate();
        frame.repaint();
    }
}
