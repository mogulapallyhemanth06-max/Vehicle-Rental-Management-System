package com.rental.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A vehicle that can be rented out.
 * Availability is managed by RentalService: a vehicle is unavailable
 * while it has an ACTIVE rental, and available again once returned.
 */
public class Vehicle {
    private final String id;
    private String brand;
    private String model;
    private String type; // e.g. Car, Bike, SUV, Van
    private int year;
    private BigDecimal dailyRate;
    private boolean available;

    public Vehicle(String id, String brand, String model, String type,
                   int year, BigDecimal dailyRate, boolean available) {
        this.id = Objects.requireNonNull(id, "id");
        this.brand = Objects.requireNonNull(brand, "brand");
        this.model = Objects.requireNonNull(model, "model");
        this.type = Objects.requireNonNull(type, "type");
        if (year < 1990 || year > 2100) {
            throw new IllegalArgumentException("year out of range: " + year);
        }
        if (dailyRate == null || dailyRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("dailyRate must be positive");
        }
        this.year = year;
        this.dailyRate = dailyRate;
        this.available = available;
    }

    public String getId() { return id; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getType() { return type; }
    public int getYear() { return year; }
    public BigDecimal getDailyRate() { return dailyRate; }
    public boolean isAvailable() { return available; }

    public void setBrand(String brand) { this.brand = Objects.requireNonNull(brand); }
    public void setModel(String model) { this.model = Objects.requireNonNull(model); }
    public void setType(String type) { this.type = Objects.requireNonNull(type); }
    public void setYear(int year) { this.year = year; }
    public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return id + " - " + brand + " " + model + " (" + type + ", " + year
                + ") Rs." + dailyRate + "/day, " + (available ? "Available" : "Rented");
    }
}
