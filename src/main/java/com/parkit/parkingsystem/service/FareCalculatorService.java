package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket) {
        // Méthode par défaut (sans réduction)
        calculateFare(ticket, false);
    }

    public void calculateFare(Ticket ticket, boolean discount) {
        if (ticket.getOutTime() == null || ticket.getOutTime().before(ticket.getInTime())) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime());
        }

        long inTimeMillis = ticket.getInTime().getTime();
        long outTimeMillis = ticket.getOutTime().getTime();

        double durationInHours = (double) (outTimeMillis - inTimeMillis) / (1000 * 60 * 60);

        if (durationInHours < 0) {
            throw new IllegalArgumentException("Out time is earlier than in time.");
        }

        // Gratuit si moins de 30 minutes
        if (durationInHours < 0.5) {
            ticket.setPrice(0);
            return;
        }

        // Tarif plein (déjà mis en place précédemment)
        double price;
        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                price = durationInHours * Fare.CAR_RATE_PER_HOUR;
                break;
            }
            case BIKE: {
                price = durationInHours * Fare.BIKE_RATE_PER_HOUR;
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown Parking Type");
        }

        // Si discount est true, on applique 5% de remise
        if (discount) {
            price = price * 0.95;
        }

        // On met à jour le prix final
        ticket.setPrice(price);
    }
}