package local.pk154938.shop.domain.ticket;

import java.time.LocalDateTime;

/**
 * Mutable POJO representing a customer ticket. Identity comes from the
 * DB-assigned {@code id}; {@code 0} marks a freshly-created ticket not yet
 * persisted. Service layer is responsible for enforcing which fields may
 * change at which point in the lifecycle.
 */
public class Ticket {
    private int id;
    private LocalDateTime createdAt;
    private String firstName;
    private String lastName;
    private String clientMessage;
    private LocalDateTime clientMessageUpdatedAt;
    private String workerMessage;
    private String workerMessageAuthor;
    private LocalDateTime workerMessageUpdatedAt;
    private LocalDateTime predictedCompletionAt;
    private TicketStatus status;
    private LocalDateTime closedAt;

    public Ticket(int id,
                  LocalDateTime createdAt,
                  String firstName,
                  String lastName,
                  String clientMessage,
                  LocalDateTime clientMessageUpdatedAt,
                  String workerMessage,
                  String workerMessageAuthor,
                  LocalDateTime workerMessageUpdatedAt,
                  LocalDateTime predictedCompletionAt,
                  TicketStatus status,
                  LocalDateTime closedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.firstName = firstName;
        this.lastName = lastName;
        this.clientMessage = clientMessage;
        this.clientMessageUpdatedAt = clientMessageUpdatedAt;
        this.workerMessage = workerMessage;
        this.workerMessageAuthor = workerMessageAuthor;
        this.workerMessageUpdatedAt = workerMessageUpdatedAt;
        this.predictedCompletionAt = predictedCompletionAt;
        this.status = status;
        this.closedAt = closedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }

    public String getClientMessage() { return clientMessage; }
    public void setClientMessage(String clientMessage) { this.clientMessage = clientMessage; }

    public LocalDateTime getClientMessageUpdatedAt() { return clientMessageUpdatedAt; }
    public void setClientMessageUpdatedAt(LocalDateTime ts) { this.clientMessageUpdatedAt = ts; }

    public String getWorkerMessage() { return workerMessage; }
    public void setWorkerMessage(String m) { this.workerMessage = m; }

    public String getWorkerMessageAuthor() { return workerMessageAuthor; }
    public void setWorkerMessageAuthor(String a) { this.workerMessageAuthor = a; }

    public LocalDateTime getWorkerMessageUpdatedAt() { return workerMessageUpdatedAt; }
    public void setWorkerMessageUpdatedAt(LocalDateTime ts) { this.workerMessageUpdatedAt = ts; }

    public LocalDateTime getPredictedCompletionAt() { return predictedCompletionAt; }
    public void setPredictedCompletionAt(LocalDateTime ts) { this.predictedCompletionAt = ts; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime ts) { this.closedAt = ts; }
}
