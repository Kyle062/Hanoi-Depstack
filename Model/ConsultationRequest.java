package Model;

import java.io.Serializable;
import java.util.Date;

public class ConsultationRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String clientUsername;
    private String clientName;
    private String reason;
    private String advisorUsername;
    private String advisorName;
    private String platform;
    private String appointmentDate;
    private Date requestDate;
    private String status; // PENDING, SCHEDULED, REJECTED, COMPLETED

    public ConsultationRequest(String clientUsername, String clientName, String reason,
            String advisorUsername, String advisorName, String platform) {
        this.clientUsername = clientUsername;
        this.clientName = clientName;
        this.reason = reason;
        this.advisorUsername = advisorUsername;
        this.advisorName = advisorName;
        this.platform = platform;
        this.appointmentDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date());
        this.requestDate = new Date();
        this.status = "PENDING";
    }

    // Getters and Setters
    public String getClientUsername() {
        return clientUsername;
    }

    public String getClientName() {
        return clientName;
    }

    public String getReason() {
        return reason;
    }

    public String getAdvisorUsername() {
        return advisorUsername;
    }

    public String getAdvisorName() {
        return advisorName;
    }

    public String getPlatform() {
        return platform;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ConsultationRequest{" +
                "client='" + clientName + '\'' +
                ", advisor='" + advisorName + '\'' +
                ", reason='" + reason + '\'' +
                ", platform='" + platform + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}