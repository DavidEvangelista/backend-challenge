package com.invillia.acme.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table

/**
 * A OrderItem.
 */
@Entity
@Table(name = "order_item")
class OrderItem(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    var id: Long? = null,

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "unit_price")
    var unitPrice: Double? = null,

    @Column(name = "quantity")
    var quantity: Int? = null,

    @JsonIgnore
    @ManyToOne
    var order: Order? = null

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
) : Serializable {
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OrderItem) return false
        if (other.id == null || id == null) return false

        return id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "OrderItem{" +
        "id=$id" +
        ", description='$description'" +
        ", unitPrice=$unitPrice" +
        ", quantity=$quantity" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
