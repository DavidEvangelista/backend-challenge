package com.invillia.acme.service

import com.invillia.acme.domain.Order
import com.invillia.acme.domain.OrderItem
import com.invillia.acme.domain.Payment
import com.invillia.acme.domain.enumeration.Status
import com.invillia.acme.domain.enumeration.StatusPayment
import com.invillia.acme.repository.OrderRepository
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service Implementation for managing [Order].
 */
@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
    private val paymentService: PaymentService,
    private val orderItemService: OrderItemService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Save a order.
     *
     * @param order the entity to save.
     * @return the persisted entity.
     */
    fun save(order: Order): Order {
        log.debug("Request to save Order : {}", order)
        var newOrder = orderRepository.save(order)
        newOrder.orderItems?.forEach {
            orderItem: OrderItem ->
                orderItem?.order = newOrder
                orderItemService.save(orderItem)
        }
        return newOrder
    }

    /**
     * Save a order.
     *
     * @param order the entity to save.
     * @return the persisted entity.
     */
    fun update(order: Order): Order {
        log.debug("Request to save Order : {}", order)
        return orderRepository.save(order)
    }

    /**
     * Get all the orders.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    fun findAll(): MutableList<Order> {
        log.debug("Request to get all Orders")
        return orderRepository.findAll()
    }

    /**
     * Get one order by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    fun findOne(id: Long): Optional<Order> {
        log.debug("Request to get Order : {}", id)
        return orderRepository.findById(id)
    }

    /**
     * Delete the order by id.
     *
     * @param id the id of the entity.
     */
    fun delete(id: Long) {
        log.debug("Request to delete Order : {}", id)

        orderRepository.deleteById(id)
    }

    /**
     * Refunded the order by id.
     *
     * @param id the id of the entity.
     */
    fun refunded(id: Long) {
        val payments = paymentService.findPaymentByOrderAndStatus(id)
        payments?.forEach {
            payment: Payment -> checkRefund(payment)
        }
    }

    /**
     *
     * check if payment date has exceeded 10 days
     * @param payment entity.
     */
    private fun checkRefund(payment: Payment)  {
        val days = ChronoUnit.DAYS.between(payment.paymentDate, LocalDate.now())
        if (days < 10) {
            payment.status = StatusPayment.CANCELED
            paymentService.update(payment)

            val order = payment.order
            if(order != null) {
                order.status = Status.REFAUNDED
                update(order)
            }
        } else {
            throw Exception("Order cannot be canceled as the 10 day limit has passed")
        }
    }
}
