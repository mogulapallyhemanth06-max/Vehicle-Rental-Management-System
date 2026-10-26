package com.rental.ui;

import com.rental.model.Customer;
import com.rental.model.Rental;
import com.rental.model.Vehicle;
import com.rental.persistence.CsvRentalStore;
import com.rental.service.RentalService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;

/** Swing desktop UI: Vehicles, Customers and Rentals tabs over RentalService. */
public class RentalApp extends JFrame {
    private final RentalService service;
    private final CsvRentalStore store;
    private final DefaultTableModel vehicleModel;
    private final DefaultTableModel customerModel;
    private final DefaultTableModel rentalModel;
    private final JLabel revenueLabel;

    public RentalApp(RentalService service, CsvRentalStore store) {
        super("Vehicle Rental Management System");
        this.service = service;
        this.store = store;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 550);
        setLocationRelativeTo(null);

        vehicleModel = new DefaultTableModel(
                new String[]{"ID", "Brand", "Model", "Type", "Year", "Daily Rate (Rs.)", "Status"}, 0);
        customerModel = new DefaultTableModel(
                new String[]{"ID", "Name", "Phone", "Licence No."}, 0);
        rentalModel = new DefaultTableModel(
                new String[]{"ID", "Vehicle", "Customer", "Days", "Total (Rs.)", "Date", "Status"}, 0);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Vehicles", vehiclesPanel());
        tabs.addTab("Customers", customersPanel());
        tabs.addTab("Rentals", rentalsPanel());
        add(tabs, BorderLayout.CENTER);

        revenueLabel = new JLabel();
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save Data");
        saveButton.addActionListener(e -> save());
        footer.add(revenueLabel);
        footer.add(saveButton);
        add(footer, BorderLayout.SOUTH);

        refreshAll();
    }

    private JPanel vehiclesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(new JTable(vehicleModel)), BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        JButton add = new JButton("Add Vehicle");
        add.addActionListener(e -> addVehicle());
        JButton remove = new JButton("Remove Selected");
        remove.addActionListener(e -> removeVehicle());
        buttons.add(add);
        buttons.add(remove);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel customersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(new JTable(customerModel)), BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        JButton add = new JButton("Add Customer");
        add.addActionListener(e -> addCustomer());
        buttons.add(add);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel rentalsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(new JTable(rentalModel)), BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        JButton rent = new JButton("Rent Vehicle");
        rent.addActionListener(e -> rentVehicle());
        JButton ret = new JButton("Return Selected Rental");
        ret.addActionListener(e -> returnRental());
        buttons.add(rent);
        buttons.add(ret);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private void addVehicle() {
        try {
            String id = prompt("Vehicle ID (e.g. V007):");
            if (id == null) return;
            String brand = prompt("Brand:");
            String model = prompt("Model:");
            String type = prompt("Type (Car / Bike / SUV / Van):");
            int year = Integer.parseInt(prompt("Year:"));
            BigDecimal rate = new BigDecimal(prompt("Daily rate (Rs.):"));
            service.addVehicle(new Vehicle(id.trim(), brand.trim(), model.trim(),
                    type.trim(), year, rate, true));
            saveQuietly();
            refreshAll();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void removeVehicle() {
        // Removal is by id typed in, so it works even without a row selection.
        String id = prompt("Vehicle ID to remove (only if available):");
        if (id == null) return;
        if (service.removeVehicle(id.trim())) {
            saveQuietly();
            refreshAll();
        } else {
            showError("Cannot remove: no such vehicle, or it is currently rented.");
        }
    }

    private void addCustomer() {
        try {
            String id = prompt("Customer ID (e.g. C004):");
            if (id == null) return;
            String name = prompt("Name:");
            String phone = prompt("Phone:");
            String licence = prompt("Driving licence no.:");
            service.addCustomer(new Customer(id.trim(), name.trim(), phone.trim(), licence.trim()));
            saveQuietly();
            refreshAll();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void rentVehicle() {
        try {
            String vehicleId = prompt("Vehicle ID to rent:");
            if (vehicleId == null) return;
            String customerId = prompt("Customer ID:");
            int days = Integer.parseInt(prompt("Number of days:"));
            Rental rental = service.rentVehicle(vehicleId.trim(), customerId.trim(), days);
            saveQuietly();
            refreshAll();
            JOptionPane.showMessageDialog(this,
                    "Booked! " + rental + "\nTotal: Rs." + rental.getTotalCost());
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void returnRental() {
        String id = prompt("Rental ID to return (e.g. R001):");
        if (id == null) return;
        try {
            Rental rental = service.returnVehicle(id.trim());
            saveQuietly();
            refreshAll();
            JOptionPane.showMessageDialog(this, "Returned: " + rental);
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void save() {
        try {
            store.save(service);
            JOptionPane.showMessageDialog(this, "Saved to data/ (vehicles, customers, rentals CSV).");
        } catch (IOException ex) {
            showError("Save failed: " + ex.getMessage());
        }
    }

    private void saveQuietly() {
        try {
            store.save(service);
        } catch (IOException ignored) {
            // UI keeps working in memory; the user can retry with Save Data.
        }
    }

    private void refreshAll() {
        vehicleModel.setRowCount(0);
        for (Vehicle v : service.getVehicles()) {
            vehicleModel.addRow(new Object[]{v.getId(), v.getBrand(), v.getModel(), v.getType(),
                    v.getYear(), v.getDailyRate(), v.isAvailable() ? "Available" : "Rented"});
        }
        customerModel.setRowCount(0);
        for (Customer c : service.getCustomers()) {
            customerModel.addRow(new Object[]{c.getId(), c.getName(), c.getPhone(), c.getLicenceNo()});
        }
        rentalModel.setRowCount(0);
        for (Rental r : service.getRentals()) {
            rentalModel.addRow(new Object[]{r.getId(), r.getVehicleId(), r.getCustomerId(),
                    r.getDays(), r.getTotalCost(), r.getRentDate(), r.getStatus()});
        }
        revenueLabel.setText("Total booked: Rs." + service.totalRevenue()
                + "  |  Available: " + service.getAvailableVehicles().size()
                + " / " + service.getVehicles().size() + " vehicles");
    }

    private String prompt(String message) {
        return JOptionPane.showInputDialog(this, message);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /** Entry used by Main: load saved data, or seed sample data on first run. */
    public static RentalApp create() {
        RentalService service = new RentalService();
        CsvRentalStore store = new CsvRentalStore(Path.of("data"));
        try {
            if (!store.load(service)) {
                service.seedSampleData();
                store.save(service);
            }
        } catch (Exception ex) {
            service.seedSampleData();
        }
        return new RentalApp(service, store);
    }
}
