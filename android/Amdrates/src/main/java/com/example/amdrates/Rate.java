package com.example.amdrates;

public class Rate {
    public double sell;
    public double buy;

    public Rate(double sell, double buy) {
        this.sell = sell;
        this.buy = buy;
    }

    public Rate() {
        this(0, 0);
    }
}
