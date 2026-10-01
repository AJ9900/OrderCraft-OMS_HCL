package com.ordercraft.service;

import com.ordercraft.dto.GoodsReceiptItemRequest;
import com.ordercraft.dto.GoodsReceiptRequest;
import com.ordercraft.entity.PoItem;
import com.ordercraft.entity.PoStatus;
import com.ordercraft.entity.PurchaseOrder;
import com.ordercraft.exception.BadRequestException;
import com.ordercraft.repository.GoodsReceiptRepository;
import com.ordercraft.repository.PoItemRepository;
import com.ordercraft.repository.PurchaseOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoodsReceiptServiceTest {

    @Mock private GoodsReceiptRepository goodsReceiptRepository;
    @Mock private PurchaseOrderRepository purchaseOrderRepository;
    @Mock private PoItemRepository poItemRepository;
    @Mock private InventoryService inventoryService;
    @Mock private AuditLogService auditLogService;

    @InjectMocks private GoodsReceiptService goodsReceiptService;

    @Test
    void rejectsPoItemFromAnotherPurchaseOrder() {
        PurchaseOrder selectedOrder = new PurchaseOrder();
        selectedOrder.setId(1L);
        selectedOrder.setPoNumber("PO-1");
        selectedOrder.setStatus(PoStatus.SENT);

        PurchaseOrder otherOrder = new PurchaseOrder();
        otherOrder.setId(2L);
        PoItem item = new PoItem();
        item.setId(3L);
        item.setPurchaseOrder(otherOrder);

        GoodsReceiptItemRequest receiptItem = new GoodsReceiptItemRequest();
        receiptItem.setPoItemId(3L);
        receiptItem.setReceivedQuantity(BigDecimal.ONE);
        GoodsReceiptRequest request = new GoodsReceiptRequest();
        request.setPoId(1L);
        request.setReceiptDate(LocalDate.now());
        request.setItems(List.of(receiptItem));

        when(purchaseOrderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(selectedOrder));
        when(poItemRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(item));

        assertThrows(BadRequestException.class, () -> goodsReceiptService.processGoodsReceipt(request));

        verify(inventoryService, never()).addStock(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }
}