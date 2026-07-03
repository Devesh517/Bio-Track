package model;

import enums.BloodGroup;
import enums.Gender;
import enums.UserRole;

import java.time.LocalDate;

public class User {

    private int id;
    private String userId;        // e.g. "BT1025" - shown in the UI
    private String fullName;
    private String email;
    private String passwordHash;
    private Gender gender;
    private BloodGroup bloodGroup;
    private LocalDate dob;
    private String phone;
    private double heightCm;
    private UserRole role;

    public User() { }

    public User(String userId, String fullName, String email, String passwordHash) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = UserRole.PATIENT;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public BloodGroup getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(BloodGroup bloodGroup) { this.bloodGroup = bloodGroup; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public double getHeightCm() { return heightCm; }
    public void setHeightCm(double heightCm) { this.heightCm = heightCm; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public int getAge() {
        if (dob == null) return -1;
        return LocalDate.now().getYear() - dob.getYear();
    }

    @Override
    public String toString() {
        return fullName + " (" + userId + ")";
    }
}