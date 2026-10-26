package com.rental.service;

import com.rental.model.Customer;
import com.rental.model.Rental;
import com.rental.model.Vehicle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Business logic for the rental system, kept separate from the Swing UI
 * so it can be tested without a display.
 *
 * Rules enforced here:
 * - Vehicle and customer ids must be unique.
 * - A vehicle can only be rented when it is available.
 * - Rental cost = daily rate x number of days (exact BigDecimal maths).
 * - Returning a rental frees the vehicle again.
 */
public class RentalService {
    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<Customer> customers = new ArrayList<>();
    private final List<Rental> rentals = new ArrayList<>();
    private int rentalSeq = 0;

    // ---- vehicles ----

    public void addVehicle(Vehicle vehicle) {
        if (findVehicle(vehicle.getId()).isPresent()) {
            throw new IllegalArgumentException("Vehicle id already exists: " + vehicle.getId());
        }
        vehicles.add(vehicle);
    }

    public boolean removeVehicle(String vehicleId) {
        Optional<Vehicle> vehicle = findVehicle(vehicleId);
        if (vehicle.isEmpty() || !vehicle.get().isAvailable()) {
            return false; // never delete a vehicle that is out on rent
        }
        return vehicles.remove(vehicle.get());
    }

    public Optional<Vehicle> findVehicle(String id) {
        return vehicles.stream().filter(v -> v.getId().equals(id)).findFirst();
    }

    public List<Vehicle> getVehicles() {
        return new ArrayList<>(vehicles);
    }

    public List<Vehicle> getAvailableVehicles() {
        List<Vehicle> out = new ArrayList<>();
        for (Vehicle v : vehicles) {
            if (v.isAvailable()) {
                out.add(v);
            }
        }
        return out;
    }

    public List<Vehicle> searchVehicles(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getVehicles();
        }
        String q = keyword.toLowerCase(Locale.ROOT);
        List<Vehicle> out = new ArrayList<>();
        for (Vehicle v : vehicles) {
            if (v.getId().toLowerCase(Locale.ROOT).contains(q)
                    || v.getBrand().toLowerCase(Locale.ROOT).contains(q)
                    || v.getModel().toLowerCase(Locale.ROOT).contains(q)
                    || v.getType().toLowerCase(Locale.ROOT).contains(q)) {
                out.add(v);
            }
        }
        return out;
    }

    // ---- customers ----

    public void addCustomer(Customer customer) {
        if (findCustomer(customer.getId()).isPresent()) {
            throw new IllegalArgumentException("Customer id already exists: " + customer.getId());
        }
        customers.add(customer);
    }

    public Optional<Customer> findCustomer(String id) {
        return customers.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    public List<Customer> getCustomers() {
        return new ArrayList<>(customers);
    }

    // ---- rentals ----

    public Rental rentVehicle(String vehicleId, String customerId, int days) {
        Vehicle vehicle = findVehicle(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("No such vehicle: " + vehicleId));
        findCustomer(customerId)
                .orElseThrow(() -> new IllegalArgumentException("No such customer: " + customerId));
        if (days <= 0) {
            throw new IllegalArgumentException("days must be positive");
        }
        if (!vehicle.isAvailable()) {
            throw new IllegalStateException("Vehicle is already rented: " + vehicleId);
        }
        BigDecimal total = vehicle.getDailyRate().multiply(BigDecimal.valueOf(days));
        Rental rental = new Rental(nextRentalId(), vehicleId, customerId, days,
                total, LocalDate.now(), Rental.Status.ACTIVE);
        rentals.add(rental);
        vehicle.setAvailable(false);
        return rental;
    }

    public Rental returnVehicle(String rentalId) {
        Rental rental = rentals.stream()
                .filter(r -> r.getId().equals(rentalId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such rental: " + rentalId));
        if (!rental.isActive()) {
            throw new IllegalStateException("Rental already returned: " + rentalId);
        }
        rental.setStatus(Rental.Status.RETURNED);
        findVehicle(rental.getVehicleId()).ifPresent(v -> v.setAvailable(true));
        return rental;
    }

    public List<Rental> getRentals() {
        return new ArrayList<>(rentals);
    }

    public List<Rental> getActiveRentals() {
        List<Rental> out = new ArrayList<>();
        for (Rental r : rentals) {
            if (r.isActive()) {
                out.add(r);
            }
        }
        return out;
    }

    /** Sum of all rentals booked so far (active + returned). */
    public BigDecimal totalRevenue() {
        BigDecimal sum = BigDecimal.ZERO;
        for (Rental r : rentals) {
            sum = sum.add(r.getTotalCost());
        }
        return sum;
    }

    /** Used when loading rentals from disk so new ids do not collide. */
    public void registerRental(Rental rental) {
        rentals.add(rental);
        try {
            int n = Integer.parseInt(rental.getId().replaceAll("\\D", ""));
            rentalSeq = Math.max(rentalSeq, n);
        } catch (NumberFormatException ignored) {
            // keep the existing sequence
        }
    }

    private String nextRentalId() {
        return String.format("R%03d", ++rentalSeq);
    }

    /** Sample data so the app is usable on first launch. */
    public void seedSampleData() {
        if (!vehicles.isEmpty() || !customers.isEmpty()) {
            return;
        }
        addVehicle(new Vehicle("V001", "Maruti", "Swift", "Car", 2022, new BigDecimal("1500"), true));
        addVehicle(new Vehicle("V002", "Hyundai", "Creta", "SUV", 2023, new BigDecimal("2800"), true));
        addVehicle(new Vehicle("V003", "Honda", "Activa", "Bike", 2021, new BigDecimal("500"), true));
        addVehicle(new Vehicle("V004", "Toyota", "Innova", "Van", 2022, new BigDecimal("3500"), true));
        addVehicle(new Vehicle("V005", "Royal Enfield", "Classic 350", "Bike", 2023, new BigDecimal("900"), true));
        addVehicle(new Vehicle("V006", "Tata", "Nexon", "SUV", 2024, new BigDecimal("2600"), true));
        addCustomer(new Customer("C001", "Aarav Sharma", "9876543210", "DL1234567890"));
        addCustomer(new Customer("C002", "Priya Verma", "9123456780", "DL0987654321"));
        addCustomer(new Customer("C003", "Rahul Singh", "9988776655", "DL1122334455"));
    }
}
