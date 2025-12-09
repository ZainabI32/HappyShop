package ci553.happyshop.client;

import ci553.happyshop.client.customer.*;
import ci553.happyshop.client.emergency.EmergencyExit;
import ci553.happyshop.client.login.LoginController;
import ci553.happyshop.client.login.LoginModel;
import ci553.happyshop.client.login.LoginView;
import ci553.happyshop.client.orderTracker.OrderTracker;
import ci553.happyshop.client.picker.PickerController;
import ci553.happyshop.client.picker.PickerModel;
import ci553.happyshop.client.picker.PickerView;
import ci553.happyshop.client.warehouse.*;
import ci553.happyshop.orderManagement.OrderHub;
import ci553.happyshop.storageAccess.DatabaseRW;
import ci553.happyshop.storageAccess.DatabaseRWFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main JavaFX application class.
 * Displays a login screen first. After successful login,
 * all HappyShop clients (Customer, Picker, Warehouse, OrderTracker)
 * start up automatically.
 */
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage loginStage) throws IOException {

        // --- SETUP LOGIN ---
        LoginModel loginModel = new LoginModel();
        LoginController loginController = new LoginController(loginModel);
        LoginView loginView = new LoginView();

        // Set behaviour when login succeeds
        loginController.setOnLoginSuccess(() -> {

            // Ensure GUI-related actions run on FX thread
            Platform.runLater(() -> {
                startCustomerClient();
                startPickerClient();
                startOrderTracker();
                initializeOrderMap();
                startWarehouseClient();
                startEmergencyExit();
            });
        });

        // Show login window
        loginView.start(loginStage, loginController);
    }


    // -----------------------
    //   CLIENT LAUNCHERS
    // -----------------------

    /** Customer GUI */
    private void startCustomerClient() {
        CustomerView view = new CustomerView();
        CustomerController controller = new CustomerController();
        CustomerModel model = new CustomerModel();
        DatabaseRW databaseRW = DatabaseRWFactory.createDatabaseRW();

        view.cusController = controller;
        controller.cusModel = model;
        model.cusView = view;
        model.databaseRW = databaseRW;

        view.start(new Stage());
    }

    /** Picker GUI */
    private void startPickerClient() {
        PickerModel model = new PickerModel();
        PickerView view = new PickerView();
        PickerController controller = new PickerController();

        view.pickerController = controller;
        controller.pickerModel = model;
        model.pickerView = view;

        model.registerWithOrderHub();
        view.start(new Stage());
    }

    /** OrderTracker Client (no window, listens to Hub) */
    private void startOrderTracker() {
        OrderTracker tracker = new OrderTracker();
        tracker.registerWithOrderHub();
    }

    /** Initialize order map for OrderHub */
    private void initializeOrderMap() {
        OrderHub.getOrderHub().initializeOrderMap();
    }

    /** Warehouse GUI */
    private void startWarehouseClient() {
        WarehouseView view = new WarehouseView();
        WarehouseController controller = new WarehouseController();
        WarehouseModel model = new WarehouseModel();
        DatabaseRW databaseRW = DatabaseRWFactory.createDatabaseRW();

        view.controller = controller;
        controller.model = model;
        model.view = view;
        model.databaseRW = databaseRW;

        view.start(new Stage());

        // Additional windows
        HistoryWindow historyWindow = new HistoryWindow();
        AlertSimulator alertSimulator = new AlertSimulator();

        model.historyWindow = historyWindow;
        model.alertSimulator = alertSimulator;

        historyWindow.warehouseView = view;
        alertSimulator.warehouseView = view;
    }

    /** EmergencyExit GUI (Singleton) */
    private void startEmergencyExit() {
        EmergencyExit.getEmergencyExit();
    }
}
