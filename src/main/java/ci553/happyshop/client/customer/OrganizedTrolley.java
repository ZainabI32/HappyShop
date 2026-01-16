package ci553.happyshop.client.customer;

import ci553.happyshop.catalogue.Product;
import java.util.*;

public class OrganizedTrolley {
    private final Map<String, Product> organizedItems;
    private final List<Product> trolleyList;

    public OrganizedTrolley() {
        this.organizedItems = new LinkedHashMap<>();
        this.trolleyList = new ArrayList<>();
    }

    public void addProduct(Product product) {
        String productId = product.getProductId();

        if (organizedItems.containsKey(productId)) {
            Product existing = organizedItems.get(productId);
            existing.setOrderedQuantity(existing.getOrderedQuantity() + product.getOrderedQuantity());
        } else {
            Product newProduct = new Product(
                    product.getProductId(),
                    product.getProductDescription(),
                    product.getProductImageName(),
                    product.getUnitPrice(),
                    product.getStockQuantity()
            );
            newProduct.setOrderedQuantity(product.getOrderedQuantity());
            organizedItems.put(productId, newProduct);
        }

        updateTrolleyList();
    }

    public boolean removeProduct(String productId) {
        if (organizedItems.containsKey(productId)) {
            organizedItems.remove(productId);
            updateTrolleyList();
            return true;
        }
        return false;
    }

    public boolean updateQuantity(String productId, int newQuantity) {
        if (organizedItems.containsKey(productId) && newQuantity > 0) {
            organizedItems.get(productId).setOrderedQuantity(newQuantity);
            updateTrolleyList();
            return true;
        }
        return false;
    }

    public void clear() {
        organizedItems.clear();
        trolleyList.clear();
    }

    public int getTotalItemCount() {
        return organizedItems.values().stream()
                .mapToInt(Product::getOrderedQuantity)
                .sum();
    }

    public double getTotalPrice() {
        return organizedItems.values().stream()
                .mapToDouble(p -> p.getUnitPrice() * p.getOrderedQuantity())
                .sum();
    }

    public List<Product> getOrganizedTrolley() {
        return new ArrayList<>(trolleyList);
    }

    public boolean isEmpty() {
        return organizedItems.isEmpty();
    }

    public Product getProduct(String productId) {
        return organizedItems.get(productId);
    }

    private void updateTrolleyList() {
        trolleyList.clear();
        trolleyList.addAll(organizedItems.values());
        trolleyList.sort(Comparator.comparing(Product::getProductId));
    }

    public String getTrolleySummary() {
        if (isEmpty()) {
            return "Your trolley is empty";
        }

        StringBuilder summary = new StringBuilder();
        summary.append("TROLLEY SUMMARY:\n");

        for (int i = 0; i < 40; i++) {
            summary.append("=");
        }
        summary.append("\n");

        for (Product product : trolleyList) {
            summary.append(String.format("%s - %s\n",
                    product.getProductId(),
                    product.getProductDescription()));
            summary.append(String.format("   Quantity: %d @ %.2f each\n",
                    product.getOrderedQuantity(),
                    product.getUnitPrice()));
            summary.append(String.format("   Subtotal: %.2f\n\n",
                    product.getUnitPrice() * product.getOrderedQuantity()));
        }

        for (int i = 0; i < 40; i++) {
            summary.append("-");
        }
        summary.append("\n");

        summary.append(String.format("TOTAL ITEMS: %d\n", getTotalItemCount()));
        summary.append(String.format("TOTAL PRICE: %.2f\n", getTotalPrice()));

        return summary.toString();
    }
}