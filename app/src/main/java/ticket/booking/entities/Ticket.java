package ticket.booking.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Ticket{

    private String ticketId;

    private String userId;

    private String source;

    private String destination;

    private String dateOfTravel;

    private Train train;

    private int row;
    private int col;

    public Ticket(){
        this.row = -1;
        this.col = -1;
    }

    public Ticket(String ticketId, String userId, String source, String destination, String dateOfTravel, Train train, int row, int col){
        this.ticketId = ticketId;
        this.userId = userId;
        this.source = source;
        this.destination = destination;
        this.dateOfTravel = dateOfTravel;
        this.train = train;
        this.row = row;
        this.col = col;
    }

    public String getTicketInfo(){
        return String.format("Ticket ID: %s belongs to User %s from %s to %s on %s, Train: %s, Seat: [R%d, C%d]", ticketId, userId, source, destination, dateOfTravel, train.getTrainId(), row, col);
    }

    public String getTicketId(){
        return ticketId;
    }

    public void setTicketId(String ticketId){
        this.ticketId = ticketId;
    }

    public String getSource(){
        return source;
    }

    public void setSource(String source){
        this.source = source;
    }

    public String getUserId(){
        return userId;
    }

    public void setUserId(String userId){
        this.userId = userId;
    }

    public String getDestination(){
        return destination;
    }

    public void setDestination(String destination){
        this.destination = destination;
    }

    public String getDateOfTravel(){
        return dateOfTravel;
    }

    public void setDateOfTravel(String dateOfTravel){
        this.dateOfTravel = dateOfTravel;
    }

    public Train getTrain(){
        return train;
    }

    public void setTrain(Train train){
        this.train = train;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }
}