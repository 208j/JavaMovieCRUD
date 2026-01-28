import java.sql.*;
import java.util.Scanner;

public class Main {
    private static final String URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASS = "9090";
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            while (true) {
                System.out.println("\n--- MOVIE SYSTEM ---");
                System.out.println("1-Create  2-Show All  3-Find  4-Update  5-Delete  6-Exit");
                System.out.print("Action: ");

                String choice = sc.nextLine();
                switch (choice) {
                    case "1" -> addMovie(conn);
                    case "2" -> showMovies(conn);
                    case "3" -> findMovie(conn);
                    case "4" -> updateMovie(conn);
                    case "5" -> deleteMovies(conn);
                    case "6" -> { return; }
                    default -> System.out.println("Invalid choice.");
                }
            }
        } catch (Exception e) { System.err.println("Error: " + e.getMessage()); }
    }

    private static void addMovie(Connection conn) throws SQLException {
        // Validate title
        String title;
        while (true) {
            System.out.print("Title: ");
            title = sc.nextLine().trim();

            if (title.isEmpty()) {
                System.out.println("Title cannot be empty. Please try again.");
                continue;
            }

            // Check for duplicate title
            String checkSql = "SELECT COUNT(*) FROM movie_tickets WHERE LOWER(title) = LOWER(?)";
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setString(1, title);
                ResultSet rs = checkPs.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Movie with this title already exists. Please choose a different title.");
                    continue;
                }
            }
            break;
        }

        // Validate genre (no numbers allowed)
        String genre;
        while (true) {
            System.out.print("Genre: ");
            genre = sc.nextLine().trim();

            if (genre.isEmpty()) {
                System.out.println("Genre cannot be empty. Please try again.");
                continue;
            }

            // Check if genre contains numbers
            boolean hasNumber = false;
            for (char c : genre.toCharArray()) {
                if (Character.isDigit(c)) {
                    hasNumber = true;
                    break;
                }
            }

            if (hasNumber) {
                System.out.println("Genre cannot contain numbers. Please try again.");
                continue;
            }
            break;
        }

        // Validate duration
        int dur;
        while (true) {
            System.out.print("Duration (minutes): ");
            try {
                dur = Integer.parseInt(sc.nextLine());
                if (dur <= 0) {
                    System.out.println("Duration must be greater than 0. Please try again.");
                    continue;
                }
                if (dur > 400) {
                    System.out.println("Duration is too long. Maximum is 400 minutes. Please try again.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }

        // Validate rating
        double rate;
        while (true) {
            System.out.print("Rating (0-10): ");
            try {
                rate = Double.parseDouble(sc.nextLine());
                if (rate < 0) {
                    System.out.println("Rating cannot be less than 0. Please try again.");
                    continue;
                }
                if (rate > 10) {
                    System.out.println("Rating cannot be greater than 10. Please try again.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid rating format. Please enter a valid number.");
            }
        }

        String sql = "INSERT INTO movie_tickets (title, genre, duration, rating, is_available) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, genre);
            ps.setInt(3, dur);
            ps.setDouble(4, rate);
            ps.setBoolean(5, true); // Default to available
            ps.executeUpdate();
            System.out.println("Movie added!");
        }
    }

    private static void showMovies(Connection conn) throws SQLException {
        String sql = "SELECT *, CASE WHEN is_available THEN 'Available' ELSE 'Not Available' END as status FROM movie_tickets ORDER BY id";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            System.out.printf("| %-3s| %-25s| %-15s| %-10s| %-7s| %-12s|\n",
                    "ID", "Title", "Genre", "Duration", "Rating", "Status");
            System.out.println("-----------------------------------------");

            boolean hasMovies = false;
            while (rs.next()) {
                hasMovies = true;
                System.out.printf("| %-3d| %-25s| %-15s| %-10d| %-7.1f| %-12s|\n",
                        rs.getInt("id"),
                        truncateString(rs.getString("title"), 23),
                        truncateString(rs.getString("genre"), 13),
                        rs.getInt("duration"),
                        rs.getDouble("rating"),
                        rs.getString("status"));
            }

            if (!hasMovies) {
                System.out.println("No movies found.");
            }
        }
    }

    private static void findMovie(Connection conn) throws SQLException {
        int id;
        while (true) {
            System.out.print("ID: ");
            try {
                id = Integer.parseInt(sc.nextLine());
                if (id <= 0) {
                    System.out.println("ID must be greater than 0. Please try again.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid ID format. Please enter a valid number.");
            }
        }

        String sql = "SELECT *, CASE WHEN is_available THEN 'Available' ELSE 'Not Available' END as status FROM movie_tickets WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("\nMovie found:");
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Title: " + rs.getString("title"));
                System.out.println("Genre: " + rs.getString("genre"));
                System.out.println("Duration: " + rs.getInt("duration") + " minutes");
                System.out.println("Rating: " + rs.getDouble("rating") + "/10");
                System.out.println("Status: " + rs.getString("status"));
            } else {
                System.out.println("Movie not found.");
            }
        }
    }

    private static void updateMovie(Connection conn) throws SQLException {
        int id;
        while (true) {
            System.out.print("ID to update: ");
            try {
                id = Integer.parseInt(sc.nextLine());
                if (id <= 0) {
                    System.out.println("ID must be greater than 0. Please try again.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid ID format. Please enter a valid number.");
            }
        }

        // Check if movie exists
        String checkSql = "SELECT id FROM movie_tickets WHERE id = ?";
        try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setInt(1, id);
            ResultSet rs = checkPs.executeQuery();
            if (!rs.next()) {
                System.out.println("Movie with ID " + id + " not found.");
                return;
            }
        }

        double rate;
        while (true) {
            System.out.print("New Rating (0-10): ");
            try {
                rate = Double.parseDouble(sc.nextLine());
                if (rate < 0) {
                    System.out.println("Rating cannot be less than 0. Please try again.");
                    continue;
                }
                if (rate > 10) {
                    System.out.println("Rating cannot be greater than 10. Please try again.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid rating format. Please enter a valid number.");
            }
        }

        try (PreparedStatement ps = conn.prepareStatement("UPDATE movie_tickets SET rating = ? WHERE id = ?")) {
            ps.setDouble(1, rate);
            ps.setInt(2, id);
            System.out.println(ps.executeUpdate() > 0 ? "Updated!" : "Update failed.");
        }
    }

    private static void deleteMovies(Connection conn) throws SQLException {
        System.out.print("Enter IDs to delete (space separated): ");
        String[] ids = sc.nextLine().split(" ");

        if (ids.length == 0 || (ids.length == 1 && ids[0].isEmpty())) {
            System.out.println("No IDs provided. Deletion cancelled.");
            return;
        }

        // Validate all IDs
        boolean validIds = true;
        for (String idStr : ids) {
            try {
                int id = Integer.parseInt(idStr.trim());
                if (id <= 0) {
                    System.out.println("Invalid ID: " + idStr + " (must be greater than 0)");
                    validIds = false;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid ID format: " + idStr);
                validIds = false;
            }
        }

        if (!validIds) {
            System.out.println("Deletion cancelled due to invalid IDs.");
            return;
        }

        System.out.print("Are you sure you want to delete " + ids.length + " movie(s)? (y/n): ");
        if (!sc.nextLine().equalsIgnoreCase("y")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        int deletedCount = 0;
        for (String id : ids) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM movie_tickets WHERE id = ?")) {
                ps.setInt(1, Integer.parseInt(id.trim()));
                deletedCount += ps.executeUpdate();
            }
        }
        System.out.println("Deletions complete. " + deletedCount + " movie(s) deleted.");
    }

    // Helper method to truncate long strings for table display
    private static String truncateString(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}