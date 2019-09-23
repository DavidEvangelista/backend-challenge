package com.invillia.acme.repository

import com.invillia.acme.domain.Payment
import com.invillia.acme.domain.enumeration.StatusPayment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data  repository for the [Payment] entity.
 */
@Suppress("unused")
@Repository
interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findAllByOrderIdAndStatus(orderId: Long, statusPayment: StatusPayment): List<Payment>
}
