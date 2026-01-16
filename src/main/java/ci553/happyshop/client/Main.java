package ci553.happyshop.client;

import ci553.happyshop.client.customer.*;
import ci553.happyshop.client.emergency.EmergencyExit;
import ci553.happyshop.client.orderTracker.OrderTracker;
import ci553.happyshop.client.picker.PickerController;
import ci553.happyshop.client.picker.PickerModel;
import ci553.happyshop.client.picker.PickerView;
import ci553.happyshop.client.warehouse.*;
import ci553.happyshop.orderManagement.OrderHub;
import ci553.happyshop.storageAccess.DatabaseRW;
import ci553.happyshop.storageAccess.DatabaseRWFactory;
import javafx.application.Application;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * The Main JavaFX application class. The Main class is executable directly.
 * It serves as a foundation for UI logic and starts all the clients (UI) in one go.
 */
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    // starts the system
    @Override
    public void start(Stage window) throws IOException {
        // FIXED: Removed WelcomePage - start directly with customer interface

        // Initialize Warehouse Login System
        WarehouseLoginSystem.initialize(DatabaseRWFactory.createDatabaseRW());

        // Start Customer Clients (both regular and improved)
        startCustomerClient();
        startImprovedCustomerClient();  // FIXED: Using your existing pattern

        // Start Picker Clients
        startPickerClient();
        startPickerClient();

        // Start Order Trackers
        startOrderTracker();
        startOrderTracker();

        // Initialize Order Map
        initializeOrderMap();

        // Start Warehouse Clients (with login)
        startWarehouseClient();
        startWarehouseClient();

        // Start Emergency Exit
        startEmergencyExit();
    }

    // FIXED: Use your existing linking pattern (direct field assignment)
    private void startCustomerClient(){
        CustomerView cusView = new CustomerView();
        CustomerController cusController = new CustomerController();
        CustomerModel cusModel = new CustomerModel();
        DatabaseRW databaseRW = DatabaseRWFactory.createDatabaseRW();

        // FIXED: Use your existing pattern - direct field assignment
        cusView.cusController = cusController;
        cusController.cusModel = cusModel;
        cusModel.cusView = cusView;
        cusModel.databaseRW = databaseRW;
        cusView.start(new Stage());
    }

    // FIXED: Use your existing linking pattern (direct field assignment)
    private void startImprovedCustomerClient(){
        ImprovedCustomerView cusView = new ImprovedCustomerView();
        CustomerController cusController = new CustomerController();
        CustomerModel cusModel = new CustomerModel();
        DatabaseRW databaseRW = DatabaseRWFactory.createDatabaseRW();

        // FIXED: Use your existing pattern - direct field assignment
        cusView.cusController = cusController;
        cusController.cusModel = cusModel;
        cusModel.cusView = cusView;
        cusModel.setDatabaseRW(databaseRW);
        cusView.start(new Stage());
    }

    private void startPickerClient(){
        PickerModel pickerModel = new PickerModel();
        PickerView pickerView = new PickerView();
        PickerController pickerController = new PickerController();
        pickerView.pickerController = pickerController;
        pickerController.pickerModel = pickerModel;
        pickerModel.pickerView = pickerView;
        pickerModel.registerWithOrderHub();
        pickerView.start(new Stage());
    }

    private void startOrderTracker(){
        OrderTracker orderTracker = new OrderTracker();
        orderTracker.registerWithOrderHub();
    }

    private void initializeOrderMap(){
        OrderHub orderHub = OrderHub.getOrderHub();
        orderHub.initializeOrderMap();
    }

    private void startWarehouseClient(){
        WarehouseView view = new WarehouseView();
        WarehouseController controller = new WarehouseController();
        WarehouseModel model = new WarehouseModel();
        DatabaseRW databaseRW = DatabaseRWFactory.createDatabaseRW();

        view.controller = controller;
        controller.model = model;
        model.view = view;
        model.databaseRW = databaseRW;
        view.start(new Stage());

        HistoryWindow historyWindow = new HistoryWindow();
        AlertSimulator alertSimulator = new AlertSimulator();

        model.historyWindow = historyWindow;
        model.alertSimulator = alertSimulator;
        historyWindow.warehouseView = view;
        alertSimulator.warehouseView = view;
    }

    private void startEmergencyExit(){
        EmergencyExit.getEmergencyExit();
    }
}