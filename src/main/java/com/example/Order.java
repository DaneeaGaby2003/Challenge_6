package com.example;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {
    private long id;
    private String userId;
    private BigDecimal total;
    private LocalDateTime createdAt;

    public Order() {}

    public Order(long id, String userId, BigDecimal total, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.total = total;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public String getUserId() { return userId; }
    public BigDecimal getTotal() { return total; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(long id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
