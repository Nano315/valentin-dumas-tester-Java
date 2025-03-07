package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
// Important : on importe lenient() :
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

/**
 * Exemples de tests pour ParkingService avec lenient().
 */
@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    @Mock
    private InputReaderUtil inputReaderUtil;

    @Mock
    private ParkingSpotDAO parkingSpotDAO;

    @Mock
    private TicketDAO ticketDAO;

    private ParkingService parkingService;
    private Ticket defaultTicket;

    @BeforeEach
    public void setUpPerTest() throws Exception {
        // Stubbing "lenient" => évite UnnecessaryStubbingException
        lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

        // Ticket par défaut (1h de parking)
        ParkingSpot defaultParkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        defaultTicket = new Ticket();
        defaultTicket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        defaultTicket.setParkingSpot(defaultParkingSpot);
        defaultTicket.setVehicleRegNumber("ABCDEF");

        // Stubs par défaut
        lenient().when(ticketDAO.getTicket(anyString())).thenReturn(defaultTicket);
        lenient().when(ticketDAO.updateTicket(any())).thenReturn(true);
        lenient().when(ticketDAO.getNbTicket(anyString())).thenReturn(1);

        lenient().when(parkingSpotDAO.updateParking(any())).thenReturn(true);

        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    }

    @Test
    public void processExitingVehicleTest() {
        // On simule un client récurrent
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(2);

        parkingService.processExitingVehicle();

        // Vérifie qu'on appelle bien updateParking() une fois
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));

        // Vérifie la remise appliquée
        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketDAO).updateTicket(ticketCaptor.capture());
        Ticket updatedTicket = ticketCaptor.getValue();

        // Tarif voiture = 1.5 / heure => remise 5% => 1.425
        assertEquals(1.425, updatedTicket.getPrice(), 0.001);
    }

    @Test
    public void testProcessIncomingVehicle() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1); // 1 => CAR
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        parkingService.processIncomingVehicle();

        // Vérifie l'enregistrement du ticket
        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
        // Vérifie la mise à jour de la place (indisponible)
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void processExitingVehicleTestUnableUpdate() {
        when(ticketDAO.updateTicket(any())).thenReturn(false);

        parkingService.processExitingVehicle();

        // On ne met pas à jour la place si le ticketDAO.updateTicket(...) échoue
        verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void testGetNextParkingNumberIfAvailable() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1); // Car
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(ParkingType.CAR, result.getParkingType());
        assertTrue(result.isAvailable());
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1); // Car
        // Pas de place => 0 => lève Exception => catch => return null
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(0);

        ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();
        assertNull(result);
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() throws Exception {
        // Saisie invalide => 3
        when(inputReaderUtil.readSelection()).thenReturn(3);

        ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();
        assertNull(result);
    }
}
