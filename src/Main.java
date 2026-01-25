import java.sql.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        String connectionUrl = "jdbc:postgresql://localhost:5432/postgres";

        Connection con = null;
        Statement stmt = null;

        try {
            Class.forName("org.postgresql.Driver");
            con = DriverManager.getConnection(connectionUrl, "postgres", "9090");
            stmt = con.createStatement();

            while (true) {

                System.out.println("\n1 - create movie");
                System.out.println("2 - read all movies");
                System.out.println("3 - read movie by id");
                System.out.println("4 - update movie");
                System.out.println("5 - delete movie");
                System.out.println("6 - exit");
                System.out.print("Choose (1-6): ");

                if (!scanner.hasNextInt()) {
                    System.out.println("Error: Please enter a number!");
                    scanner.nextLine();
                    continue;
                }
                int choice = scanner.nextInt();
                scanner.nextLine();

                if (choice < 1 || choice > 6) {
                    System.out.println("Error: Please enter a number between 1 and 6.");
                    continue;
                }

                if (choice == 1) {
                    System.out.println("--- Create New Movie ---");

                    String title = "";
                    while (true) {
                        System.out.print("Title: ");
                        title = scanner.nextLine();

                        String checkSql = "SELECT count(*) FROM movie_tickets WHERE title = ?";
                        PreparedStatement checkStmt = con.prepareStatement(checkSql);
                        checkStmt.setString(1, title);
                        ResultSet checkRs = checkStmt.executeQuery();

                        checkRs.next();
                        int count = checkRs.getInt(1);

                        if (count > 0) {
                            System.out.println("Error: Movie with this title already exists! Try another.");
                        } else {
                            break;
                        }
                    }

                    System.out.print("Genre: ");
                    String genre = scanner.nextLine();

                    int duration = -1;
                    while (duration <= 0) {
                        System.out.print("Duration (min): ");
                        if (scanner.hasNextInt()) {
                            duration = scanner.nextInt();
                            if (duration <= 0) System.out.println("Error: Duration must be positive!");
                        } else {
                            System.out.println("Error: Please enter a number!");
                            scanner.next();
                        }
                    }

                    double rating = -1;
                    while (rating < 0 || rating > 10) {
                        System.out.print("Rating (0.0 - 10.0): ");
                        if (scanner.hasNextDouble()) {
                            rating = scanner.nextDouble();
                            if (rating < 0 || rating > 10) System.out.println("Error: Rating must be between 0 and 10!");
                        } else {
                            System.out.println("Error: Please enter a number!");
                            scanner.next();
                        }
                    }

                    System.out.print("Available (true/false): ");
                    boolean isAvailable = scanner.nextBoolean();
                    scanner.nextLine();

                    String sql = "INSERT INTO movie_tickets (title, genre, duration, rating, is_available) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement ps = con.prepareStatement(sql);

                    ps.setString(1, title);
                    ps.setString(2, genre);
                    ps.setInt(3, duration);
                    ps.setDouble(4, rating);
                    ps.setBoolean(5, isAvailable);

                    ps.executeUpdate();
                    System.out.println("Success: Movie created!");
                }
                else if (choice == 2) {

                    String sql = "SELECT * FROM movie_tickets";

                    ResultSet rs = stmt.executeQuery(sql);

                    System.out.println("\n--- MOVIE LIST ---");

                    while (rs.next()) {
                        System.out.println(
                                rs.getInt("id") + " | " +
                                        rs.getString("title") + " | " +
                                        rs.getString("genre") + " | " +
                                        rs.getInt("duration") + " min | " +
                                        rs.getDouble("rating") + "/10 | " +
                                        rs.getBoolean("is_available")
                        );
                    }
                }
                else if (choice == 3) {

                    System.out.print("Enter movie id: ");
                    int id = scanner.nextInt();
                    scanner.nextLine();

                    String sql = "SELECT * FROM movie_tickets WHERE id = ?";

                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, id);

                    ResultSet rs = ps.executeQuery();

                    if (rs.next()) {
                        System.out.println(
                                rs.getInt("id") + " | " +
                                        rs.getString("title") + " | " +
                                        rs.getString("genre") + " | " +
                                        rs.getInt("duration") + " min | " +
                                        rs.getDouble("rating") + "/10 | " +
                                        rs.getBoolean("is_available")
                        );
                    } else {
                        System.out.println("Movie not found");
                    }
                }
                else if (choice == 4) {

                    System.out.print("Enter movie ID to update: ");
                    if (!scanner.hasNextInt()) {
                        System.out.println("Invalid ID format.");
                        scanner.nextLine();
                        continue;
                    }
                    int id = scanner.nextInt();
                    scanner.nextLine();

                    String selectSql = "SELECT * FROM movie_tickets WHERE id = ?";
                    PreparedStatement psSelect = con.prepareStatement(selectSql);
                    psSelect.setInt(1, id);
                    ResultSet rs = psSelect.executeQuery();

                    if (!rs.next()) {
                        System.out.println("Movie with ID " + id + " not found.");
                        continue;
                    }

                    System.out.println("\nCurrent movie data:");
                    System.out.println("1. Title: " + rs.getString("title"));
                    System.out.println("2. Genre: " + rs.getString("genre"));
                    System.out.println("3. Duration: " + rs.getInt("duration"));
                    System.out.println("4. Rating: " + rs.getDouble("rating"));
                    System.out.println("5. Available: " + rs.getBoolean("is_available"));

                    System.out.println("\nChoose field to update (1-5):");
                    if (!scanner.hasNextInt()) {
                        System.out.println("Invalid choice.");
                        scanner.nextLine();
                        continue;
                    }
                    int fieldChoice = scanner.nextInt();
                    scanner.nextLine();

                    String sql = "";
                    PreparedStatement psUpdate = null;

                    switch (fieldChoice) {
                        case 1:
                            System.out.println("Old title: " + rs.getString("title"));
                            String newTitle = "";
                            while (true) {
                                System.out.print("Enter new title: ");
                                newTitle = scanner.nextLine();

                                if (newTitle.trim().isEmpty()) {
                                    System.out.println("Title cannot be empty.");
                                    continue;
                                }

                                String checkSql = "SELECT count(*) FROM movie_tickets WHERE title = ? AND id != ?";
                                PreparedStatement checkStmt = con.prepareStatement(checkSql);
                                checkStmt.setString(1, newTitle);
                                checkStmt.setInt(2, id);
                                ResultSet checkRs = checkStmt.executeQuery();
                                checkRs.next();

                                if (checkRs.getInt(1) > 0) {
                                    System.out.println("Error: Title already exists! Choose another.");
                                } else {
                                    break;
                                }
                            }

                            sql = "UPDATE movie_tickets SET title = ? WHERE id = ?";
                            psUpdate = con.prepareStatement(sql);
                            psUpdate.setString(1, newTitle);
                            psUpdate.setInt(2, id);
                            break;

                        case 2:
                            System.out.println("Old genre: " + rs.getString("genre"));
                            System.out.print("New genre: ");
                            String newGenre = scanner.nextLine();

                            sql = "UPDATE movie_tickets SET genre = ? WHERE id = ?";
                            psUpdate = con.prepareStatement(sql);
                            psUpdate.setString(1, newGenre);
                            psUpdate.setInt(2, id);
                            break;

                        case 3:
                            System.out.println("Old duration: " + rs.getInt("duration"));
                            int newDuration = -1;

                            while (newDuration <= 0) {
                                System.out.print("Enter new duration (min): ");
                                if (scanner.hasNextInt()) {
                                    newDuration = scanner.nextInt();
                                    if (newDuration <= 0) System.out.println("Error: Duration must be positive.");
                                } else {
                                    System.out.println("Error: Enter a valid number.");
                                    scanner.next();
                                }
                            }
                            scanner.nextLine();

                            sql = "UPDATE movie_tickets SET duration = ? WHERE id = ?";
                            psUpdate = con.prepareStatement(sql);
                            psUpdate.setInt(1, newDuration);
                            psUpdate.setInt(2, id);
                            break;

                        case 4:
                            System.out.println("Old rating: " + rs.getDouble("rating"));
                            double newRating = -1;

                            while (newRating < 0 || newRating > 10) {
                                System.out.print("Enter new rating (0-10): ");
                                if (scanner.hasNextDouble()) {
                                    newRating = scanner.nextDouble();
                                    if (newRating < 0 || newRating > 10) System.out.println("Error: Rating must be 0-10.");
                                } else {
                                    System.out.println("Error: Enter a valid number.");
                                    scanner.next();
                                }
                            }
                            scanner.nextLine();

                            sql = "UPDATE movie_tickets SET rating = ? WHERE id = ?";
                            psUpdate = con.prepareStatement(sql);
                            psUpdate.setDouble(1, newRating);
                            psUpdate.setInt(2, id);
                            break;

                        case 5:
                            System.out.println("Old availability: " + rs.getBoolean("is_available"));
                            System.out.print("New availability (true/false): ");

                            while (!scanner.hasNextBoolean()) {
                                System.out.println("Error: Type 'true' or 'false'");
                                scanner.next();
                            }
                            boolean newAvailable = scanner.nextBoolean();
                            scanner.nextLine();

                            sql = "UPDATE movie_tickets SET is_available = ? WHERE id = ?";
                            psUpdate = con.prepareStatement(sql);
                            psUpdate.setBoolean(1, newAvailable);
                            psUpdate.setInt(2, id);
                            break;

                        default:
                            System.out.println("Wrong field choice.");
                            continue;
                    }

                    if (psUpdate != null) {
                        psUpdate.executeUpdate();
                        System.out.println("Movie updated successfully!");

                        PreparedStatement psShow = con.prepareStatement("SELECT * FROM movie_tickets WHERE id = ?");
                        psShow.setInt(1, id);
                        ResultSet res = psShow.executeQuery();
                        if(res.next()) {
                            System.out.println("Updated Data: " + res.getString("title") + " | " + res.getString("genre") + " | " + res.getInt("duration") + "min" + " | " + res.getDouble("rating") + "/10" + " | " + res.getBoolean("is_available"));
                        }
                    }
                }

                else if (choice == 5) {

                    System.out.print("Enter movie IDs to delete (separated by space): ");
                    String input = scanner.nextLine();
                    String[] parts = input.split(" ");

                    int[] ids = new int[parts.length];
                    for (int i = 0; i < parts.length; i++) {
                        ids[i] = Integer.parseInt(parts[i]);
                    }

                    System.out.println("\nMovies to delete:");
                    for (int id : ids) {
                        PreparedStatement psSelect = con.prepareStatement(
                                "SELECT * FROM movie_tickets WHERE id = ?"
                        );
                        psSelect.setInt(1, id);
                        ResultSet rs = psSelect.executeQuery();
                        if (rs.next()) {
                            System.out.println(
                                    rs.getInt("id") + " | " +
                                            rs.getString("title") + " | " +
                                            rs.getString("genre") + " | " +
                                            rs.getInt("duration") + " | " +
                                            rs.getDouble("rating") + " | " +
                                            rs.getBoolean("is_available")
                            );
                        } else {
                            System.out.println(id + " - Movie not found");
                        }
                    }

                    System.out.print("\nAre you sure you want to delete these movies? (y/n): ");
                    String confirm = scanner.nextLine();

                    if (confirm.equalsIgnoreCase("y")) {
                        for (int id : ids) {
                            PreparedStatement psDelete = con.prepareStatement(
                                    "DELETE FROM movie_tickets WHERE id = ?"
                            );
                            psDelete.setInt(1, id);
                            psDelete.executeUpdate();
                        }
                        System.out.println("Selected movies deleted successfully!");
                    } else {
                        System.out.println("Delete cancelled");
                    }
                }

                else if (choice == 6) {
                    System.out.println("Exit");
                    break;
                }

                else {
                    System.out.println("Wrong option");
                }
            }

            con.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}