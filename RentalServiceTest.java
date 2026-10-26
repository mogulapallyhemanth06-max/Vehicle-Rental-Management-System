package com.rental;

import com.rental.model.Customer;
import com.rental.model.Rental;
import com.rental.model.Vehicle;
import com.rental.service.RentalService;

import java.math.BigDecimal;

/**
 * Dependency-free smoke tests for RentalService.
 * Run with test.sh. Exits non-zero on the first failure.
 */
public class RentalServiceTest {
    private static int passed = 0;

    private static void check(boolean condition, String name) {
        if (!condition) {
            throw new AssertionError("FAILED: " + name);
        }
        passed++;
        System.out.println("PASS: " + name);
    }

    public static void main(String[] args) {
        RentalService service = new RentalService();
        service.addVehicle(new Vehicle("V001", "Maruti", "Swift", "Car", 2022,
                new BigDecimal("1500"), true));
        service.addVehicle(new Vehicle("V002", "Honda", "Activa", "Bike", 2021,
                new BigDecimal("500"), true));
        service.addCustomer(new Customer("C001", "Aarav Sharma", "9876543210", "DL123"));

        // Cost = daily rate x days
        Rental rental = service.rentVehicle("V001", "C001", 3);
        check(rental.getTotalCost().equals(new BigDecimal("4500")), "rent cost 1500 x 3 = 4500");
        check(!service.findVehicle("V001").orElseThrow().isAvailable(), "rented vehicle unavailable");
        check(service.getAvailableVehicles().size() == 1, "one vehicle still available");

        // Double-booking must fail
        try {
            service.rentVehicle("V001", "C001", 1);
            check(false, "double booking rejected");
        } catch (IllegalStateException expected) {
            check(true, "double booking rejected");
        }

        // Return frees the vehicle, and it can be rented again
        service.returnVehicle(rental.getId());
        check(service.findVehicle("V001").orElseThrow().isAvailable(), "returned vehicle available again");
        Rental again = service.rentVehicle("V001", "C001", 2);
        check(again.getTotalCost().equals(new BigDecimal("3000")), "re-rent cost 1500 x 2 = 3000");

        // Search
        check(service.searchVehicles("swift").size() == 1, "search by model finds Swift");
        check(service.searchVehicles("Bike").size() == 1, "search by type finds Bike");

        // Duplicate ids rejected
        try {
            service.addVehicle(new Vehicle("V001", "X", "Y", "Car", 2020,
                    new BigDecimal("100"), true));
            check(false, "duplicate vehicle id rejected");
        } catch (IllegalArgumentException expected) {
            check(true, "duplicate vehicle id rejected");
        }

        // Cannot remove a rented vehicle
        check(!service.removeVehicle("V001"), "rented vehicle cannot be removed");
        check(service.removeVehicle("V002"), "available vehicle can be removed");

        // Unknown ids rejected
        try {
            service.rentVehicle("V999", "C001", 1);
            check(false, "unknown vehicle rejected");
        } catch (IllegalArgumentException expected) {
            check(true, "unknown vehicle rejected");
        }

        System.out.println("All " + passed + " tests passed.");
    }
}
