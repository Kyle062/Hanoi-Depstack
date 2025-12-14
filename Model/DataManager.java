package Model;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataManager {
    private static final String USERS_FILE = "users.dat";
    private static final String DEBTS_FILE = "debts_";
    private static final String CONSULTATION_REQUESTS_FILE = "consultation_requests.dat";
    private static final String SCHEDULED_APPOINTMENTS_FILE = "scheduled_appointments.dat";
    private static final String CLIENT_REQUESTS_FILE = "client_requests_";

    // Save users
    public static void saveUsers(Map<String, User> users) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    // Load users
    @SuppressWarnings("unchecked")
    public static Map<String, User> loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<String, User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new HashMap<>();
        }
    }

    // Save consultation requests (for advisors)
    public static void saveConsultationRequests(ArrayList<ConsultationRequest> requests) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CONSULTATION_REQUESTS_FILE))) {
            oos.writeObject(requests);
        } catch (IOException e) {
            System.err.println("Error saving consultation requests: " + e.getMessage());
        }
    }

    // Load consultation requests (for advisors)
    @SuppressWarnings("unchecked")
    public static ArrayList<ConsultationRequest> loadConsultationRequests() {
        File file = new File(CONSULTATION_REQUESTS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (ArrayList<ConsultationRequest>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }

    // Save scheduled appointments
    public static void saveScheduledAppointments(ArrayList<ConsultationAppointment> appointments) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SCHEDULED_APPOINTMENTS_FILE))) {
            oos.writeObject(appointments);
        } catch (IOException e) {
            System.err.println("Error saving scheduled appointments: " + e.getMessage());
        }
    }

    // Load scheduled appointments
    @SuppressWarnings("unchecked")
    public static ArrayList<ConsultationAppointment> loadScheduledAppointments() {
        File file = new File(SCHEDULED_APPOINTMENTS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (ArrayList<ConsultationAppointment>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }

    // Save client consultation requests (for clients to see their own requests)
    public static void saveClientRequests(String clientUsername, ArrayList<ConsultationRequest> requests) {
        String filename = CLIENT_REQUESTS_FILE + clientUsername + ".dat";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(requests);
        } catch (IOException e) {
            System.err.println("Error saving client requests: " + e.getMessage());
        }
    }

    // Load client consultation requests
    @SuppressWarnings("unchecked")
    public static ArrayList<ConsultationRequest> loadClientRequests(String clientUsername) {
        String filename = CLIENT_REQUESTS_FILE + clientUsername + ".dat";
        File file = new File(filename);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (ArrayList<ConsultationRequest>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }

    // Save user debts
    public static void saveUserDebts(String username, ArrayList<Debt> debts, ArrayList<Debt> paidOffDebts) {
        String filename = DEBTS_FILE + username + ".dat";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            DebtData debtData = new DebtData(debts, paidOffDebts);
            oos.writeObject(debtData);
        } catch (IOException e) {
            System.err.println("Error saving debts: " + e.getMessage());
        }
    }

    // Load user debts
    public static DebtData loadUserDebts(String username) {
        String filename = DEBTS_FILE + username + ".dat";
        File file = new File(filename);
        if (!file.exists()) {
            return new DebtData();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (DebtData) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new DebtData();
        }
    }

    // Add a consultation request
    public static void addConsultationRequest(ConsultationRequest request) {
        // Add to advisor's list
        ArrayList<ConsultationRequest> advisorRequests = loadConsultationRequests();
        advisorRequests.add(request);
        saveConsultationRequests(advisorRequests);

        // Add to client's personal list
        ArrayList<ConsultationRequest> clientRequests = loadClientRequests(request.getClientUsername());
        clientRequests.add(request);
        saveClientRequests(request.getClientUsername(), clientRequests);
    }

    // Get all financial advisors
    public static ArrayList<User> getFinancialAdvisors() {
        Map<String, User> users = loadUsers();
        ArrayList<User> advisors = new ArrayList<>();

        for (User user : users.values()) {
            if ("ADVISOR".equals(user.getUserType())) {
                advisors.add(user);
            }
        }

        return advisors;
    }

    // In DataManager class, update or replace the deleteConsultationRequest method:
    public static void deleteConsultationRequest(ConsultationRequest request) {
        try {
            // Remove from advisor's global list
            ArrayList<ConsultationRequest> advisorRequests = loadConsultationRequests();
            ConsultationRequest toRemove = null;

            // Find the exact request to remove
            for (ConsultationRequest req : advisorRequests) {
                if (req.getClientUsername().equals(request.getClientUsername()) &&
                        req.getAdvisorUsername().equals(request.getAdvisorUsername()) &&
                        req.getReason().equals(request.getReason())) {

                    // Use timestamp comparison for better accuracy
                    long timeDiff = Math.abs(req.getRequestDate().getTime() - request.getRequestDate().getTime());
                    if (timeDiff < 60000) { // Within 1 minute
                        toRemove = req;
                        break;
                    }
                }
            }

            if (toRemove != null) {
                advisorRequests.remove(toRemove);
                saveConsultationRequests(advisorRequests);
                System.out.println("Removed consultation request from advisor list: " +
                        request.getClientName() + " -> " + request.getAdvisorName());
            }

            // Update client's list - don't remove, just update status
            ArrayList<ConsultationRequest> clientRequests = loadClientRequests(request.getClientUsername());
            boolean updated = false;

            for (int i = 0; i < clientRequests.size(); i++) {
                ConsultationRequest cr = clientRequests.get(i);
                if (cr.getClientUsername().equals(request.getClientUsername()) &&
                        cr.getAdvisorUsername().equals(request.getAdvisorUsername()) &&
                        cr.getReason().equals(request.getReason())) {

                    // Update the status to match
                    cr.setStatus(request.getStatus());
                    updated = true;
                    System.out.println("Updated client request status to: " + request.getStatus());
                    break;
                }
            }

            if (!updated) {
                // If not found, add it with the updated status
                request.setStatus(request.getStatus());
                clientRequests.add(request);
            }

            saveClientRequests(request.getClientUsername(), clientRequests);

        } catch (Exception e) {
            System.err.println("Error in deleteConsultationRequest: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // In the DataManager class, add this method:
    public static void updateClientRequestStatus(String clientUsername, ConsultationRequest originalRequest,
            String newStatus) {
        try {
            // Load client's personal requests
            ArrayList<ConsultationRequest> clientRequests = loadClientRequests(clientUsername);

            // Find and update the matching request
            for (int i = 0; i < clientRequests.size(); i++) {
                ConsultationRequest cr = clientRequests.get(i);

                // Match by multiple criteria to ensure we find the right request
                if (cr.getClientUsername().equals(originalRequest.getClientUsername()) &&
                        cr.getAdvisorUsername().equals(originalRequest.getAdvisorUsername()) &&
                        cr.getReason().equals(originalRequest.getReason()) &&
                        Math.abs(cr.getRequestDate().getTime() - originalRequest.getRequestDate().getTime()) < 1000) { // Within
                                                                                                                       // 1
                                                                                                                       // second

                    // Update the status
                    cr.setStatus(newStatus);
                    System.out.println("Updated request status for client " + clientUsername +
                            " to " + newStatus + " with advisor " + originalRequest.getAdvisorName());
                    break;
                }
            }

            // Save the updated client requests
            saveClientRequests(clientUsername, clientRequests);

        } catch (Exception e) {
            System.err.println("Error updating client request status for " + clientUsername + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // DebtData class
    public static class DebtData implements Serializable {
        private static final long serialVersionUID = 1L;
        private ArrayList<Debt> currentDebts;
        private ArrayList<Debt> paidOffDebts;

        public DebtData() {
            this.currentDebts = new ArrayList<>();
            this.paidOffDebts = new ArrayList<>();
        }

        public DebtData(ArrayList<Debt> currentDebts, ArrayList<Debt> paidOffDebts) {
            this.currentDebts = currentDebts;
            this.paidOffDebts = paidOffDebts;
        }

        public ArrayList<Debt> getCurrentDebts() {
            return currentDebts;
        }

        public ArrayList<Debt> getPaidOffDebts() {
            return paidOffDebts;
        }
    }
}