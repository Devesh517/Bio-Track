package biotrack.model;
import java.sql.Timestamp;

public class User {
    private final int userId;
    private final String name;
    private final int age;
    private final String gender;
    private final String phone;
    private final String email;
    private final String bloodGroup;
    private final String emergencyContact;
    private final String passwordHash;
    private final byte[] profilePicture;
    private final String profilePictureType;
    private final String conditions;
    private final String allergies;
    private final Timestamp createdAt;

    public User(int userId, String name, int age, String gender, String phone, String email,
                String bloodGroup, String emergencyContact, String passwordHash,
                byte[] profilePicture, String profilePictureType, String conditions, String allergies,
                Timestamp createdAt) {
        this.userId = userId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.bloodGroup = bloodGroup;
        this.emergencyContact = emergencyContact;
        this.passwordHash = passwordHash;
        this.profilePicture = profilePicture;
        this.profilePictureType = profilePictureType;
        this.conditions = conditions;
        this.allergies = allergies;
        this.createdAt = createdAt;
    }

    public int getUserId() { return userId; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getBloodGroup() { return bloodGroup; }
    public String getEmergencyContact() { return emergencyContact; }
    /** Only used internally by DAO/auth code - never display this in the UI. */
    public String getPasswordHash() { return passwordHash; }
    public byte[] getProfilePicture() { return profilePicture; }
    public String getProfilePictureType() { return profilePictureType; }
    public boolean hasProfilePicture() { return profilePicture != null && profilePicture.length > 0; }
    public String getConditions() { return conditions; }
    public String getAllergies() { return allergies; }
    public Timestamp getCreatedAt() { return createdAt; }
}
