package com.ordercraft.dto;

import com.ordercraft.entity.CustomerOrder;
import com.ordercraft.entity.Inventory;
import java.math.BigDecimal;
import java.util.List;

public class DashboardStatsDto {
    private long totalOrders;
    private long pendingOrders;
    private long ordersInProduction;
    private long completedOrders;
    private long lowStockCount;
    private long pendingPurchaseOrders;
    private long pendingInvoices;
    private BigDecimal outstandingPaymentsAmount;
    private BigDecimal totalRevenue;
    private List<CustomerOrder> recentOrders;
    private List<Inventory> lowStockItems;

    public DashboardStatsDto() {}

    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

    public long getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; }

    public long getOrdersInProduction() { return ordersInProduction; }
    public void setOrdersInProduction(long ordersInProduction) { this.ordersInProduction = ordersInProduction; }

    public long getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(long completedOrders) { this.completedOrders = completedOrders; }

    public long getLowStockCount() { return lowStockCount; }
    public void setLowStockCount(long lowStockCount) { this.lowStockCount = lowStockCount; }

    public long getPendingPurchaseOrders() { return pendingPurchaseOrders; }
    public void setPendingPurchaseOrders(long pendingPurchaseOrders) { this.pendingPurchaseOrders = pendingPurchaseOrders; }

    public long getPendingInvoices() { return pendingInvoices; }
    public void setPendingInvoices(long pendingInvoices) { this.pendingInvoices = pendingInvoices; }

    public BigDecimal getOutstandingPaymentsAmount() { return outstandingPaymentsAmount; }
    public void setOutstandingPaymentsAmount(BigDecimal outstandingPaymentsAmount) { this.outstandingPaymentsAmount = outstandingPaymentsAmount; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public List<CustomerOrder> getRecentOrders() { return recentOrders; }
    public void setRecentOrders(List<CustomerOrder> recentOrders) { this.recentOrders = recentOrders; }

    public List<Inventory> getLowStockItems() { return lowStockItems; }
    public void setLowStockItems(List<Inventory> lowStockItems) { this.lowStockItems = lowStockItems; }
}
