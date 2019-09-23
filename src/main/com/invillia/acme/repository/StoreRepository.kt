package com.invillia.acme.repository

import com.invillia.acme.domain.Store
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data  repository for the [Store] entity.
 */
@Suppress("unused")
@Repository
interface StoreRepository : JpaRepository<Store, Long>
