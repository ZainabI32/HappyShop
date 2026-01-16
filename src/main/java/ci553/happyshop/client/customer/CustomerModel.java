package ci553.happyshop.client.customer;

import ci553.happyshop.catalogue.Product;
import ci553.happyshop.catalogue.Order;
import ci553.happyshop.storageAccess.DatabaseRW;
import ci553.happyshop.orderManagement.OrderHub;
import ci553.happyshop.utility.StorageLocation;
import ci553.happyshop.utility.ProductListFormatter;
import ci553.happyshop.client.customer.OrganizedTrolley;
import ci553.happyshop.client.customer.StockShortageHandler;
import ci553.happyshop.client.customer.FlexibleSearchEngine;
import ci553.happyshop.client.customer.PaymentProcessor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class CustomerModel {
    public CustomerView cusView;
    public DatabaseRW databaseRW;

    // Enhanced components
    protected OrganizedTrolley organizedTrolley;
    protected FlexibleSearchEngine searchEngine;
    private Product theProduct = null;
    private final ArrayList<Product> trolley = new ArrayList<>();

    // UI display elements - made public for controller access
    public String imageName = "imageHolder.jpg";
    public String displayLaSearchResult = "No Product was searched yet";
    public String displayTaTrolley = "";
    public String displayTaReceipt = "";

    // Constructor to initialize enhanced components
    public CustomerModel() {
        this.organizedTrolley = new OrganizedTrolley();
    }

    // Set databaseRW and initialize search engine
    public void setDatabaseRW(DatabaseRW databaseRW) {
        this.databaseRW = databaseRW;
        this.searchEngine = new FlexibleSearchEngine(databaseRW);
    }

    // ========================================================================
    // FLEXIBLE SEARCH METHODS - Complete Implementation
    // ========================================================================

    // BASIC SEARCH: By ID using FlexibleSearchEngine
    public void searchById(String productId) throws SQLException {
        if (productId != null && !productId.trim().isEmpty()) {
            FlexibleSearchEngine.SearchCriteria.Builder builder = new FlexibleSearchEngine.SearchCriteria.Builder();
            builder.productId(productId.trim());
            builder.searchType(FlexibleSearchEngine.SearchType.EXACT_ID);

            List<Product> results = searchEngine.search(builder.build());
            handleSearchResults(results, "ID: " + productId);
        } else {
            theProduct = null;
            displayLaSearchResult = "Please enter a Product ID";
        }
        updateView();
    }

    // BASIC SEARCH: By name using FlexibleSearchEngine
    public void searchByName(String productName) throws SQLException {
        if (productName != null && !productName.trim().isEmpty()) {
            FlexibleSearchEngine.SearchCriteria.Builder builder = new FlexibleSearchEngine.SearchCriteria.Builder();
            builder.productName(productName.trim());
            builder.searchType(FlexibleSearchEngine.SearchType.PARTIAL_NAME);

            List<Product> results = searchEngine.search(builder.build());
            handleSearchResults(results, "Name: " + productName);
        } else {
            theProduct = null;
            displayLaSearchResult = "Please enter a Product Name";
        }
        updateView();
    }

    // ADVANCED SEARCH: Multi-criteria search using FlexibleSearchEngine
    public void performAdvancedSearch(String productId, String productName, String category,
                                      Double minPrice, Double maxPrice, boolean inStockOnly) throws SQLException {

        FlexibleSearchEngine.SearchCriteria.Builder builder = new FlexibleSearchEngine.SearchCriteria.Builder();

        if (productId != null && !productId.trim().isEmpty()) {
            builder.productId(productId.trim());
        }

        if (productName != null && !productName.trim().isEmpty()) {
            builder.productName(productName.trim());
        }

        if (category != null && !category.trim().isEmpty()) {
            builder.category(category.trim());
        }

        if (minPrice != null || maxPrice != null) {
            builder.priceRange(minPrice, maxPrice);
        }

        if (inStockOnly) {
            builder.inStockOnly(true);
        }

        builder.searchType(FlexibleSearchEngine.SearchType.COMBINED);

        List<Product> results = searchEngine.search(builder.build());
        String searchCriteria = buildSearchCriteriaString(productId, productName, category, minPrice, maxPrice, inStockOnly);
        handleSearchResults(results, searchCriteria);
        updateView();
    }

    // QUICK SEARCH: By name using FlexibleSearchEngine
    public void quickSearchByName(String name) throws SQLException {
        if (name != null && !name.trim().isEmpty()) {
            List<Product> results = searchEngine.quickSearchByName(name);
            handleSearchResults(results, "name: " + name);
        }
    }

    // QUICK SEARCH: By price range using FlexibleSearchEngine
    public void quickSearchByPriceRange(double min, double max) throws SQLException {
        List<Product> results = searchEngine.quickSearchByPriceRange(min, max);
        handleSearchResults(results, String.format("price range: £%.2f - £%.2f", min, max));
    }

    // QUICK SEARCH: In-stock items using FlexibleSearchEngine
    public void quickSearchInStock() throws SQLException {
        List<Product> results = searchEngine.quickSearchInStock();
        handleSearchResults(results, "in-stock items");
    }

    // HELPER: Handle search results from FlexibleSearchEngine
    private void handleSearchResults(List<Product> results, String searchCriteria) {
        if (results.isEmpty()) {
            theProduct = null;
            displayLaSearchResult = String.format("No products found for %s", searchCriteria);
            System.out.println("No products found for: " + searchCriteria);
        } else if (results.size() == 1) {
            theProduct = results.get(0);
            if (theProduct.getStockQuantity() > 0) {
                displayLaSearchResult = formatProductInfo(theProduct);
                System.out.println("Single product found: " + theProduct.getProductId());
            } else {
                theProduct = null;
                displayLaSearchResult = "Product found but out of stock";
                System.out.println("Product out of stock: " + results.get(0).getProductId());
            }
        } else {
            theProduct = results.get(0);
            displayLaSearchResult = String.format("Found %d products for %s. Showing first:\n%s",
                    results.size(), searchCriteria, formatProductInfo(theProduct));
            System.out.println("Multiple products found: " + results.size());
        }
    }

    // HELPER: Format product information for display
    private String formatProductInfo(Product product) {
        return String.format("Product ID: %s\n%s\nPrice: £%.2f\nStock: %d units",
                product.getProductId(),
                product.getProductDescription(),
                product.getUnitPrice(),
                product.getStockQuantity());
    }

    // HELPER: Build search criteria description string
    private String buildSearchCriteriaString(String productId, String productName, String category,
                                             Double minPrice, Double maxPrice, boolean inStockOnly) {
        StringBuilder criteria = new StringBuilder();
        if (productId != null && !productId.isEmpty()) criteria.append("ID:").append(productId).append(" ");
        if (productName != null && !productName.isEmpty()) criteria.append("Name:").append(productName).append(" ");
        if (category != null && !category.isEmpty()) criteria.append("Category:").append(category).append(" ");
        if (minPrice != null) criteria.append("Min:£").append(minPrice).append(" ");
        if (maxPrice != null) criteria.append("Max:£").append(maxPrice).append(" ");
        if (inStockOnly) criteria.append("(In Stock Only)");
        return criteria.toString().trim();
    }

    // SEARCH ENHANCEMENT FEATURES
    public List<String> getSearchSuggestions(String partialInput) {
        return searchEngine.getSearchSuggestions(partialInput);
    }

    public List<String> getRecentSearches() {
        return searchEngine.getRecentSearches();
    }

    public List<String> getPopularSearches() {
        return searchEngine.getPopularSearches();
    }

    public List<Product> getLastSearchResults() {
        return searchEngine.getLastSearchResults();
    }

    // ========================================================================
    // TROLLEY & CHECKOUT METHODS (with OrganizedTrolley)
    // ========================================================================

    // Enhanced addToTrolley using OrganizedTrolley
    void addToTrolley() {
        if (theProduct != null) {
            theProduct.setOrderedQuantity(1);
            organizedTrolley.addProduct(theProduct);
            displayTaTrolley = organizedTrolley.getTrolleySummary();
            displayLaSearchResult = "Product added to trolley";
            updateView();
        }
    }

    // Enhanced checkout with payment processing and stock handling
    void checkOut() throws IOException, SQLException {
        if (!organizedTrolley.isEmpty()) {
            ArrayList<Product> trolleyItems = new ArrayList<>(organizedTrolley.getOrganizedTrolley());
            ArrayList<Product> insufficientProducts = databaseRW.purchaseStocks(trolleyItems);

            if (insufficientProducts.isEmpty()) {
                // All stock available - proceed with payment
                OrderHub orderHub = OrderHub.getOrderHub();
                Order theOrder = orderHub.newOrder(trolleyItems);

                // Process payment
                double totalAmount = organizedTrolley.getTotalPrice();
                PaymentProcessor.PaymentResult paymentResult = PaymentProcessor.processPayment(theOrder, totalAmount);

                if (paymentResult.isSuccess()) {
                    // Payment successful - clear trolley and show receipt
                    organizedTrolley.clear();
                    displayTaTrolley = "";
                    displayTaReceipt = String.format(
                            "ORDER CONFIRMED\n" +
                                    "Transaction ID: %s\n" +
                                    "Payment Method: %s\n" +
                                    "Order ID: %s\n" +
                                    "Ordered Date/Time: %s\n" +
                                    "Total Paid: £%.2f\n\n" +
                                    "Items Purchased:\n%s",
                            paymentResult.getTransactionId(),
                            paymentResult.getPaymentMethod().getDisplayName(),
                            theOrder.getOrderId(),
                            theOrder.getOrderedDateTime(),
                            totalAmount,
                            ProductListFormatter.buildString(theOrder.getProductList())
                    );
                    PaymentProcessor.showPaymentSuccess(paymentResult);
                } else {
                    // Payment failed - show error message
                    displayLaSearchResult = "Payment failed. Please try again.";
                    PaymentProcessor.showPaymentFailure(paymentResult);
                }
            } else {
                // Stock shortage - handle gracefully
                StockShortageHandler.StockShortageResult result =
                        StockShortageHandler.showStockShortageDialog(insufficientProducts);

                if (result.shouldProceed()) {
                    // Auto-adjust quantities
                    StockShortageHandler.autoAdjustQuantities(result.getProductsToReduce());

                    // Remove out-of-stock items
                    for (Product product : result.getProductsToRemove()) {
                        organizedTrolley.removeProduct(product.getProductId());
                    }

                    // Update trolley display
                    displayTaTrolley = organizedTrolley.getTrolleySummary();

                    String adjustmentMessage = StockShortageHandler.createAdjustmentSummary(
                            result.getProductsToRemove(),
                            result.getProductsToReduce()
                    );
                    StockShortageHandler.showSuccessNotification(adjustmentMessage);
                } else {
                    displayLaSearchResult = "Checkout cancelled. Please review your trolley.";
                }
            }
        } else {
            displayTaTrolley = "Your trolley is empty";
            System.out.println("Your trolley is empty");
        }
        updateView();
    }

    // Trolley management methods
    void cancel() {
        organizedTrolley.clear();
        displayTaTrolley = organizedTrolley.getTrolleySummary();
        displayTaReceipt = "";
        updateView();
    }

    void closeReceipt() {
        displayTaReceipt = "";
    }

    void updateView() {
        if (theProduct != null) {
            imageName = theProduct.getProductImageName();
            String relativeImageUrl = StorageLocation.imageFolder + imageName;
            Path imageFullPath = Paths.get(relativeImageUrl).toAbsolutePath();
            imageName = imageFullPath.toUri().toString();
        } else {
            imageName = "imageHolder.jpg";
        }
        cusView.update(imageName, displayLaSearchResult, displayTaTrolley, displayTaReceipt);
    }

    // Getter methods
    public ArrayList<Product> getTrolley() {
        return new ArrayList<>(organizedTrolley.getOrganizedTrolley());
    }

    public OrganizedTrolley getOrganizedTrolley() {
        return organizedTrolley;
    }
}