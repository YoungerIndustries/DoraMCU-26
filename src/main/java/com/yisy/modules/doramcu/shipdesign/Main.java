package com.yisy.modules.doramcu.shipdesign;

import javax.swing.*;

/**
 * Main entry point for the FullThrust application.
 * Launches the ShipBuilder UI.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            ShipBuilderUI ui = new ShipBuilderUI();
            ui.setVisible(true);
        });
    }
}
