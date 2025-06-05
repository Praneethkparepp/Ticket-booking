# Java CLI Ticket Booking System

This is a command-line interface (CLI) application for booking train tickets, built using Java and Gradle. It simulates a basic ticket booking platform.

## Features

The system supports the following features:

*   **User Authentication**:
    *   Users can sign up with a username and password.
    *   Registered users can log in to access booking functionalities.
    *   Passwords are securely hashed.
*   **Train Search**:
    *   Users can search for available trains by specifying a source and destination station.
    *   The system displays a list of matching trains, including their ID and station timings.
*   **Seat Booking**:
    *   Users can select a train from the search results.
    *   They can view the seat layout for the selected train (0 for available, 1 for booked).
    *   Users can book a specific seat by providing its row and column number.
    *   Booked seat information (row and column) is stored with the ticket.
*   **View Bookings**:
    *   Users can view a list of all tickets they have booked.
    *   Ticket details include source, destination, date of travel, train ID, and seat information.
*   **Ticket Cancellation**:
    *   Users can cancel a previously booked ticket.
    *   To cancel, the user needs to provide the unique `ticketId` of the booking.
    *   Upon successful cancellation:
        *   The ticket is removed from the user's list of booked tickets.
        *   The corresponding seat on the train is made available again for other users to book.
        *   Both user data (`users.json`) and train data (`trains.json`) are updated to reflect the cancellation.

## How to Run

1.  Ensure you have Java and Gradle installed.
2.  Clone the repository.
3.  Navigate to the project directory.
4.  Build the project using Gradle: `gradlew build` (or `./gradlew build` on Unix-like systems).
5.  Run the application using the main class `ticket.booking.App`. This might involve a command like `gradlew run` if a run task is configured in `build.gradle`, or by directly executing the JAR file produced by the build.

## Data Storage

*   User information (including booked tickets) is stored in `app/src/main/java/ticket/booking/localDB/users.json`.
*   Train information (including seat availability) is stored in `app/src/main/java/ticket/booking/localDB/trains.json`.

## Using the CLI

Upon running the application, users are presented with a menu:

1.  Sign up
2.  Login
3.  Fetch Bookings
4.  Search Trains
5.  Book a Seat
6.  Cancel my Booking
7.  Exit the App

Follow the on-screen prompts to interact with the system. For cancelling a booking (option 6), you will need the `ticketId`. This ID is generated upon booking and can be found by inspecting the `users.json` file or if the application is modified to display it after booking.
