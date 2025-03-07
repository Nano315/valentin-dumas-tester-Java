package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket) {

        if (ticket.getOutTime() == null || ticket.getOutTime().before(ticket.getInTime())) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime());
        }

        long inTimeMillis = ticket.getInTime().getTime();
        long outTimeMillis = ticket.getOutTime().getTime();

        double durationInHours = (double) (outTimeMillis - inTimeMillis) / (1000 * 60 * 60);

        if (durationInHours < 0) {
            throw new IllegalArgumentException("Out time is earlier than in time.");
        }

        // Implémentation de la gratuité pour moins de 30 minutes
        if (durationInHours < 0.5) {
            ticket.setPrice(0);
            return; // on arrête la méthode, c'est gratuit
        }

        // Sinon, on applique le tarif normal
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