package ci553.happyshop.client.customer;

import ci553.happyshop.catalogue.Order;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.*;

public class PaymentProcessor {

    public enum PaymentMethod {
        CREDIT_CARD("Credit Card"),
        DEBIT_CARD("Debit Card"),
        PAYPAL("PayPal"),
        APPLE_PAY("Apple Pay"),
        GOOGLE_PAY("Google Pay"),
        CASH("Cash on Delivery");

        private final String displayName;

        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public record PaymentResult(boolean success, String transactionId, String message, PaymentMethod paymentMethod) {
    }

    public static PaymentResult processPayment(Order order, double totalAmount) {
        Dialog<PaymentResult> dialog = new Dialog<>();
        dialog.setTitle("Payment Processing");
        dialog.setHeaderText("Complete your purchase");

        ButtonType payButton = new ButtonType("Pay Now", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(payButton, cancelButton);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER_LEFT);

        Label orderSummary = new Label(String.format("Order ID: %s\nTotal Amount: %.2f",
                order.getOrderId(), totalAmount));
        orderSummary.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label paymentLabel = new Label("Select Payment Method:");
        ComboBox<PaymentMethod> paymentMethodCombo = new ComboBox<>();
        paymentMethodCombo.getItems().addAll(PaymentMethod.values());
        paymentMethodCombo.setValue(PaymentMethod.CREDIT_CARD);

        content.getChildren().addAll(orderSummary, paymentLabel, paymentMethodCombo);

        VBox paymentDetailsPanel = new VBox(10);
        paymentDetailsPanel.setStyle("-fx-padding: 15px; -fx-border-color: #ccc; -fx-border-radius: 5px;");

        content.getChildren().add(paymentDetailsPanel);

        paymentMethodCombo.setOnAction(e -> {
            paymentDetailsPanel.getChildren().clear();
            PaymentMethod selectedMethod = paymentMethodCombo.getValue();
            paymentDetailsPanel.getChildren().add(createPaymentDetailsPanel(selectedMethod));
        });

        paymentDetailsPanel.getChildren().add(createPaymentDetailsPanel(PaymentMethod.CREDIT_CARD));

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == payButton) {
                return processPaymentTransaction(order, totalAmount, paymentMethodCombo.getValue());
            }
            return null;
        });

        Optional<PaymentResult> result = dialog.showAndWait();
        return result.orElse(new PaymentResult(false, null, "Payment cancelled", null));
    }

    // FIX: Change return type from Node to VBox to match actual return types
    private static VBox createPaymentDetailsPanel(PaymentMethod method) {
        switch (method) {
            case CREDIT_CARD:
            case DEBIT_CARD:
                return createCardPaymentPanel();

            case PAYPAL:
                return createPayPalPanel();

            case APPLE_PAY:
            case GOOGLE_PAY:
                return createMobilePaymentPanel(method);

            case CASH:
                return createCashPaymentPanel();

            default:
                return new VBox(new Label("Payment method not supported"));
        }
    }

    private static VBox createCardPaymentPanel() {
        VBox panel = new VBox(10);
        panel.setAlignment(Pos.CENTER_LEFT);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField cardNumber = new TextField();
        cardNumber.setPromptText("1234 5678 9012 3456");
        TextField cardHolder = new TextField();
        cardHolder.setPromptText("John Doe");
        TextField expiry = new TextField();
        expiry.setPromptText("MM/YY");
        TextField cvv = new TextField();
        cvv.setPromptText("123");

        grid.add(new Label("Card Number:"), 0, 0);
        grid.add(cardNumber, 1, 0);
        grid.add(new Label("Cardholder Name:"), 0, 1);
        grid.add(cardHolder, 1, 1);
        grid.add(new Label("Expiry Date:"), 0, 2);
        grid.add(expiry, 1, 2);
        grid.add(new Label("CVV:"), 0, 3);
        grid.add(cvv, 1, 3);

        panel.getChildren().add(grid);
        return panel;
    }

    private static VBox createPayPalPanel() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        TextField email = new TextField();
        email.setPromptText("your.email@example.com");
        PasswordField password = new PasswordField();
        password.setPromptText("PayPal Password");

        box.getChildren().addAll(
                new Label("PayPal Email:"),
                email,
                new Label("PayPal Password:"),
                password
        );

        return box;
    }

    private static VBox createMobilePaymentPanel(PaymentMethod method) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);

        Label instruction = new Label(String.format("You will be redirected to %s to complete payment",
                method.getDisplayName()));
        instruction.setStyle("-fx-font-size: 14px; -fx-text-alignment: center;");

        Label note = new Label("Please ensure your device supports the selected payment method");
        note.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        box.getChildren().addAll(instruction, note);
        return box;
    }

    private static VBox createCashPaymentPanel() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        Label info = new Label("Cash on Delivery Instructions:");
        info.setStyle("-fx-font-weight: bold;");

        TextArea instructions = new TextArea();
        instructions.setText("Please have exact change ready for the delivery driver.\n" +
                "Our drivers carry limited change.\n" +
                "You can pay by cash or card to the driver.");
        instructions.setEditable(false);
        instructions.setPrefRowCount(4);
        instructions.setWrapText(true);

        box.getChildren().addAll(info, instructions);
        return box;
    }

    private static PaymentResult processPaymentTransaction(Order order, double amount, PaymentMethod method) {
        try {
            Thread.sleep(2000);

            String transactionId = generateTransactionId();

            if (Math.random() < 0.1) {
                return new PaymentResult(false, null,
                        "Payment declined by bank. Please try a different payment method.", method);
            }

            String successMessage = String.format(
                    "Payment successful!\nTransaction ID: %s\nAmount: %.2f\nMethod: %s",
                    transactionId, amount, method.getDisplayName()
            );

            return new PaymentResult(true, transactionId, successMessage, method);

        } catch (InterruptedException e) {
            return new PaymentResult(false, null, "Payment processing interrupted", method);
        } catch (Exception e) {
            return new PaymentResult(false, null, "Payment processing error: " + e.getMessage(), method);
        }
    }

    private static String generateTransactionId() {
        return "TXN" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    public static void showPaymentSuccess(PaymentResult result) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Payment Successful");
        alert.setHeaderText("Thank you for your purchase!");
        alert.setContentText(result.message());
        alert.showAndWait();
    }

    public static void showPaymentFailure(PaymentResult result) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Payment Failed");
        alert.setHeaderText("Unable to process payment");
        alert.setContentText(result.message() + "\n\nPlease try a different payment method or contact your bank.");
        alert.showAndWait();
    }
}