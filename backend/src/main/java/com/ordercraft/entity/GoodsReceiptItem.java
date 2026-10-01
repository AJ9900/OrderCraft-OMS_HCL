package com.ordercraft.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "goods_receipt_items")
public class GoodsReceiptItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_receipt_id", nullable = false)
    private GoodsReceipt goodsReceipt;

    @NotNull(message = "PO item is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "po_item_id", nullable = false)
    private PoItem poItem;

    @NotNull(message = "Received quantity is required")
    @DecimalMin(value = "0.01", message = "Received quantity must be greater than 0")
    @Column(name = "received_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal receivedQuantity;

    public GoodsReceiptItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public GoodsReceipt getGoodsReceipt() { return goodsReceipt; }
    public void setGoodsReceipt(GoodsReceipt goodsReceipt) { this.goodsReceipt = goodsReceipt; }

    public PoItem getPoItem() { return poItem; }
    public void setPoItem(PoItem poItem) { this.poItem = poItem; }

    public BigDecimal getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(BigDecimal receivedQuantity) { this.receivedQuantity = receivedQuantity; }
}
