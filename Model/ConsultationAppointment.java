package Model;

import java.io.Serializable;
import java.util.Date;

public class ConsultationAppointment implements Serializable {
    private static final long serialVersionUID = 1L;

    private String clientUsername;
    private String clientName;
    private String advisorUsername;
    private String advisorName;
    private String reason;
    private String platform;
    private String appointmentDate;
    private Date scheduledDate;
    private String status; // SCHEDULED, COMPLETED, CANCELLED

    public ConsultationAppointment(String clientUsername, String clientName,
            String advisorUsername, String advisorName,
            String reason, String platform, String appointmentDate,
            Date scheduledDate, String status) {
        this.clientUsername = clientUsername;
        this.clientName = clientName;
        this.advisorUsername = advisorUsername;
        this.advisorName = advisorName;
        this.reason = reason;
        this.platform = platform;
        this.appointmentDate = appointmentDate;
        this.scheduledDate = scheduledDate;
        this.status = status;
    }

    // Getters and Setters
    public String getClientUsername() {
        return clientUsername;
    }

    public String getClientName() {
        return clientName;
    }

    public String getAdvisorUsername() {
        return advisorUsername;
    }

    public String getAdvisorName() {
        return advisorName;
    }

    public String getReason() {
        return reason;
    }

    public String getPlatform() {
        return platform;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public Date getScheduledDate() {
        return scheduledDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ConsultationAppointment{" +
                "clientUsername='" + clientUsername + '\'' +
                ", clientName='" + clientName + '\'' +
                ", advisorUsername='" + advisorUsername + '\'' +
                ", advisorName='" + advisorName + '\'' +
                ", reason='" + reason + '\'' +
                ", platform='" + platform + '\'' +
                ", appointmentDate='" + appointmentDate + '\'' +
                ", scheduledDate=" + scheduledDate +
                ", status='" + status + '\'' +
                '}';
    }
}