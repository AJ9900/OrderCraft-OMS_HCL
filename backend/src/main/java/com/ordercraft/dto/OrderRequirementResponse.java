package com.ordercraft.dto;

import com.ordercraft.entity.OrderStatus;
import java.util.List;

public class OrderRequirementResponse {
    private Long orderId;
    private String orderNumber;
    private String customerName;
    private OrderStatus orderStatus;
    private List<MaterialRequirementDto> requirements;
    private boolean hasAnyShortage;
    private String summaryMessage;

    public OrderRequirementResponse() {}

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public OrderStatus getOrderStatus() { return orderStatus; }
    public void setOrderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; }

    public List<MaterialRequirementDto> getRequirements() { return requirements; }
    public void setRequirements(List<MaterialRequirementDto> requirements) { this.requirements = requirements; }

    public boolean isHasAnyShortage() { return hasAnyShortage; }
    public void setHasAnyShortage(boolean hasAnyShortage) { this.hasAnyShortage = hasAnyShortage; }

    public String getSummaryMessage() { return summaryMessage; }
    public void setSummaryMessage(String summaryMessage) { this.summaryMessage = summaryMessage; }
}
