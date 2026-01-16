package ci553.happyshop.client.customer;

import java.io.IOException;
import java.sql.SQLException;

public class CustomerController {
    public CustomerModel cusModel;
    public CustomerView cusView;

    public void setCustomerModel(CustomerModel cusModel) {
        this.cusModel = cusModel;
    }

    public void setCustomerView(CustomerView cusView) {
        this.cusView = cusView;
    }

    public void doAction(String action) throws SQLException, IOException {
        switch (action) {
            case "Search":
                if (cusView != null && cusModel != null) {
                    String productId = cusView.tfId.getText().trim();
                    String productName = cusView.tfName.getText().trim();

                    if (!productId.isEmpty()) {
                        cusModel.searchById(productId);
                    } else if (!productName.isEmpty()) {
                        cusModel.searchByName(productName);
                    } else {
                        cusModel.displayLaSearchResult = "Please enter Product ID or Name";
                        cusModel.updateView();
                    }
                }
                break;

            case "Advanced Search":
                if (cusView != null && cusModel != null) {
                    String advProductId = cusView.tfId.getText().trim();
                    String advProductName = cusView.tfName.getText().trim();

                    // Check if it's ImprovedCustomerView for advanced fields
                    String category = "";
                    String minPrice = "";
                    String maxPrice = "";

                    if (cusView instanceof ImprovedCustomerView improvedView) {
                        category = improvedView.tfCategory.getText().trim();
                        minPrice = improvedView.tfMinPrice.getText().trim();
                        maxPrice = improvedView.tfMaxPrice.getText().trim();
                    }

                    Double min = minPrice.isEmpty() ? null : Double.parseDouble(minPrice);
                    Double max = maxPrice.isEmpty() ? null : Double.parseDouble(maxPrice);

                    cusModel.performAdvancedSearch(advProductId, advProductName, category, min, max, false);
                }
                break;

            case "Search In Stock":
                if (cusModel != null) {
                    cusModel.quickSearchInStock();
                }
                break;

            case "Add to Trolley":
                if (cusModel != null) {
                    cusModel.addToTrolley();
                }
                break;

            case "Checkout":
                if (cusModel != null) {
                    cusModel.checkOut();
                }
                break;

            case "Cancel":
                if (cusModel != null) {
                    cusModel.cancel();
                }
                break;

            case "OK & Close":
                if (cusModel != null) {
                    cusModel.closeReceipt();
                }
                break;

            default:
                System.out.println("Unknown action: " + action);
                break;
        }
    }
}