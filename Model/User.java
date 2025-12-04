package Model;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String fullName;
    private String email;
    private String username;
    private String password;
    private String userType; // "DEBTOR" or "ADVISOR"

    public User(String fullName, String email, String username, String password, String userType) {
        this.fullName = fullName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.userType = userType;
    }

    // Getters and setters
    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getUserType() {
        return userType;
    }

    // Update the existing constructor if needed
    public User(String fullName, String email, String username, String password) {
        this(fullName, email, username, password, "DEBTOR"); // Default to debtor
    }
}