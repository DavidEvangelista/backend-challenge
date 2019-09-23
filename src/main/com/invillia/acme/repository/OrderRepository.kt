package com.invillia.acme.repository

import com.invillia.acme.domain.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data  repository for the [Order] entity.
 */
@Suppress("unused")
@Repository
interface OrderRepository : JpaRepository<Order, Long>
