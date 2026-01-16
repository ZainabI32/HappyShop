package ci553.happyshop.client.customer;

import ci553.happyshop.catalogue.Product;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StockShortageHandler {

    public record StockShortageResult(boolean shouldProceed, List<Product> productsToRemove,
                                      List<Product> productsToReduce) {
    }

    public static StockShortageResult showStockShortageDialog(List<Product> insufficientProducts) {
        if (insufficientProducts.isEmpty()) {
            return new StockShortageResult(true, new ArrayList<>(), new ArrayList<>());
        }

        StringBuilder message = new StringBuilder();
        message.append("STOCK SHORTAGE DETECTED!\n\n");
        message.append("The following products have insufficient stock:\n\n");

        List<Product> productsToRemove = new ArrayList<>();
        List<Product> productsToReduce = new ArrayList<>();

        for (Product product : insufficientProducts) {
            message.append(String.format("%s - %s\n",
                    product.getProductId(),
                    product.getProductDescription()));
            message.append(String.format("  Available: %d, Requested: %d\n\n",
                    product.getStockQuantity(),
                    product.getOrderedQuantity()));

            if (product.getStockQuantity() == 0) {
                productsToRemove.add(product);
            } else {
                productsToReduce.add(product);
            }
        }

        message.append("What would you like to do?\n");
        message.append("Auto-adjust quantities to available stock\n");
        message.append("Remove out-of-stock items\n");
        message.append("Cancel checkout and return to trolley\n");

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Stock Shortage");
        alert.setHeaderText("Some products are unavailable");
        alert.setContentText(message.toString());

        ButtonType adjustButton = new ButtonType("Auto-Adjust");
        ButtonType removeButton = new ButtonType("Remove Out-of-Stock");
        ButtonType cancelButton = new ButtonType("Cancel Checkout");

        alert.getButtonTypes().setAll(adjustButton, removeButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == adjustButton) {
                return new StockShortageResult(true, productsToRemove, productsToReduce);
            } else if (result.get() == removeButton) {
                return new StockShortageResult(true, productsToRemove, new ArrayList<>());
            }
        }

        return new StockShortageResult(false, new ArrayList<>(), new ArrayList<>());
    }

    public static void showSuccessNotification(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void autoAdjustQuantities(List<Product> productsToAdjust) {
        for (Product product : productsToAdjust) {
            int availableStock = product.getStockQuantity();
            if (availableStock > 0) {
                product.setOrderedQuantity(availableStock);
            }
        }
    }

    public static String createAdjustmentSummary(List<Product> removedProducts, List<Product> adjustedProducts) {
        StringBuilder summary = new StringBuilder();
        summary.append("Checkout Adjusted Successfully!\n\n");

        if (!removedProducts.isEmpty()) {
            summary.append("Removed items (out of stock):\n");
            for (Product product : removedProducts) {
                summary.append(String.format("%s - %s\n",
                        product.getProductId(),
                        product.getProductDescription()));
            }
            summary.append("\n");
        }

        if (!adjustedProducts.isEmpty()) {
            summary.append("Adjusted quantities:\n");
            for (Product product : adjustedProducts) {
                summary.append(String.format("%s - %s: Quantity set to %d\n",
                        product.getProductId(),
                        product.getProductDescription(),
                        product.getOrderedQuantity()));
            }
        }

        return summary.toString();
    }
}