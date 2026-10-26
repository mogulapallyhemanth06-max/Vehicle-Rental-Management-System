package com.rental.model;

import java.util.Objects;

/** A customer who rents vehicles. */
public class Customer {
    private final String id;
    private String name;
    private String phone;
    private String licenceNo;

    public Customer(String id, String name, String phone, String licenceNo) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.phone = Objects.requireNonNull(phone, "phone");
        this.licenceNo = Objects.requireNonNull(licenceNo, "licenceNo");
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getLicenceNo() { return licenceNo; }

    public void setName(String name) { this.name = Objects.requireNonNull(name); }
    public void setPhone(String phone) { this.phone = Objects.requireNonNull(phone); }
    public void setLicenceNo(String licenceNo) { this.licenceNo = Objects.requireNonNull(licenceNo); }

    @Override
    public String toString() {
        return id + " - " + name + " (" + phone + ", Licence: " + licenceNo + ")";
    }
}
