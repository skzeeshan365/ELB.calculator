package com.elbcalculator;

public class Db_data {

    private String energy_charges, fixed_charges, wheeling_charges, fac_charges;

    public Db_data() {

    }

    public Db_data(String energy_charges, String fixed_charges, String wheeling_charges, String fac_charges) {
        this.energy_charges = energy_charges;
        this.fixed_charges = fixed_charges;
        this.wheeling_charges = wheeling_charges;
        this.fac_charges = fac_charges;
    }

    public String getEnergy_charges() {
        return energy_charges;
    }

    public void setEnergy_charges(String energy_charges) {
        this.energy_charges = energy_charges;
    }

    public String getFixed_charges() {
        return fixed_charges;
    }

    public void setFixed_charges(String fixed_charges) {
        this.fixed_charges = fixed_charges;
    }

    public String getWheeling_charges() {
        return wheeling_charges;
    }

    public void setWheeling_charges(String wheeling_charges) {
        this.wheeling_charges = wheeling_charges;
    }

    public String getFac_charges() {
        return fac_charges;
    }

    public void setFac_charges(String fac_charges) {
        this.fac_charges = fac_charges;
    }
}
