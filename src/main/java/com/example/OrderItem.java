package com.example;

import java.math.BigDecimal;

public class OrderItem {
    private long id;
    private long orderId;
    private String productId;
    private int qty;
    private BigDecimal price;

    // Campos “de vista” para la plantilla (join con products)
    private String productName;        // opcional: nombre del producto
    private BigDecimal subtotal;       // price * qty

    public OrderItem() {}

    public OrderItem(long id, long orderId, String productId, int qty, BigDecimal price) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.qty = qty;
        this.price = price;
        this.subtotal = price.multiply(new BigDecimal(qty));
    }

    // Getters/Setters
    public long getId() { return id; }
    public long getOrderId() { return orderId; }
    public String getProductId() { return productId; }
    public int getQty() { return qty; }
    public BigDecimal getPrice() { return price; }
    public String getProductName() { return productName; }
    public BigDecimal getSubtotal() { return subtotal; }

    public void setId(long id) { this.id = id; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
    public void setProductId(String productId) { this.productId = productId; }
    public void setQty(int qty) { this.qty = qty; this.subtotal = price != null ? price.multiply(new BigDecimal(qty)) : null; }
    public void setPrice(BigDecimal price) { this.price = price; this.subtotal = price != null ? price.multiply(new BigDecimal(qty)) : null; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
