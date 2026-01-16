package ci553.happyshop.client.warehouse;

import ci553.happyshop.storageAccess.DatabaseRW;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.util.Optional;

public class WarehouseLoginSystem {

    public record WarehouseWorker(String workerId, String name, String role, boolean isActive) {

        public boolean canAccessStockManagement() {
                return isActive && (role.equals("MANAGER") || role.equals("WORKER"));
            }

            public boolean canAccessReports() {
                return isActive && role.equals("MANAGER");
            }
        }

    private static WarehouseWorker currentWorker;
    private static DatabaseRW databaseRW;

    public static void initialize(DatabaseRW database) {
        databaseRW = database;
    }

    /**
     * Shows login dialog and authenticates warehouse worker
     */
    public static boolean showLoginDialog() {
        Dialog<WarehouseWorker> dialog = new Dialog<>();
        dialog.setTitle("Warehouse Worker Login");
        dialog.setHeaderText("Please enter your credentials");

        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, cancelButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Worker ID");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        grid.add(new Label("Worker ID:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return authenticateWorker(username.getText(), password.getText());
            }
            return null;
        });

        Optional<WarehouseWorker> result = dialog.showAndWait();

        if (result.isPresent() && result.get() != null) {
            currentWorker = result.get();
            showWelcomeMessage();
            return true;
        }

        return false;
    }

    /**
     * Authenticates worker credentials (simplified for demo)
     */
    private static WarehouseWorker authenticateWorker(String workerId, String password) {
        if (workerId == null || workerId.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            showError("Invalid Input", "Please enter both Worker ID and Password");
            return null;
        }

        // Simplified authentication - replace with real database check
        if (workerId.equals("ADMIN") && password.equals("admin123")) {
            return new WarehouseWorker("ADMIN", "System Administrator", "MANAGER", true);
        } else if (workerId.equals("WORKER001") && password.equals("worker123")) {
            return new WarehouseWorker("WORKER001", "John Smith", "WORKER", true);
        } else if (workerId.equals("WORKER002") && password.equals("worker456")) {
            return new WarehouseWorker("WORKER002", "Jane Doe", "WORKER", true);
        } else if (workerId.equals("MANAGER001") && password.equals("manager123")) {
            return new WarehouseWorker("MANAGER001", "Bob Johnson", "MANAGER", true);
        } else {
            showError("Authentication Failed", "Invalid Worker ID or Password");
            return null;
        }
    }

    /**
     * Shows welcome message after successful login
     */
    private static void showWelcomeMessage() {
        if (currentWorker != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Login Successful");
            alert.setHeaderText("Welcome, " + currentWorker.name() + "!");
            alert.setContentText(String.format("Role: %s\nAccess Level: %s\n\nYou are now logged into the warehouse system.",
                    currentWorker.role(),
                    getAccessLevelDescription()));
            alert.showAndWait();
        }
    }

    /**
     * Gets current logged-in worker
     */
    public static WarehouseWorker getCurrentWorker() {
        return currentWorker;
    }

    /**
     * Checks if worker is logged in
     */
    public static boolean isWorkerLoggedIn() {
        return currentWorker != null;
    }

    /**
     * Checks if current worker can perform action
     */
    public static boolean canPerformAction(String action) {
        if (!isWorkerLoggedIn()) {
            return false;
        }

        switch (action.toUpperCase()) {
            case "VIEW_STOCK":
                return currentWorker.canAccessStockManagement();
            case "UPDATE_STOCK":
                return currentWorker.canAccessStockManagement();
            case "GENERATE_REPORTS":
                return currentWorker.canAccessReports();
            case "ADD_NEW_PRODUCT":
                return currentWorker.canAccessReports();
            default:
                return false;
        }
    }

    /**
     * Logs out current worker
     */
    public static void logout() {
        if (currentWorker != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Logout");
            alert.setHeaderText("Goodbye, " + currentWorker.name());
            alert.setContentText("You have been successfully logged out.");
            alert.showAndWait();

            currentWorker = null;
        }
    }

    /**
     * Shows access denied message
     */
    public static void showAccessDenied(String action) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Access Denied");
        alert.setHeaderText("Insufficient Permissions");
        alert.setContentText(String.format("You do not have permission to: %s\n\nRequired role: %s",
                action,
                getRequiredRoleForAction(action)));
        alert.showAndWait();
    }

    /**
     * Gets access level description
     */
    private static String getAccessLevelDescription() {
        if (currentWorker == null) return "None";

        switch (currentWorker.role()) {
            case "MANAGER":
                return "Full Access";
            case "WORKER":
                return "Limited Access";
            default:
                return "Basic Access";
        }
    }

    /**
     * Gets required role for action
     */
    private static String getRequiredRoleForAction(String action) {
        switch (action.toUpperCase()) {
            case "GENERATE_REPORTS":
            case "ADD_NEW_PRODUCT":
                return "Manager";
            case "VIEW_STOCK":
            case "UPDATE_STOCK":
                return "Worker or Manager";
            default:
                return "Unknown";
        }
    }

    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}