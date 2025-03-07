package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket) {

        if (ticket.getOutTime() == null || ticket.getOutTime().before(ticket.getInTime())) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime());
        }

        // Récupère les millisecondes depuis le 1er janvier 1970
        long inTimeMillis = ticket.getInTime().getTime();
        long outTimeMillis = ticket.getOutTime().getTime();

        // Durée en heures : on divise par (1000 ms * 60 sec * 60 min)
        double durationInHours = (double) (outTimeMillis - inTimeMillis) / (1000 * 60 * 60);

        // S’il y a eu une erreur et que c’est négatif, on peut lever une exception
        if (durationInHours < 0) {
            throw new IllegalArgumentException("Out time is earlier than in time.");
        }

        // On applique le tarif en fonction du type de véhicule
        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice(durationInHours * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(durationInHours * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown Parking Type");
        }
    }
}