package com.example;

public class Product {

    private String id;
    private String name;
    private String descr;
    private String imageUrl;
    private double price;
    private int stock;

    // Oferta (pueden ser null si no hay)
    private Double promoPrice;
    private String validUntil; // yyyy-MM-dd

    public Product() {}

    public Product(String id, String name, String descr, String imageUrl, double price, int stock) {
        this.id = id;
        this.name = name;
        this.descr = descr;
        this.imageUrl = imageUrl;
        this.price = price;
        this.stock = stock;
    }

    // ===== getters/setters base =====
    public String getId() { return id; }
    public void setId(String id) { this.id = (id == null) ? null : id.trim(); }

    public String getName() { return name; }
    public void setName(String name) { this.name = (name == null) ? null : name.trim(); }

    public String getDescr() { return descr; }
    public void setDescr(String descr) { this.descr = descr; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    // ===== oferta =====
    public Double getPromoPrice() { return promoPrice; }
    public void setPromoPrice(Double promoPrice) { this.promoPrice = promoPrice; }

    public String getValidUntil() { return validUntil; }
    public void setValidUntil(String validUntil) { this.validUntil = validUntil; }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", descr='" + descr + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", promoPrice=" + promoPrice +
                ", validUntil='" + validUntil + '\'' +
                '}';
    }
}
