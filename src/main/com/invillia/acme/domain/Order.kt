package com.invillia.acme.domain

import com.invillia.acme.domain.enumeration.Status
import java.io.Serializable
import java.time.LocalDate
import javax.persistence.*

/**
 * A Order.
 */
@Entity
@Table(name = "jhi_order")
class Order(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    var id: Long? = null,

    @Column(name = "address")
    var address: String? = null,

    @Column(name = "confirmation_date")
    var confirmationDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: Status? = null,

    @ManyToOne
    var store: Store? = null,

    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER)
    val orderItems: List<OrderItem>? = emptyList()

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
) : Serializable {
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Order) return false
        if (other.id == null || id == null) return false

        return id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "Order{" +
        "id=$id" +
        ", address='$address'" +
        ", confirmationDate='$confirmationDate'" +
        ", status='$status'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
