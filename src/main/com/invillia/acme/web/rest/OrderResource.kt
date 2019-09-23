package com.invillia.acme.web.rest

import com.invillia.acme.domain.Order
import com.invillia.acme.service.OrderService
import com.invillia.acme.web.rest.errors.BadRequestAlertException
import io.github.jhipster.web.util.HeaderUtil
import io.github.jhipster.web.util.ResponseUtil
import java.lang.Exception
import java.net.URI
import java.net.URISyntaxException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.reflect.UndeclaredThrowableException

private const val ENTITY_NAME = "backendchallengeOrder"

/**
 * REST controller for managing [com.invillia.acme.domain.Order].
 */
@RestController
@RequestMapping("/api")
class OrderResource(
    private val orderService: OrderService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /orders` : Create a new order.
     *
     * @param order the order to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new order, or with status `400 (Bad Request)` if the order has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/orders")
    fun createOrder(@RequestBody order: Order): ResponseEntity<Order> {
        log.debug("REST request to save Order : {}", order)
        if (order.id != null) {
            throw BadRequestAlertException(
                "A new order cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = orderService.save(order)
        return ResponseEntity.created(URI("/api/orders/" + result.id))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * `PUT  /orders` : Updates an existing order.
     *
     * @param order the order to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated order,
     * or with status `400 (Bad Request)` if the order is not valid,
     * or with status `500 (Internal Server Error)` if the order couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/orders")
    fun updateOrder(@RequestBody order: Order): ResponseEntity<Order> {
        log.debug("REST request to update Order : {}", order)
        if (order.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        val result = orderService.save(order)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, false, ENTITY_NAME,
                     order.id.toString()
                )
            )
            .body(result)
    }

    /**
     * `GET  /orders` : get all the orders.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of orders in body.
     */
    @GetMapping("/orders")
    fun getAllOrders(): MutableList<Order> {
        log.debug("REST request to get all Orders")
        return orderService.findAll()
    }

    /**
     * `GET  /orders/:id` : get the "id" order.
     *
     * @param id the id of the order to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the order, or with status `404 (Not Found)`.
     */
    @GetMapping("/orders/{id}")
    fun getOrder(@PathVariable id: Long): ResponseEntity<Order> {
        log.debug("REST request to get Order : {}", id)
        val order = orderService.findOne(id)
        return ResponseUtil.wrapOrNotFound(order)
    }

    /**
     *  `DELETE  /orders/:id` : delete the "id" order.
     *
     * @param id the id of the order to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/orders/{id}")
    fun deleteOrder(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Order : {}", id)
        orderService.delete(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build()
    }

    /**
     * `GET  /orders/:id` : get the "id" order.
     *
     * @param id the id of the order to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the order, or with status 417, (`Expectation Failed`).
     */
    @GetMapping("/orders/refunded/{id}")
    fun refundedOrder(@PathVariable id: Long): ResponseEntity<String?> {
        log.debug("REST request to get Order : {}", id)
        try {
            orderService.refunded(id)
            return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, id.toString())).build()
        } catch (e: UndeclaredThrowableException) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("{'error': '${e.undeclaredThrowable.message}'}")
        }
    }
}
