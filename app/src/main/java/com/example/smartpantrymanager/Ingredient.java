package com.example.smartpantrymanager;

/** A single item in the user's pantry. */
public class Ingredient {
    private long id;
    private String name;
    private double quantity;
    private String unit;        // e.g. g, ml, pcs, tsp, tbsp, cup
    private String expiryDate;  // yyyy-MM-dd, may be null/empty

    public Ingredient(long id, String name, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getExpiryDate() { return expiryDate; }

    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
}