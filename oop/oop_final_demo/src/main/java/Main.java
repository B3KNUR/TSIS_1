import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        AuthService authService = new AuthService();
        Admin admin = new Admin();
        Librarian librarian = new Librarian();
        Student student = new Student();

        while (true) {
            if (!authService.isLoggedIn()) {
                System.out.println("\nSelect your role:");
                System.out.println("1. Student");
                System.out.println("2. Admin");
                System.out.println("3. Teacher");
                System.out.println("4. Librarian");
                System.out.println("5. Exit");
                System.out.print("Enter choice: ");
                int choice = 0;

                try {
                    choice = scanner.nextInt();
                    scanner.nextLine();
                } catch (Exception e) {
                    System.out.println("Invalid input. Please enter a number between 1 and 5.");
                    scanner.nextLine();
                    continue;
                }

                String chosenRole;
                switch (choice) {
                    case 1 -> chosenRole = "Student";
                    case 2 -> chosenRole = "Admin";
                    case 3 -> chosenRole = "Teacher";
                    case 4 -> chosenRole = "Librarian";
                    case 5 -> {
                        System.out.println("Exiting...");
                        scanner.close();
                        DBConnection.closeConnection();
                        return;
                    }
                    default -> {
                        System.out.println("Invalid choice. Try again.");
                        continue;
                    }
                }

                System.out.print("Username: ");
                String username = scanner.nextLine();

                System.out.print("Password: ");
                String password = scanner.nextLine();

                if (authService.login(username, password, chosenRole)) {
                    System.out.println("Login successful as " + chosenRole + ".");
                } else {
                    System.out.println("Invalid credentials or role. Try again.");
                }
            } else {
                User currentUser = authService.getLoggedInUser();
                String role = currentUser.getRole();

                System.out.println("\nWelcome " + currentUser.getUsername() + " (" + role + ")!");

                if (role.equalsIgnoreCase("Student")) {
                    studentMenu(scanner, librarian, student, authService, currentUser);
                }
                else if (role.equalsIgnoreCase("Admin")) {
                    adminMenu(scanner, admin, authService);
                }
                else if (role.equalsIgnoreCase("Teacher")) {
                    teacherMenu(scanner, authService);
                }
                else if (role.equalsIgnoreCase("Librarian")) {
                    librarianMenu(scanner, librarian, authService);
                }
                else {
                    System.out.println("Unknown role. Logging out for security.");
                    authService.logout();
                }
            }
        }
    }


    private static void studentMenu(Scanner scanner, Librarian librarian, Student student, AuthService authService, User currentUser) {
        System.out.println("1. View Available Books");
        System.out.println("2. Borrow Book");
        System.out.println("3. Return Book");
        System.out.println("4. View Borrowed Books");
        System.out.println("5. Logout");
        System.out.print("Enter choice: ");
        int choice = 0;

        try {
            choice = scanner.nextInt();
            scanner.nextLine();
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter a number between 1 and 5.");
            scanner.nextLine();
            return;
        }

        switch (choice) {
            case 1 -> {
                List<Book> availableBooks = librarian.getAllBooks();
                System.out.println("\nAvailable Books:");
                boolean anyAvailable = false;
                for (Book book : availableBooks) {
                    if (book.isAvailable()) {
                        System.out.println(book);
                        anyAvailable = true;
                    }
                }
                if (!anyAvailable) {
                    System.out.println("No books are currently available.");
                }
            }
            case 2 -> {
                System.out.print("Enter code of the book to borrow: ");
                String code = scanner.nextLine();
                student.borrowBook(currentUser, code);
            }
            case 3 -> {
                System.out.print("Enter code of the book to return: ");
                String code = scanner.nextLine();
                student.returnBook(currentUser, code);
            }
            case 4 -> {
                List<BorrowedBook> borrowedBooks = student.getBorrowedBooks(currentUser);
                System.out.println("\nYour Borrowed Books:");
                if (borrowedBooks.isEmpty()) {
                    System.out.println("You have not borrowed any books.");
                } else {
                    for (BorrowedBook bb : borrowedBooks) {
                        System.out.println(bb);
                    }
                }
            }
            case 5 -> {
                authService.logout();
                System.out.println("Logged out successfully.");
            }
            default -> System.out.println("Invalid choice.");
        }
    }


    private static void adminMenu(Scanner scanner, Admin admin, AuthService authService) {
        System.out.println("1. Add Users");
        System.out.println("2. Remove User");
        System.out.println("3. View All Users");
        System.out.println("4. Logout");
        System.out.print("Enter choice: ");
        int choice = 0;

        try {
            choice = scanner.nextInt();
            scanner.nextLine();
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter a number between 1 and 4.");
            scanner.nextLine();
            return;
        }

        switch (choice) {
            case 1 -> {
                System.out.print("Enter number of users to add: ");
                int numUsers = 0;
                try {
                    numUsers = scanner.nextInt();
                    scanner.nextLine();
                } catch (Exception e) {
                    System.out.println("Invalid number. Operation cancelled.");
                    scanner.nextLine();
                    break;
                }

                List<User> newUsers = new ArrayList<>();

                for (int i = 0; i < numUsers; i++) {
                    System.out.println("Enter details for user " + (i + 1) + ":");
                    System.out.print("Username: ");
                    String newUsername = scanner.nextLine();
                    System.out.print("Password: ");
                    String newPassword = scanner.nextLine();
                    System.out.print("Role (Student/Admin/Teacher/Librarian): ");
                    String newRole = scanner.nextLine();


                    if (!isValidRole(newRole)) {
                        System.out.println("Invalid role. Skipping user " + newUsername + ".");
                        continue;
                    }

                    try {
                        newUsers.add(new User(newUsername, newPassword, newRole));
                    } catch (IllegalArgumentException e) {
                        System.out.println("Error creating user: " + e.getMessage() + ". Skipping user " + newUsername + ".");
                    }
                }

                if (!newUsers.isEmpty()) {
                    admin.addUser(newUsers);
                } else {
                    System.out.println("No valid users to add.");
                }
            }
            case 2 -> {
                System.out.print("Enter username of user to remove: ");
                String removeUsername = scanner.nextLine();

                User userToRemove = getUserByUsername(admin.getUsers(), removeUsername);
                if (userToRemove != null) {
                    admin.removeUser(userToRemove);
                } else {
                    System.out.println("User not found.");
                }
            }
            case 3 -> {
                List<User> users = admin.getUsers();
                if (users.isEmpty()) {
                    System.out.println("No users found.");
                } else {
                    System.out.println("List of users:");
                    for (User user : users) {
                        System.out.println("- " + user.getUsername() + " (" + user.getRole() + ")");
                    }
                }
            }
            case 4 -> {
                authService.logout();
                System.out.println("Logged out successfully.");
            }
            default -> System.out.println("Invalid choice.");
        }
    }


    private static void teacherMenu(Scanner scanner, AuthService authService) {
        System.out.println("1. View My Courses");
        System.out.println("2. Enter Grades");
        System.out.println("3. Logout");
        System.out.print("Enter choice: ");
        int choice = 0;


        try {
            choice = scanner.nextInt();
            scanner.nextLine();
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter a number between 1 and 3.");
            scanner.nextLine();
            return;
        }

        switch (choice) {
            case 1 -> System.out.println("Teacher viewing courses...");
            case 2 -> System.out.println("Teacher entering grades...");
            case 3 -> {
                authService.logout();
                System.out.println("Logged out successfully.");
            }
            default -> System.out.println("Invalid choice.");
        }
    }


    private static void librarianMenu(Scanner scanner, Librarian librarian, AuthService authService) {
        System.out.println("1. Add Book");
        System.out.println("2. Remove Book");
        System.out.println("3. View All Books");
        System.out.println("4. Search Books");
        System.out.println("5. View Borrowed Books");
        System.out.println("6. Logout");
        System.out.print("Enter choice: ");
        int choice = 0;


        try {
            choice = scanner.nextInt();
            scanner.nextLine();
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter a number between 1 and 6.");
            scanner.nextLine();
            return;
        }

        switch (choice) {
            case 1 -> {
                System.out.print("Enter number of books to add: ");
                int numBooks = 0;
                try {
                    numBooks = scanner.nextInt();
                    scanner.nextLine();
                } catch (Exception e) {
                    System.out.println("Invalid number. Operation cancelled.");
                    scanner.nextLine();
                    break;
                }

                List<Book> newBooks = new ArrayList<>();

                for (int i = 0; i < numBooks; i++) {
                    System.out.println("Enter details for book " + (i + 1) + ":");
                    System.out.print("Title: ");
                    String title = scanner.nextLine();
                    System.out.print("Author: ");
                    String author = scanner.nextLine();
                    System.out.print("Code: ");
                    String code = scanner.nextLine();


                    if (title.isEmpty() || author.isEmpty() || code.isEmpty()) {
                        System.out.println("Invalid input. Skipping this book.");
                        continue;
                    }

                    try {
                        newBooks.add(new Book(title, author, code));
                    } catch (IllegalArgumentException e) {
                        System.out.println("Error creating book: " + e.getMessage() + ". Skipping this book.");
                    }
                }

                if (!newBooks.isEmpty()) {
                    librarian.addBooks(newBooks);
                } else {
                    System.out.println("No valid books to add.");
                }
            }
            case 2 -> {
                System.out.print("Enter code of the book to remove: ");
                String code = scanner.nextLine();
                librarian.removeBook(code);
            }
            case 3 -> {
                List<Book> allBooks = librarian.getAllBooks();
                System.out.println("\nAll Books:");
                if (allBooks.isEmpty()) {
                    System.out.println("No books found in the library.");
                } else {
                    for (Book book : allBooks) {
                        System.out.println(book);
                    }
                }
            }
            case 4 -> {
                System.out.print("Enter keyword to search (title, author, or code): ");
                String keyword = scanner.nextLine();
                List<Book> searchResults = librarian.searchBooks(keyword);
                System.out.println("\nSearch Results:");
                if (searchResults.isEmpty()) {
                    System.out.println("No books match your search.");
                } else {
                    for (Book book : searchResults) {
                        System.out.println(book);
                    }
                }
            }
            case 5 -> {
                List<BorrowedBook> borrowedBooks = librarian.viewBorrowedBooks();
                System.out.println("\nBorrowed Books:");
                if (borrowedBooks.isEmpty()) {
                    System.out.println("No books are currently borrowed.");
                } else {
                    for (BorrowedBook bb : borrowedBooks) {
                        System.out.println(bb);
                    }
                }
            }
            case 6 -> {
                authService.logout();
                System.out.println("Logged out successfully.");
            }
            default -> System.out.println("Invalid choice.");
        }
    }


    private static boolean isValidRole(String role) {
        return role.equalsIgnoreCase("Student") ||
                role.equalsIgnoreCase("Admin") ||
                role.equalsIgnoreCase("Teacher") ||
                role.equalsIgnoreCase("Librarian");
    }

    private static User getUserByUsername(List<User> users, String username) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }
}