package com.epam.spring.domain;

import java.util.Set;

public class Auditorium implements Comparable<Auditorium> {
    private final String name;
    private final int seatsNumber;
    private final Set<Integer> vipSeats;

    public Auditorium(String name, int seatsNumber, Set<Integer> vipSeats) {
        this.name = name;
        this.seatsNumber = seatsNumber;
        this.vipSeats = vipSeats;
    }

    public String getName() {
        return name;
    }

    public int getSeatsNumber() {
        return seatsNumber;
    }

    public Set<Integer> getVipSeats() {
        return vipSeats;
    }

    @Override
    public int compareTo(Auditorium o) {
        return this.name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return "Auditorium{" +
                "name='" + name + '\'' +
                ", seatsNumber=" + seatsNumber +
                ", vipSeats=" + vipSeats +
                '}';
    }
}
