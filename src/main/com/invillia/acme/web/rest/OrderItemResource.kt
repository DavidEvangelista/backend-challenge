package com.invillia.acme.web.rest

import com.invillia.acme.domain.OrderItem
import com.invillia.acme.service.OrderItemService
import com.invillia.acme.web.rest.errors.BadRequestAlertException
import io.github.jhipster.web.util.HeaderUtil
import io.github.jhipster.web.util.ResponseUtil
import java.net.URI
import java.net.URISyntaxException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private const val ENTITY_NAME = "backendchallengeOrderItem"

/**
 * REST controller for managing [com.invillia.acme.domain.OrderItem].
 */
@RestController
@RequestMapping("/api")
class OrderItemResource(
    private val orderItemService: OrderItemService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /order-items` : Create a new orderItem.
     *
     * @param orderItem the orderItem to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new orderItem, or with status `400 (Bad Request)` if the orderItem has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/order-items")
    fun createOrderItem(@RequestBody orderItem: OrderItem): ResponseEntity<OrderItem> {
        log.debug("REST request to save OrderItem : {}", orderItem)
        if (orderItem.id != null) {
            throw BadRequestAlertException(
                "A new orderItem cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = orderItemService.save(orderItem)
        return ResponseEntity.created(URI("/api/order-items/" + result.id))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * `PUT  /order-items` : Updates an existing orderItem.
     *
     * @param orderItem the orderItem to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated orderItem,
     * or with status `400 (Bad Request)` if the orderItem is not valid,
     * or with status `500 (Internal Server Error)` if the orderItem couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/order-items")
    fun updateOrderItem(@RequestBody orderItem: OrderItem): ResponseEntity<OrderItem> {
        log.debug("REST request to update OrderItem : {}", orderItem)
        if (orderItem.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        val result = orderItemService.save(orderItem)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, false, ENTITY_NAME,
                     orderItem.id.toString()
                )
            )
            .body(result)
    }

    /**
     * `GET  /order-items` : get all the orderItems.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of orderItems in body.
     */
    @GetMapping("/order-items")
    fun getAllOrderItems(): MutableList<OrderItem> {
        log.debug("REST request to get all OrderItems")
        return orderItemService.findAll()
    }

    /**
     * `GET  /order-items/:id` : get the "id" orderItem.
     *
     * @param id the id of the orderItem to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the orderItem, or with status `404 (Not Found)`.
     */
    @GetMapping("/order-items/{id}")
    fun getOrderItem(@PathVariable id: Long): ResponseEntity<OrderItem> {
        log.debug("REST request to get OrderItem : {}", id)
        val orderItem = orderItemService.findOne(id)
        return ResponseUtil.wrapOrNotFound(orderItem)
    }

    /**
     *  `DELETE  /order-items/:id` : delete the "id" orderItem.
     *
     * @param id the id of the orderItem to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/order-items/{id}")
    fun deleteOrderItem(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete OrderItem : {}", id)
        orderItemService.delete(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build()
    }
}
