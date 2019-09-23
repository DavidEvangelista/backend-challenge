package com.invillia.acme.service

import com.invillia.acme.domain.Store
import com.invillia.acme.repository.StoreRepository
import java.util.Optional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service Implementation for managing [Store].
 */
@Service
@Transactional
class StoreService(
    private val storeRepository: StoreRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Save a store.
     *
     * @param store the entity to save.
     * @return the persisted entity.
     */
    fun save(store: Store): Store {
        log.debug("Request to save Store : {}", store)
        return storeRepository.save(store)
    }

    /**
     * Get all the stores.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    fun findAll(): MutableList<Store> {
        log.debug("Request to get all Stores")
        return storeRepository.findAll()
    }

    /**
     * Get one store by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    fun findOne(id: Long): Optional<Store> {
        log.debug("Request to get Store : {}", id)
        return storeRepository.findById(id)
    }

    /**
     * Delete the store by id.
     *
     * @param id the id of the entity.
     */
    fun delete(id: Long) {
        log.debug("Request to delete Store : {}", id)

        storeRepository.deleteById(id)
    }
}
