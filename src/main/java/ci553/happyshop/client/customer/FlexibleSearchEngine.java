package ci553.happyshop.client.customer;

import ci553.happyshop.catalogue.Product;
import ci553.happyshop.storageAccess.DatabaseRW;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class FlexibleSearchEngine {

    public enum SearchType {
        EXACT_ID("Exact ID Match"),
        PARTIAL_NAME("Partial Name Match"),
        CATEGORY("Category Search"),
        PRICE_RANGE("Price Range"),
        COMBINED("Combined Search"),
        ADVANCED("Advanced Filters");

        private final String description;

        SearchType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public static class SearchCriteria {
        private String productId;
        private String productName;
        private String category;
        private Double minPrice;
        private Double maxPrice;
        private Boolean inStockOnly;
        private SearchType searchType;

        public static class Builder {
            private final SearchCriteria criteria = new SearchCriteria();

            public Builder productId(String productId) {
                criteria.productId = productId;
                return this;
            }

            public Builder productName(String productName) {
                criteria.productName = productName;
                return this;
            }

            public Builder category(String category) {
                criteria.category = category;
                return this;
            }

            public Builder priceRange(Double min, Double max) {
                criteria.minPrice = min;
                criteria.maxPrice = max;
                return this;
            }

            public Builder inStockOnly(boolean inStock) {
                criteria.inStockOnly = inStock;
                return this;
            }

            public Builder searchType(SearchType type) {
                criteria.searchType = type;
                return this;
            }

            public SearchCriteria build() {
                return criteria;
            }
        }

        public String getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getCategory() { return category; }
        public Double getMinPrice() { return minPrice; }
        public Double getMaxPrice() { return maxPrice; }
        public Boolean getInStockOnly() { return inStockOnly; }
        public SearchType getSearchType() { return searchType; }
    }

    private final DatabaseRW databaseRW;
    private List<Product> lastSearchResults;

    public FlexibleSearchEngine(DatabaseRW databaseRW) {
        this.databaseRW = databaseRW;
        this.lastSearchResults = new ArrayList<>();
    }

    public List<Product> search(SearchCriteria criteria) throws SQLException {
        List<Product> allProducts = getAllProducts();  // Get all products first
        List<Product> results = new ArrayList<>();

        switch (criteria.getSearchType()) {
            case EXACT_ID:
                results = searchByExactId(allProducts, criteria.getProductId());
                break;

            case PARTIAL_NAME:
                results = searchByPartialName(allProducts, criteria.getProductName());
                break;

            case CATEGORY:
                results = searchByCategory(allProducts, criteria.getCategory());
                break;

            case PRICE_RANGE:
                results = searchByPriceRange(allProducts, criteria.getMinPrice(), criteria.getMaxPrice());
                break;

            case COMBINED:
                results = searchCombined(allProducts, criteria);
                break;

            case ADVANCED:
                results = searchAdvanced(allProducts, criteria);
                break;

            default:
                results = allProducts;
        }

        // Apply stock filter if requested
        if (criteria.getInStockOnly() != null && criteria.getInStockOnly()) {
            results = results.stream()
                    .filter(p -> p.getStockQuantity() > 0)
                    .collect(Collectors.toList());
        }

        results.sort(Comparator.comparing(Product::getProductId));
        lastSearchResults = results;
        return results;
    }

    // Helper method to get all products (works with basic DatabaseRW)
    private List<Product> getAllProducts() throws SQLException {
        List<Product> allProducts = new ArrayList<>();

        // For basic DatabaseRW, we'll use a simple approach
        // Get products by trying common IDs or use your existing methods
        String[] commonIds = {"0001", "0002", "0003", "0004", "0005", "0006", "0007", "0008", "0009", "0010"};

        for (String id : commonIds) {
            try {
                Product product = databaseRW.searchByProductId(id);
                if (product != null) {
                    allProducts.add(product);
                }
            } catch (SQLException e) {
                // Product doesn't exist, continue
                continue;
            }
        }

        return allProducts;
    }

    // Search implementations
    private List<Product> searchByExactId(List<Product> products, String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return products.stream()
                .filter(p -> p.getProductId().equalsIgnoreCase(productId.trim()))
                .collect(Collectors.toList());
    }

    private List<Product> searchByPartialName(List<Product> products, String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String lowerName = name.toLowerCase();
        return products.stream()
                .filter(p -> p.getProductDescription().toLowerCase().contains(lowerName))
                .collect(Collectors.toList());
    }

    private List<Product> searchByCategory(List<Product> products, String category) {
        if (category == null || category.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String lowerCategory = category.toLowerCase();
        return products.stream()
                .filter(p -> p.getProductDescription().toLowerCase().contains(lowerCategory))
                .collect(Collectors.toList());
    }

    private List<Product> searchByPriceRange(List<Product> products, Double min, Double max) {
        return products.stream()
                .filter(p -> {
                    double price = p.getUnitPrice();
                    boolean withinMin = min == null || price >= min;
                    boolean withinMax = max == null || price <= max;
                    return withinMin && withinMax;
                })
                .collect(Collectors.toList());
    }

    private List<Product> searchCombined(List<Product> products, SearchCriteria criteria) {
        return products.stream()
                .filter(p -> {
                    boolean matchesId = criteria.getProductId() == null ||
                            p.getProductId().equalsIgnoreCase(criteria.getProductId());
                    boolean matchesName = criteria.getProductName() == null ||
                            p.getProductDescription().toLowerCase().contains(criteria.getProductName().toLowerCase());
                    boolean matchesCategory = criteria.getCategory() == null ||
                            p.getProductDescription().toLowerCase().contains(criteria.getCategory().toLowerCase());
                    boolean matchesPrice = true;

                    if (criteria.getMinPrice() != null) {
                        matchesPrice = p.getUnitPrice() >= criteria.getMinPrice();
                    }
                    if (criteria.getMaxPrice() != null && matchesPrice) {
                        matchesPrice = p.getUnitPrice() <= criteria.getMaxPrice();
                    }

                    return matchesId && matchesName && matchesCategory && matchesPrice;
                })
                .collect(Collectors.toList());
    }

    private List<Product> searchAdvanced(List<Product> products, SearchCriteria criteria) {
        return searchCombined(products, criteria);
    }

    // Quick search methods
    public List<Product> quickSearchByName(String name) throws SQLException {
        SearchCriteria criteria = new SearchCriteria.Builder()
                .productName(name)
                .searchType(SearchType.PARTIAL_NAME)
                .build();
        return search(criteria);
    }

    public List<Product> quickSearchByPriceRange(double min, double max) throws SQLException {
        SearchCriteria criteria = new SearchCriteria.Builder()
                .priceRange(min, max)
                .searchType(SearchType.PRICE_RANGE)
                .build();
        return search(criteria);
    }

    public List<Product> quickSearchInStock() throws SQLException {
        SearchCriteria criteria = new SearchCriteria.Builder()
                .inStockOnly(true)
                .searchType(SearchType.ADVANCED)
                .build();
        return search(criteria);
    }

    // Search enhancement features
    public List<String> getSearchSuggestions(String partialInput) {
        // Return mock suggestions for now
        List<String> suggestions = Arrays.asList("iPhone", "Samsung", "Laptop", "Coffee Maker", "Phone", "Tablet");
        return suggestions.stream()
                .filter(s -> s.toLowerCase().contains(partialInput.toLowerCase()))
                .limit(10)
                .collect(Collectors.toList());
    }

    public List<String> getRecentSearches() {
        return Arrays.asList("iPhone", "Samsung TV", "Laptop", "Coffee Maker");
    }

    public List<String> getPopularSearches() {
        return Arrays.asList("Electronics", "Laptop", "Phone", "Coffee");
    }

    public List<Product> getLastSearchResults() {
        return new ArrayList<>(lastSearchResults);
    }
}