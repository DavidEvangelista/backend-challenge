package com.invillia.acme.domain

import com.invillia.acme.domain.enumeration.StatusPayment
import java.io.Serializable
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table

/**
 * A Payment.
 */
@Entity
@Table(name = "payment")
class Payment(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    var id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: StatusPayment? = null,

    @Column(name = "credit_card_number")
    var creditCardNumber: Int? = null,

    @Column(name = "payment_date")
    var paymentDate: LocalDate? = null,

    @ManyToOne
    var order: Order? = null

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
) : Serializable {
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Payment) return false
        if (other.id == null || id == null) return false

        return id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "Payment{" +
        "id=$id" +
        ", status='$status'" +
        ", creditCardNumber=$creditCardNumber" +
        ", paymentDate='$paymentDate'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
