package Model;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {
    private static final String USERS_FILE = "users.dat";
    private static final String DEBTS_FILE = "debts_";
    private static final String CONSULTATION_REQUESTS_FILE = "consultation_requests.dat";
    private static final String SCHEDULED_APPOINTMENTS_FILE = "scheduled_appointments.dat";

    // Save users to file
    public static void saveUsers(Map<String, User> users) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
            System.out.println("Users saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load users from file
    @SuppressWarnings("unchecked")
    public static Map<String, User> loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            System.out.println("Users file not found. Creating new user database.");
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<String, User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading users: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    // Save consultation requests
    public static void saveConsultationRequests(List<ConsultationRequest> requests) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CONSULTATION_REQUESTS_FILE))) {
            oos.writeObject(requests);
            System.out.println("Consultation requests saved: " + requests.size());
        } catch (IOException e) {
            System.err.println("Error saving consultation requests: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load consultation requests
    @SuppressWarnings("unchecked")
    public static List<ConsultationRequest> loadConsultationRequests() {
        File file = new File(CONSULTATION_REQUESTS_FILE);
        if (!file.exists()) {
            System.out.println("Consultation requests file not found.");
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<ConsultationRequest>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading consultation requests: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Save scheduled appointments
    public static void saveScheduledAppointments(List<ConsultationAppointment> appointments) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SCHEDULED_APPOINTMENTS_FILE))) {
            oos.writeObject(appointments);
            System.out.println("Scheduled appointments saved: " + appointments.size());
        } catch (IOException e) {
            System.err.println("Error saving scheduled appointments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load scheduled appointments
    @SuppressWarnings("unchecked")
    public static List<ConsultationAppointment> loadScheduledAppointments() {
        File file = new File(SCHEDULED_APPOINTMENTS_FILE);
        if (!file.exists()) {
            System.out.println("Scheduled appointments file not found.");
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<ConsultationAppointment>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading scheduled appointments: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Save debts for a specific user
    public static void saveUserDebts(String username, List<Debt> debts, List<Debt> paidOffDebts) {
        String filename = DEBTS_FILE + username + ".dat";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            DebtData debtData = new DebtData(debts, paidOffDebts);
            oos.writeObject(debtData);
            System.out.println("Debts saved for user: " + username);
        } catch (IOException e) {
            System.err.println("Error saving debts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load debts for a specific user
    public static DebtData loadUserDebts(String username) {
        String filename = DEBTS_FILE + username + ".dat";
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("No saved debts found for user: " + username);
            return new DebtData();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (DebtData) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading debts: " + e.getMessage());
            e.printStackTrace();
            return new DebtData();
        }
    }

    // Add a consultation request from client
    public static void addConsultationRequest(ConsultationRequest request) {
        List<ConsultationRequest> requests = loadConsultationRequests();
        requests.add(request);
        saveConsultationRequests(requests);
        System.out.println("Consultation request added for client: " + request.getClientName());
    }

    // Get all financial advisors
    public static List<User> getFinancialAdvisors() {
        Map<String, User> users = loadUsers();
        List<User> advisors = new ArrayList<>();

        for (User user : users.values()) {
            if ("ADVISOR".equals(user.getUserType())) {
                advisors.add(user);
            }
        }

        System.out.println("Found " + advisors.size() + " financial advisors");
        return advisors;
    }

    // Get consultation requests for a specific advisor (by username)
    public static List<ConsultationRequest> getConsultationRequestsForAdvisor(String advisorUsername) {
        List<ConsultationRequest> allRequests = loadConsultationRequests();
        List<ConsultationRequest> advisorRequests = new ArrayList<>();

        // For now, all requests are visible to all advisors
        // In a more advanced system, you could filter by advisor specialization, etc.
        return allRequests;
    }

    // Get scheduled appointments for a specific advisor
    public static List<ConsultationAppointment> getAppointmentsForAdvisor(String advisorUsername) {
        List<ConsultationAppointment> allAppointments = loadScheduledAppointments();
        List<ConsultationAppointment> advisorAppointments = new ArrayList<>();

        for (ConsultationAppointment appointment : allAppointments) {
            if (appointment.getAdvisorUsername().equals(advisorUsername)) {
                advisorAppointments.add(appointment);
            }
        }

        return advisorAppointments;
    }

    // Delete a consultation request (when scheduled or rejected)
    public static void deleteConsultationRequest(ConsultationRequest request) {
        List<ConsultationRequest> requests = loadConsultationRequests();
        requests.remove(request);
        saveConsultationRequests(requests);
        System.out.println("Consultation request removed for client: " + request.getClientName());
    }

    // Inner class to hold both current and paid off debts
    public static class DebtData implements Serializable {
        private static final long serialVersionUID = 1L;
        private List<Debt> currentDebts;
        private List<Debt> paidOffDebts;

        public DebtData() {
            this.currentDebts = new java.util.ArrayList<>();
            this.paidOffDebts = new java.util.ArrayList<>();
        }

        public DebtData(List<Debt> currentDebts, List<Debt> paidOffDebts) {
            this.currentDebts = currentDebts;
            this.paidOffDebts = paidOffDebts;
        }

        public List<Debt> getCurrentDebts() {
            return currentDebts;
        }

        public List<Debt> getPaidOffDebts() {
            return paidOffDebts;
        }
    }
}