package com.epam.spring.services;

import com.epam.spring.domain.Auditorium;

import java.util.*;

public class AuditoriumServiceImpl implements AuditoriumService {
    private Properties properties;
    private Map<String, Auditorium> auditoriums;
    private List<Auditorium> auditoriumsList;

    public AuditoriumServiceImpl(Properties properties) {
        this.properties = properties;
    }

    private void init() {
        Set<String> prefixes = new HashSet<>();
        for (String key: properties.stringPropertyNames()) {
            prefixes.add(key.substring(0, key.indexOf('.')));
        }

        auditoriums = new HashMap<>(prefixes.size());
        auditoriumsList = new ArrayList<>(prefixes.size());
        for (String prefix: prefixes) {
            String name = properties.getProperty(prefix + ".name");
            String seatsNumber = properties.getProperty(prefix + ".seatsNumber");
            String vipSeats = properties.getProperty(prefix + ".vipSeats");
            Auditorium auditorium = createAuditorium(name, seatsNumber, vipSeats);
            auditoriums.put(name, auditorium);
            auditoriumsList.add(auditorium);
        }
        Collections.sort(auditoriumsList);
    }

    private static Auditorium createAuditorium(String name, String seatsNumber, String vipSeats) {
        if (name == null || seatsNumber == null) {
            throw new IllegalArgumentException("Check auditorium properties files: all auditoriums must contain name and seats number.");
        }
        int seatsNumberNumeric = Integer.parseInt(seatsNumber);
        Set<Integer> vipSeatsSet;
        if (vipSeats != null) {
            String[] vipSeatsSplitted = vipSeats.split(",");
            vipSeatsSet = new HashSet<>(vipSeatsSplitted.length);
            for (String vipSeat: vipSeatsSplitted) {
                vipSeatsSet.add(Integer.parseInt(vipSeat));
            }
        } else {
            vipSeatsSet = Collections.emptySet();
        }
        return new Auditorium(name, seatsNumberNumeric, vipSeatsSet);
    }

    @Override
    public List<Auditorium> getAll() {
        return auditoriumsList;
    }

    @Override
    public Auditorium getByName(String name) {
        return auditoriums.get(name);
    }
}
