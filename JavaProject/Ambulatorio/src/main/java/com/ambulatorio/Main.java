package com.ambulatorio;

import com.ambulatorio.boundary.MainPage;

import javax.swing.*;

public class Main {
    static void main() {
        JFrame frame = new JFrame();
        frame.setTitle("Ambulatorio");
        frame.setContentPane(new MainPage().contentPane);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        frame.setSize(900, 900);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }
}
