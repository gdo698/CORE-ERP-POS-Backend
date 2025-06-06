package com.core.erp.repository;

import com.core.erp.domain.PurchaseOrderEntity;
import com.core.erp.dto.order.PurchaseOrderProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrderEntity, Long> {
    Page<PurchaseOrderEntity> findByStore_StoreIdOrderByOrderIdDesc(Integer storeId, Pageable pageable);

    // 발주 제한 repo
    boolean existsByStore_StoreIdAndOrderDateBetween(Integer storeId, LocalDateTime start, LocalDateTime end);

    @Query(value = """
SELECT
    CAST(o.order_id AS UNSIGNED) AS orderId,
    o.total_quantity AS totalQuantity,
    o.total_amount AS totalAmount,
    o.order_date AS orderDate,
    o.order_status AS orderStatus
FROM purchase_order o
WHERE o.store_id = :storeId
    AND (:orderId IS NULL OR o.order_id LIKE CONCAT('%', :orderId, '%'))
    AND (:orderStatus IS NULL OR o.order_status = :orderStatus)
    AND (
        (:startDate IS NULL OR :endDate IS NULL) 
        OR (o.order_date BETWEEN :startDate AND :endDate)
    )
ORDER BY o.order_date DESC
""",
            countQuery = "SELECT COUNT(*) FROM purchase_order o WHERE o.store_id = :storeId",
            nativeQuery = true)
    Page<PurchaseOrderProjection> searchOrderHistory(
            @Param("storeId") Integer storeId,
            @Param("orderId") String orderId,
            @Param("orderStatus") Integer orderStatus,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            Pageable pageable
    );

    // KPI: 오늘 날짜 기준, 매장의 총 발주 금액 합계
    @Query("""
SELECT COALESCE(SUM(p.totalAmount), 0)
FROM PurchaseOrderEntity p
WHERE p.store.storeId = :storeId
  AND p.orderDate BETWEEN :startDate AND :endDate
""")
    int sumTotalAmountByStoreAndPeriod(
            @Param("storeId") Integer storeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

}