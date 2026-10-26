package com.rental;

import com.rental.ui.RentalApp;

import javax.swing.SwingUtilities;

/** Launches the Vehicle Rental Management System desktop app. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> RentalApp.create().setVisible(true));
    }
}
