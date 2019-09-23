package com.invillia.acme.service

import com.invillia.acme.domain.Payment
import com.invillia.acme.domain.enumeration.Status
import com.invillia.acme.domain.enumeration.StatusPayment
import com.invillia.acme.repository.OrderRepository
import com.invillia.acme.repository.PaymentRepository
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service Implementation for managing [Payment].
 */
@Service
@Transactional
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val orderRepository: OrderRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Save a payment.
     *
     * @param payment the entity to save.
     * @return the persisted entity.
     */
    fun save(payment: Payment): Payment {
        log.debug("Request to save Payment : {}", payment)

        val order = orderRepository.findById(payment.order?.id).get()
        if (order != null) {
            order.status = Status.PAYMENT_CONFIRMED
            order.confirmationDate = LocalDate.now()
            orderRepository.save(order)
        }
        payment.paymentDate = LocalDate.now(ZoneId.systemDefault())
        payment.status = StatusPayment.COMPLETED
        return paymentRepository.save(payment)
    }

    /**
     * Update a payment.
     *
     * @param payment the entity to save.
     * @return the persisted entity.
     */
    fun update(payment: Payment): Payment {
        log.debug("Request to update Payment : {}", payment)
        return paymentRepository.save(payment)
    }

    /**
     * Get all the payments.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    fun findAll(): MutableList<Payment> {
        log.debug("Request to get all Payments")
        return paymentRepository.findAll()
    }

    /**
     * Get one payment by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    fun findOne(id: Long): Optional<Payment> {
        log.debug("Request to get Payment : {}", id)
        return paymentRepository.findById(id)
    }

    /**
     * Delete the payment by id.
     *
     * @param id the id of the entity.
     */
    fun delete(id: Long) {
        log.debug("Request to delete Payment : {}", id)

        paymentRepository.deleteById(id)
    }

    fun findPaymentByOrderAndStatus(orderId: Long): List<Payment> {
        return paymentRepository.findAllByOrderIdAndStatus(orderId, StatusPayment.COMPLETED)
    }
}
