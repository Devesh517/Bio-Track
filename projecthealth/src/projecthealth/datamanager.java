package projecthealth;

import java.sql.*;

public class datamanager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/health"; 
    private static final String USER = "root"; 
    private static final String PASSWORD = "";

    // Create Database and Tables if not already existing
    public static void createDatabaseAndTable() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            Statement stmt = conn.createStatement();

            // Creating the healthbasicinfo table
            String createUserTable = "CREATE TABLE IF NOT EXISTS healthbasicinfo (" +
                                     "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                                     "name VARCHAR(70) NOT NULL, " +
                                     "age INT NOT NULL, " +
                                     "gender VARCHAR(30), " +
                                     "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(createUserTable);

            // Creating the healthdata table
            String createHealthDataTable = "CREATE TABLE IF NOT EXISTS healthdata (" +
                                           "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                           "user_id INT, " +
                                           "weight DOUBLE, " +
                                           "height DOUBLE, " +
                                           "bmi DOUBLE, " +
                                           "temperature DOUBLE, " +
                                           "blood_pressure INT, " +
                                           "heart_rate INT, " +
                                           "sugar_level INT, " +
                                           "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                                           "FOREIGN KEY (user_id) REFERENCES healthbasicinfo(user_id))";
            stmt.execute(createHealthDataTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Insert User into healthbasicinfo table and return the generated userId
    public static int insertUser(String name, int age, String gender) {
        String query = "INSERT INTO healthbasicinfo (name, age, gender) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, age);
            pstmt.setString(3, gender);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);  // Return the generated user_id
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;  // Return -1 if error occurs
    }

    // Insert health data for the user
    public static void insertHealthData(int userId, double weight, double height, double bmi,
                                        double temperature, int bloodPressure, int heartRate, int sugarLevel) {
        String query = "INSERT INTO healthdata (user_id, weight, height, bmi, temperature, blood_pressure, heart_rate, sugar_level) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setDouble(2, weight);
            pstmt.setDouble(3, height);
            pstmt.setDouble(4, bmi);
            pstmt.setDouble(5, temperature);
            pstmt.setInt(6, bloodPressure);
            pstmt.setInt(7, heartRate);
            pstmt.setInt(8, sugarLevel);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fetch health data and user details for all users
    public static void fetchData() {
        String query = "SELECT u.user_id, u.name, u.age, u.gender, h.weight, h.height, h.bmi, h.temperature, " +
                       "h.blood_pressure, h.heart_rate, h.sugar_level, h.timestamp " +
                       "FROM healthbasicinfo u " +
                       "INNER JOIN healthdata h ON u.user_id = h.user_id";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                System.out.println("User ID: " + rs.getInt("user_id") +
                                   ", Name: " + rs.getString("name") +
                                   ", Age: " + rs.getInt("age") +
                                   ", Gender: " + rs.getString("gender") +
                                   ", Weight: " + rs.getDouble("weight") + " kg" +
                                   ", Height: " + rs.getDouble("height") + " m" +
                                   ", BMI: " + rs.getDouble("bmi") +
                                   ", Temperature: " + rs.getDouble("temperature") + " °C" +
                                   ", Blood Pressure: " + rs.getInt("blood_pressure") + " mmHg" +
                                   ", Heart Rate: " + rs.getInt("heart_rate") + " bpm" +
                                   ", Sugar Level: " + rs.getInt("sugar_level") + " mg/dL" +
                                   ", Timestamp: " + rs.getTimestamp("timestamp"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fetch health data for a specific user by userId and return it as a formatted string
    public static String fetchDataById(int userId) {
        StringBuilder result = new StringBuilder();
        String query = "SELECT u.user_id, u.name, u.age, u.gender, h.weight, h.height, h.bmi, h.temperature, " +
                       "h.blood_pressure, h.heart_rate, h.sugar_level, h.timestamp " +
                       "FROM healthbasicinfo u " +
                       "INNER JOIN healthdata h ON u.user_id = h.user_id " +
                       "WHERE u.user_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                result.append("User ID: ").append(rs.getInt("user_id")).append("\n");
                result.append("Name: ").append(rs.getString("name")).append("\n");
                result.append("Age: ").append(rs.getInt("age")).append("\n");
                result.append("Gender: ").append(rs.getString("gender")).append("\n");
                result.append("Weight: ").append(rs.getDouble("weight")).append(" kg\n");
                result.append("Height: ").append(rs.getDouble("height")).append(" m\n");
                result.append("BMI: ").append(rs.getDouble("bmi")).append("\n");
                result.append("Body Temperature: ").append(rs.getDouble("temperature")).append(" °C\n");
                result.append("Blood Pressure: ").append(rs.getInt("blood_pressure")).append(" mmHg\n");
                result.append("Heart Rate: ").append(rs.getInt("heart_rate")).append(" bpm\n");
                result.append("Sugar Level: ").append(rs.getInt("sugar_level")).append(" mg/dL\n");
                result.append("Timestamp: ").append(rs.getTimestamp("timestamp")).append("\n");
            } else {
                result.append("No data found for User ID: ").append(userId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

}
