package ticket.booking.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ticket.booking.entities.Ticket;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.service.TrainService; // Added import
import ticket.booking.util.UserServiceUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class UserBookingService{
    
    private ObjectMapper objectMapper = new ObjectMapper();

    private List<User> userList;

    private User user;

    private final String USER_FILE_PATH = "app/src/main/java/ticket/booking/localDb/users.json";

    public UserBookingService(User user) throws IOException {
        this.user = user;
        loadUserListFromFile();
    }

    public UserBookingService() throws IOException {
        loadUserListFromFile();
    }

    private void loadUserListFromFile() throws IOException {
        userList = objectMapper.readValue(new File(USER_FILE_PATH), new TypeReference<List<User>>() {});
    }

    public Boolean loginUser(){
        Optional<User> foundUser = userList.stream().filter(user1 -> {
            return user1.getName().equals(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
        }).findFirst();
        return foundUser.isPresent();
    }

    public Boolean signUp(User user1){
        try{
            userList.add(user1);
            saveUserListToFile();
            return Boolean.TRUE;
        }catch (IOException ex){
            return Boolean.FALSE;
        }
    }

    private void saveUserListToFile() throws IOException {
        File usersFile = new File(USER_FILE_PATH);
        objectMapper.writeValue(usersFile, userList);
    }

    public void fetchBookings(){
        Optional<User> userFetched = userList.stream().filter(user1 -> {
            return user1.getName().equals(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
        }).findFirst();
        if(userFetched.isPresent()){
            userFetched.get().printTickets();
        }
    }

public Boolean cancelBooking(String ticketId) {
    if (ticketId == null || ticketId.isEmpty()) {
        System.out.println("Ticket ID cannot be null or empty.");
        return Boolean.FALSE;
    }

    if (this.user == null || this.user.getUserId() == null) {
        System.out.println("User not logged in or user session is invalid. Cannot cancel booking.");
        return Boolean.FALSE;
    }

    Optional<User> currentUserOptional = userList.stream()
            .filter(u -> u.getUserId() != null && u.getUserId().equals(this.user.getUserId()))
            .findFirst();

    if (!currentUserOptional.isPresent()) {
        System.out.println("User session error or user not found in list. Please login again.");
        return Boolean.FALSE;
    }
    User userInList = currentUserOptional.get();

    List<Ticket> tickets = userInList.getTicketsBooked();
    if (tickets == null) { // Should ideally be initialized to an empty list
        tickets = new ArrayList<>();
    }

    // Make a mutable copy for removal to avoid ConcurrentModificationException if tickets is an unmodifiable list
    List<Ticket> mutableTickets = new ArrayList<>(tickets);
    Optional<Ticket> ticketToCancelOptional = mutableTickets.stream()
            .filter(ticket -> ticket.getTicketId().equals(ticketId))
            .findFirst();

    if (!ticketToCancelOptional.isPresent()) {
        System.out.println("No ticket found with ID " + ticketId + " for the current user.");
        return Boolean.FALSE;
    }

    Ticket ticketToCancel = ticketToCancelOptional.get();
    Train trainOfCancelledTicket = ticketToCancel.getTrain();
    int row = ticketToCancel.getRow(); // Use actual getter
    int col = ticketToCancel.getCol(); // Use actual getter

    boolean removed = mutableTickets.removeIf(ticket -> ticket.getTicketId().equals(ticketId));

    if (removed) {
        userInList.setTicketsBooked(mutableTickets);
        try {
            saveUserListToFile(); // Saves user file
            System.out.println("Ticket with ID " + ticketId + " has been canceled by user."); // User-facing message

            // Now, attempt to make the seat available
            TrainService trainService = null;
            try {
                trainService = new TrainService();
            } catch (IOException e_ts) {
                System.out.println("ERROR: Could not initialize TrainService to update seat availability: " + e_ts.getMessage());
            }

            if (trainOfCancelledTicket != null && trainService != null) {
                String trainIdStr = trainOfCancelledTicket.getTrainId();
                // int row and col are already defined from ticketToCancel.getRow/Col()
                boolean seatMadeAvailable = trainService.makeSeatAvailable(trainIdStr, row, col);
                if (seatMadeAvailable) {
                    System.out.println("INFO: Seat [R" + row + ", C" + col + "] for train " + trainIdStr + " status updated.");
                } else {
                    System.out.println("WARN: Could not make seat [R" + row + ", C" + col + "] for train " + trainIdStr + " available via TrainService. Check logs.");
                }
            } else if (trainOfCancelledTicket == null) {
                System.out.println("WARN: Could not retrieve train information for the cancelled ticket. Seat status not updated in train database.");
            } else { // trainService is null
                 System.out.println("WARN: TrainService not available. Seat status not updated in train database.");
            }

            if (this.user != null) {
                this.user.setTicketsBooked(new ArrayList<>(mutableTickets));
            }
            return Boolean.TRUE;
        } catch (IOException e_user) { // Catch for saveUserListToFile()
            System.out.println("Error saving user data after cancellation: " + e_user.getMessage());
            return Boolean.FALSE;
        }
    } else {
        // This case should technically not be reached if ticketToCancelOptional was present,
        // but kept for logical completeness.
        System.out.println("Error removing ticket with ID " + ticketId);
        return Boolean.FALSE;
    }
}
        

    public List<Train> getTrains(String source, String destination){
        try{
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source, destination);
        }catch(IOException ex){
            return new ArrayList<>();
        }
    }

    public List<List<Integer>> fetchSeats(Train train){
            return train.getSeats();
    }

    public Boolean bookTrainSeat(Train train, int row, int seat) {
        try{
            TrainService trainService = new TrainService();
            List<List<Integer>> seats = train.getSeats();
            if (row >= 0 && row < seats.size() && seat >= 0 && seat < seats.get(row).size()) {
                if (seats.get(row).get(seat) == 0) {
                    seats.get(row).set(seat, 1); // Mark as booked
                    train.setSeats(seats);
                    trainService.addTrain(train); // Persist change to train availability

                    // --- Start: New logic for ticket creation and saving ---
                    if (this.user == null || this.user.getUserId() == null) {
                        System.out.println("User not logged in. Cannot assign ticket.");
                        // Consider rolling back seat reservation here if critical
                        // seats.get(row).set(seat, 0);
                        // trainService.addTrain(train);
                        return false;
                    }

                    String ticketId = UUID.randomUUID().toString();
                    String userId = this.user.getUserId();

                    // Placeholder for source and destination from train stations list
                    List<String> stations = train.getStations();
                    String sourceStation = (stations == null || stations.isEmpty()) ? "Unknown Source" : stations.get(0);
                    String destinationStation = (stations == null || stations.isEmpty() || stations.size() < 1) ? "Unknown Destination" : stations.get(stations.size() - 1);

                    String dateOfTravel = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
                    // 'train' is the trainObject
                    // 'row' is the ticketRow
                    // 'seat' is the ticketCol

                    Ticket newTicket = new Ticket(ticketId, userId, sourceStation, destinationStation, dateOfTravel, train, row, seat);

                    Optional<User> currentUserOptional = userList.stream()
                            .filter(u -> u.getUserId() != null && u.getUserId().equals(this.user.getUserId()))
                            .findFirst();

                    if (!currentUserOptional.isPresent()) {
                        System.out.println("User session invalid or user not found in list. Cannot save ticket.");
                        // Roll back seat reservation
                         seats.get(row).set(seat, 0); // Unmark seat
                         trainService.addTrain(train); // Persist rollback
                        return false;
                    }
                    User userInList = currentUserOptional.get();

                    List<Ticket> ticketsBooked = userInList.getTicketsBooked();
                    if (ticketsBooked == null) {
                        ticketsBooked = new ArrayList<>();
                    }
                    ticketsBooked.add(newTicket);
                    userInList.setTicketsBooked(ticketsBooked);

                    // Update the current session user's tickets as well
                    if (this.user != null) {
                        this.user.setTicketsBooked(new ArrayList<>(ticketsBooked));
                    }

                    saveUserListToFile(); // Save updated user list with the new ticket

                    System.out.println("Ticket booked successfully with ID: " + newTicket.getTicketId() + " for seat R" + row + "C" + seat);
                    return true; // Booking successful
                    // --- End: New logic for ticket creation and saving ---
                } else {
                    System.out.println("Seat is already booked.");
                    return false; // Seat is already booked
                }
            } else {
                return false; // Invalid row or seat index
            }
        }catch (IOException ex){
            return Boolean.FALSE;
        }
    }
}
