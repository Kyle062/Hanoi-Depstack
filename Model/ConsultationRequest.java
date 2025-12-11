package Model;

import java.io.Serializable;
import java.util.Date;

public class ConsultationRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String clientUsername;
    private String clientName;
    private String reason;
    private String preferredDate;
    private Date requestDate;

    public ConsultationRequest(String clientUsername, String clientName, String reason, String preferredDate) {
        this.clientUsername = clientUsername;
        this.clientName = clientName;
        this.reason = reason;
        this.preferredDate = preferredDate;
        this.requestDate = new Date();
    }

    // Getters
    public String getClientUsername() {
        return clientUsername;
    }

    public String getClientName() {
        return clientName;
    }

    public String getReason() {
        return reason;
    }

    public String getPreferredDate() {
        return preferredDate;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    @Override
    public String toString() {
        return "ConsultationRequest{" +
                "clientUsername='" + clientUsername + '\'' +
                ", clientName='" + clientName + '\'' +
                ", reason='" + reason + '\'' +
                ", preferredDate='" + preferredDate + '\'' +
                ", requestDate=" + requestDate +
                '}';
    }
}