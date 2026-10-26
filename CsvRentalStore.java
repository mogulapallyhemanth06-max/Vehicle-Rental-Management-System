package com.rental.persistence;

import com.rental.model.Customer;
import com.rental.model.Rental;
import com.rental.model.Vehicle;
import com.rental.service.RentalService;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Very small CSV persistence (no external libraries).
 * Files live under data/: vehicles.csv, customers.csv, rentals.csv.
 * Commas inside text fields are replaced with spaces on save, so a
 * plain split(",") is safe on load.
 */
public class CsvRentalStore {
    private final Path dir;

    public CsvRentalStore(Path dir) {
        this.dir = dir;
    }

    public void save(RentalService service) throws IOException {
        Files.createDirectories(dir);
        StringBuilder v = new StringBuilder();
        for (Vehicle vehicle : service.getVehicles()) {
            v.append(vehicle.getId()).append(',')
                    .append(clean(vehicle.getBrand())).append(',')
                    .append(clean(vehicle.getModel())).append(',')
                    .append(clean(vehicle.getType())).append(',')
                    .append(vehicle.getYear()).append(',')
                    .append(vehicle.getDailyRate()).append(',')
                    .append(vehicle.isAvailable()).append('\n');
        }
        Files.writeString(dir.resolve("vehicles.csv"), v.toString());

        StringBuilder c = new StringBuilder();
        for (Customer customer : service.getCustomers()) {
            c.append(customer.getId()).append(',')
                    .append(clean(customer.getName())).append(',')
                    .append(clean(customer.getPhone())).append(',')
                    .append(clean(customer.getLicenceNo())).append('\n');
        }
        Files.writeString(dir.resolve("customers.csv"), c.toString());

        StringBuilder r = new StringBuilder();
        for (Rental rental : service.getRentals()) {
            r.append(rental.getId()).append(',')
                    .append(rental.getVehicleId()).append(',')
                    .append(rental.getCustomerId()).append(',')
                    .append(rental.getDays()).append(',')
                    .append(rental.getTotalCost()).append(',')
                    .append(rental.getRentDate()).append(',')
                    .append(rental.getStatus()).append('\n');
        }
        Files.writeString(dir.resolve("rentals.csv"), r.toString());
    }

    /** Loads stored data into an empty service. Returns false if no data files exist. */
    public boolean load(RentalService service) throws IOException {
        Path vehiclesFile = dir.resolve("vehicles.csv");
        if (!Files.exists(vehiclesFile)) {
            return false;
        }
        for (String line : Files.readAllLines(vehiclesFile)) {
            if (line.isBlank()) {
                continue;
            }
            String[] p = line.split(",", -1);
            service.addVehicle(new Vehicle(p[0], p[1], p[2], p[3],
                    Integer.parseInt(p[4]), new BigDecimal(p[5]), Boolean.parseBoolean(p[6])));
        }
        Path customersFile = dir.resolve("customers.csv");
        if (Files.exists(customersFile)) {
            for (String line : Files.readAllLines(customersFile)) {
                if (line.isBlank()) {
                    continue;
                }
                String[] p = line.split(",", -1);
                service.addCustomer(new Customer(p[0], p[1], p[2], p[3]));
            }
        }
        Path rentalsFile = dir.resolve("rentals.csv");
        if (Files.exists(rentalsFile)) {
            for (String line : Files.readAllLines(rentalsFile)) {
                if (line.isBlank()) {
                    continue;
                }
                String[] p = line.split(",", -1);
                service.registerRental(new Rental(p[0], p[1], p[2], Integer.parseInt(p[3]),
                        new BigDecimal(p[4]), LocalDate.parse(p[5]),
                        Rental.Status.valueOf(p[6])));
            }
        }
        return true;
    }

    public List<Path> files() {
        return List.of(dir.resolve("vehicles.csv"), dir.resolve("customers.csv"), dir.resolve("rentals.csv"));
    }

    private static String clean(String s) {
        return s.replace(',', ' ').replace('\n', ' ');
    }
}
