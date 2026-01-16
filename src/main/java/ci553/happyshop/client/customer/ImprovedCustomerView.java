package ci553.happyshop.client.customer;

import ci553.happyshop.utility.UIStyle;
import ci553.happyshop.utility.WinPosManager;
import ci553.happyshop.utility.WindowBounds;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.effect.DropShadow;

import java.io.IOException;
import java.sql.SQLException;

public class ImprovedCustomerView extends CustomerView{
    public CustomerController cusController;

    private final int WIDTH = UIStyle.customerWinWidth + 200;
    private final int HEIGHT = UIStyle.customerWinHeight + 100;
    private final int COLUMN_WIDTH = WIDTH / 2 - 10;

    private HBox hbRoot;
    private VBox vbTrolleyPage;
    private VBox vbReceiptPage;

    TextField tfId;
    TextField tfName;
    TextField tfCategory;
    TextField tfPriceRange;

    private ImageView ivProduct;
    private Label lbProductInfo;
    private TextArea taTrolley;
    private TextArea taReceipt;
    private ListView<String> lvSearchResults;
    private ProgressBar pbStockLevel;
    private Label lbWelcomeMessage;

    private Stage viewWindow;

    public void start(Stage window) {
        VBox vbSearchPage = createEnhancedSearchPage();
        vbTrolleyPage = createEnhancedTrolleyPage();
        vbReceiptPage = createEnhancedReceiptPage();

        Line line = new Line(0, 0, 0, HEIGHT);
        line.setStrokeWidth(6);
        line.setStroke(createGradient());
        VBox lineContainer = new VBox(line);
        lineContainer.setPrefWidth(6);
        lineContainer.setAlignment(Pos.CENTER);

        hbRoot = new HBox(15, vbSearchPage, lineContainer, vbTrolleyPage);
        hbRoot.setAlignment(Pos.CENTER);
        hbRoot.setStyle(createEnhancedRootStyle());

        Scene scene = new Scene(hbRoot, WIDTH, HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/styles/enhanced-ui.css").toExternalForm());

        window.setScene(scene);
        window.setTitle("HappyShop Customer Client");
        WinPosManager.registerWindow(window, WIDTH, HEIGHT);
        window.show();
        viewWindow = window;
    }

    private VBox createEnhancedSearchPage() {
        lbWelcomeMessage = new Label("Welcome to HappyShop!");
        lbWelcomeMessage.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label laPageTitle = new Label("Advanced Product Search");
        laPageTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #34495e;");

        Label laId = new Label("Product ID:");
        laId.setStyle(UIStyle.labelStyle);
        tfId = new TextField();
        tfId.setPromptText("e.g., 0001");
        tfId.setStyle(createEnhancedTextFieldStyle());

        Label laName = new Label("Product Name:");
        laName.setStyle(UIStyle.labelStyle);
        tfName = new TextField();
        tfName.setPromptText("e.g., iPhone, Samsung");
        tfName.setStyle(createEnhancedTextFieldStyle());

        Label laCategory = new Label("Category:");
        laCategory.setStyle(UIStyle.labelStyle);
        tfCategory = new TextField();
        tfCategory.setPromptText("e.g., Electronics, Clothing");
        tfCategory.setStyle(createEnhancedTextFieldStyle());

        Label laPriceRange = new Label("Price Range ():");
        laPriceRange.setStyle(UIStyle.labelStyle);
        HBox priceBox = new HBox(10);
        TextField tfMinPrice = new TextField();
        tfMinPrice.setPromptText("Min");
        tfMinPrice.setStyle(createEnhancedTextFieldStyle() + "-fx-pref-width: 80px;");
        TextField tfMaxPrice = new TextField();
        tfMaxPrice.setPromptText("Max");
        tfMaxPrice.setStyle(createEnhancedTextFieldStyle() + "-fx-pref-width: 80px;");
        priceBox.getChildren().addAll(tfMinPrice, new Label("-"), tfMaxPrice);

        lvSearchResults = new ListView<>();
        lvSearchResults.setPrefHeight(200);
        lvSearchResults.setStyle(createEnhancedListViewStyle());

        Button btnSearch = new Button("Search Products");
        btnSearch.setStyle(createEnhancedButtonStyle("#3498db"));
        btnSearch.setOnAction(this::buttonClicked);

        Button btnClearSearch = new Button("Clear");
        btnClearSearch.setStyle(createEnhancedButtonStyle("#e74c3c"));
        btnClearSearch.setOnAction(this::buttonClicked);

        HBox searchButtons = new HBox(10, btnSearch, btnClearSearch);
        searchButtons.setAlignment(Pos.CENTER);

        ivProduct = new ImageView("imageHolder.jpg");
        ivProduct.setFitHeight(120);
        ivProduct.setFitWidth(120);
        ivProduct.setPreserveRatio(true);
        ivProduct.setSmooth(true);
        ivProduct.setEffect(new DropShadow(10, Color.GRAY));

        lbProductInfo = new Label("Search for products to start shopping!");
        lbProductInfo.setWrapText(true);
        lbProductInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50; -fx-padding: 10px;");

        pbStockLevel = new ProgressBar(0);
        pbStockLevel.setStyle("-fx-accent: #27ae60;");
        Label lbStockLabel = new Label("Stock Level:");
        lbStockLabel.setStyle(UIStyle.labelStyle);
        VBox stockBox = new VBox(5, lbStockLabel, pbStockLevel);

        Button btnAddToTrolley = new Button("Add to Trolley");
        btnAddToTrolley.setStyle(createEnhancedButtonStyle("#27ae60"));
        btnAddToTrolley.setOnAction(this::buttonClicked);

        Button btnViewDetails = new Button("View Details");
        btnViewDetails.setStyle(createEnhancedButtonStyle("#f39c12"));
        btnViewDetails.setOnAction(this::buttonClicked);

        HBox actionButtons = new HBox(10, btnAddToTrolley, btnViewDetails);
        actionButtons.setAlignment(Pos.CENTER);

        VBox searchFields = new VBox(15,
                lbWelcomeMessage,
                laPageTitle,
                createFieldBox(laId, tfId),
                createFieldBox(laName, tfName),
                createFieldBox(laCategory, tfCategory),
                createFieldBox(laPriceRange, priceBox),
                searchButtons,
                lvSearchResults
        );

        VBox productDisplay = new VBox(15,
                new HBox(20, ivProduct, lbProductInfo),
                stockBox,
                actionButtons
        );
        productDisplay.setAlignment(Pos.CENTER);

        VBox vbSearchPage = new VBox(20, searchFields, productDisplay);
        vbSearchPage.setPrefWidth(COLUMN_WIDTH);
        vbSearchPage.setAlignment(Pos.TOP_CENTER);
        vbSearchPage.setStyle("-fx-padding: 20px; -fx-background-color: #ecf0f1; -fx-background-radius: 10px;");

        return vbSearchPage;
    }

    private VBox createEnhancedTrolleyPage() {
        Label laPageTitle = new Label("Your Shopping Trolley");
        laPageTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        taTrolley = new TextArea();
        taTrolley.setEditable(false);
        taTrolley.setPrefSize(WIDTH/2, HEIGHT-100);
        taTrolley.setStyle("-fx-font-size: 14px; -fx-font-family: 'Monospace';");

        Button btnClearTrolley = new Button("Clear Trolley");
        btnClearTrolley.setStyle(createEnhancedButtonStyle("#e74c3c"));
        btnClearTrolley.setOnAction(this::buttonClicked);

        Button btnUpdateQuantity = new Button("Update Quantity");
        btnUpdateQuantity.setStyle(createEnhancedButtonStyle("#f39c12"));
        btnUpdateQuantity.setOnAction(this::buttonClicked);

        Button btnCheckout = new Button("Checkout");
        btnCheckout.setStyle(createEnhancedButtonStyle("#27ae60"));
        btnCheckout.setOnAction(this::buttonClicked);

        HBox hbBtns = new HBox(15, btnClearTrolley, btnUpdateQuantity, btnCheckout);
        hbBtns.setStyle("-fx-padding: 20px;");
        hbBtns.setAlignment(Pos.CENTER);

        Label lbTotal = new Label("Total: 0.00");
        lbTotal.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e74c3d;");

        VBox vbTrolleyPage = new VBox(20, laPageTitle, taTrolley, lbTotal, hbBtns);
        vbTrolleyPage.setPrefWidth(COLUMN_WIDTH);
        vbTrolleyPage.setAlignment(Pos.TOP_CENTER);
        vbTrolleyPage.setStyle("-fx-padding: 20px; -fx-background-color: #ffffff; -fx-background-radius: 10px;");

        return vbTrolleyPage;
    }

    private VBox createEnhancedReceiptPage() {
        Label laPageTitle = new Label("Purchase Receipt");
        laPageTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        taReceipt = new TextArea();
        taReceipt.setEditable(false);
        taReceipt.setPrefSize(WIDTH/2, HEIGHT-100);
        taReceipt.setStyle("-fx-font-size: 14px; -fx-font-family: 'Monospace';");

        Button btnCloseReceipt = new Button("OK & Continue Shopping");
        btnCloseReceipt.setStyle(createEnhancedButtonStyle("#3498db"));
        btnCloseReceipt.setOnAction(this::buttonClicked);

        Button btnPrintReceipt = new Button("Print Receipt");
        btnPrintReceipt.setStyle(createEnhancedButtonStyle("#95a5a6"));
        btnPrintReceipt.setOnAction(this::buttonClicked);

        HBox receiptButtons = new HBox(15, btnCloseReceipt, btnPrintReceipt);
        receiptButtons.setAlignment(Pos.CENTER);

        VBox vbReceiptPage = new VBox(20, laPageTitle, taReceipt, receiptButtons);
        vbReceiptPage.setPrefWidth(COLUMN_WIDTH);
        vbReceiptPage.setAlignment(Pos.TOP_CENTER);
        vbReceiptPage.setStyle("-fx-padding: 20px; -fx-background-color: #d5f4e6; -fx-background-radius: 10px;");

        return vbReceiptPage;
    }

    private HBox createFieldBox(Label label, Node field) {
        HBox box = new HBox(10, label, field);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private String createEnhancedTextFieldStyle() {
        return "-fx-font-size: 14px; -fx-padding: 8px; -fx-border-radius: 5px; " +
                "-fx-border-color: #bdc3c7; -fx-background-radius: 5px;";
    }

    private String createEnhancedButtonStyle(String color) {
        return "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px; " +
                "-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-background-radius: 5px; -fx-cursor: hand;";
    }

    private String createEnhancedListViewStyle() {
        return "-fx-font-size: 13px; -fx-padding: 5px; -fx-border-radius: 5px; " +
                "-fx-border-color: #bdc3c7; -fx-background-radius: 5px;";
    }

    private String createEnhancedRootStyle() {
        return "-fx-background-color: linear-gradient(to bottom, #ffffff, #ecf0f1); " +
                "-fx-padding: 10px; -fx-font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;";
    }

    private LinearGradient createGradient() {
        return new LinearGradient(0, 0, 0, 1, true, null,
                new Stop(0, Color.PINK),
                new Stop(1, Color.PURPLE)
        );
    }

    private void buttonClicked(ActionEvent event) {
        try {
            Button btn = (Button) event.getSource();
            String action = btn.getText();

            if (action.contains("Add to Trolley")) {
                showTrolleyOrReceiptPage(vbTrolleyPage);
            }
            if (action.contains("OK & Continue Shopping")) {
                showTrolleyOrReceiptPage(vbTrolleyPage);
            }

            cusController.doAction(action);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(String imageName, String searchResult, String trolley, String receipt,
                       Double stockLevel, java.util.List<String> searchResults) {
        ivProduct.setImage(new Image(imageName));
        lbProductInfo.setText(searchResult);
        taTrolley.setText(trolley);

        if (stockLevel != null) {
            pbStockLevel.setProgress(stockLevel);
        }

        if (searchResults != null) {
            lvSearchResults.getItems().clear();
            lvSearchResults.getItems().addAll(searchResults);
        }

        if (!receipt.equals("")) {
            showTrolleyOrReceiptPage(vbReceiptPage);
            taReceipt.setText(receipt);
        }
    }

    public void updateStockLevel(double stockLevel) {
        pbStockLevel.setProgress(stockLevel);
    }

    private void showTrolleyOrReceiptPage(Node pageToShow) {
        int lastIndex = hbRoot.getChildren().size() - 1;
        if (lastIndex >= 0) {
            hbRoot.getChildren().set(lastIndex, pageToShow);
        }
    }

    WindowBounds getWindowBounds() {
        return new WindowBounds(viewWindow.getX(), viewWindow.getY(),
                viewWindow.getWidth(), viewWindow.getHeight());
    }
}