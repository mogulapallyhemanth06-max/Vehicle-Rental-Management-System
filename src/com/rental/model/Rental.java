package com.rental.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/** One rental booking: a vehicle rented by a customer for a number of days. */
public class Rental {
    public enum Status { ACTIVE, RETURNED }

    private final String id;
    private final String vehicleId;
    private final String customerId;
    private final int days;
    private final BigDecimal totalCost;
    private final LocalDate rentDate;
    private Status status;

    public Rental(String id, String vehicleId, String customerId, int days,
                  BigDecimal totalCost, LocalDate rentDate, Status status) {
        this.id = Objects.requireNonNull(id, "id");
        this.vehicleId = Objects.requireNonNull(vehicleId, "vehicleId");
        this.customerId = Objects.requireNonNull(customerId, "customerId");
        if (days <= 0) {
            throw new IllegalArgumentException("days must be positive");
        }
        this.days = days;
        this.totalCost = Objects.requireNonNull(totalCost, "totalCost");
        this.rentDate = Objects.requireNonNull(rentDate, "rentDate");
        this.status = Objects.requireNonNull(status, "status");
    }

    public String getId() { return id; }
    public String getVehicleId() { return vehicleId; }
    public String getCustomerId() { return customerId; }
    public int getDays() { return days; }
    public BigDecimal getTotalCost() { return totalCost; }
    public LocalDate getRentDate() { return rentDate; }
    public Status getStatus() { return status; }
    public boolean isActive() { return status == Status.ACTIVE; }

    public void setStatus(Status status) { this.status = Objects.requireNonNull(status); }

    @Override
    public String toString() {
        return id + " - Vehicle " + vehicleId + " to Customer " + customerId
                + " for " + days + " day(s), Rs." + totalCost + ", " + status;
    }
}
