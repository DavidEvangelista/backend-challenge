package com.invillia.acme.web.rest

import com.invillia.acme.domain.Store
import com.invillia.acme.service.StoreService
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

private const val ENTITY_NAME = "backendchallengeStore"

/**
 * REST controller for managing [com.invillia.acme.domain.Store].
 */
@RestController
@RequestMapping("/api")
class StoreResource(
    private val storeService: StoreService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /stores` : Create a new store.
     *
     * @param store the store to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new store, or with status `400 (Bad Request)` if the store has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/stores")
    fun createStore(@RequestBody store: Store): ResponseEntity<Store> {
        log.debug("REST request to save Store : {}", store)
        if (store.id != null) {
            throw BadRequestAlertException(
                "A new store cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = storeService.save(store)
        return ResponseEntity.created(URI("/api/stores/" + result.id))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * `PUT  /stores` : Updates an existing store.
     *
     * @param store the store to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated store,
     * or with status `400 (Bad Request)` if the store is not valid,
     * or with status `500 (Internal Server Error)` if the store couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/stores")
    fun updateStore(@RequestBody store: Store): ResponseEntity<Store> {
        log.debug("REST request to update Store : {}", store)
        if (store.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        val result = storeService.save(store)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, false, ENTITY_NAME,
                     store.id.toString()
                )
            )
            .body(result)
    }

    /**
     * `GET  /stores` : get all the stores.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of stores in body.
     */
    @GetMapping("/stores")
    fun getAllStores(): MutableList<Store> {
        log.debug("REST request to get all Stores")
        return storeService.findAll()
    }

    /**
     * `GET  /stores/:id` : get the "id" store.
     *
     * @param id the id of the store to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the store, or with status `404 (Not Found)`.
     */
    @GetMapping("/stores/{id}")
    fun getStore(@PathVariable id: Long): ResponseEntity<Store> {
        log.debug("REST request to get Store : {}", id)
        val store = storeService.findOne(id)
        return ResponseUtil.wrapOrNotFound(store)
    }

    /**
     *  `DELETE  /stores/:id` : delete the "id" store.
     *
     * @param id the id of the store to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/stores/{id}")
    fun deleteStore(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Store : {}", id)
        storeService.delete(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build()
    }
}
