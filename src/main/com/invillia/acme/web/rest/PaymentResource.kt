package com.invillia.acme.web.rest

import com.invillia.acme.domain.Payment
import com.invillia.acme.service.PaymentService
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

private const val ENTITY_NAME = "backendchallengePayment"

/**
 * REST controller for managing [com.invillia.acme.domain.Payment].
 */
@RestController
@RequestMapping("/api")
class PaymentResource(
    private val paymentService: PaymentService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /payments` : Create a new payment.
     *
     * @param payment the payment to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new payment, or with status `400 (Bad Request)` if the payment has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/payments")
    fun createPayment(@RequestBody payment: Payment): ResponseEntity<Payment> {
        log.debug("REST request to save Payment : {}", payment)
        if (payment.id != null) {
            throw BadRequestAlertException(
                "A new payment cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = paymentService.save(payment)
        return ResponseEntity.created(URI("/api/payments/" + result.id))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * `PUT  /payments` : Updates an existing payment.
     *
     * @param payment the payment to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated payment,
     * or with status `400 (Bad Request)` if the payment is not valid,
     * or with status `500 (Internal Server Error)` if the payment couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/payments")
    fun updatePayment(@RequestBody payment: Payment): ResponseEntity<Payment> {
        log.debug("REST request to update Payment : {}", payment)
        if (payment.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        val result = paymentService.update(payment)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, false, ENTITY_NAME,
                     payment.id.toString()
                )
            )
            .body(result)
    }

    /**
     * `GET  /payments` : get all the payments.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of payments in body.
     */
    @GetMapping("/payments")
    fun getAllPayments(): MutableList<Payment> {
        log.debug("REST request to get all Payments")
        return paymentService.findAll()
    }

    /**
     * `GET  /payments/:id` : get the "id" payment.
     *
     * @param id the id of the payment to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the payment, or with status `404 (Not Found)`.
     */
    @GetMapping("/payments/{id}")
    fun getPayment(@PathVariable id: Long): ResponseEntity<Payment> {
        log.debug("REST request to get Payment : {}", id)
        val payment = paymentService.findOne(id)
        return ResponseUtil.wrapOrNotFound(payment)
    }

    /**
     *  `DELETE  /payments/:id` : delete the "id" payment.
     *
     * @param id the id of the payment to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/payments/{id}")
    fun deletePayment(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Payment : {}", id)
        paymentService.delete(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build()
    }
}
