package invillia.acme.web.rest

import com.invillia.acme.InvilliaApplication
import com.invillia.acme.domain.Payment
import com.invillia.acme.domain.enumeration.StatusPayment
import com.invillia.acme.repository.PaymentRepository
import com.invillia.acme.service.PaymentService
import com.invillia.acme.web.rest.PaymentResource
import com.invillia.acme.web.rest.errors.ExceptionTranslator
import java.time.LocalDate
import java.time.ZoneId
import javax.persistence.EntityManager
import kotlin.test.assertNotNull
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.Validator

/**
 * Integration tests for the [PaymentResource] REST controller.
 *
 * @see PaymentResource
 */
@SpringBootTest(classes = [InvilliaApplication::class])
class PaymentResourceIT {

    @Autowired
    private lateinit var paymentRepository: PaymentRepository

    @Autowired
    private lateinit var paymentService: PaymentService

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var exceptionTranslator: ExceptionTranslator

    @Autowired
    private lateinit var em: EntityManager

    @Autowired
    private lateinit var validator: Validator

    private lateinit var restPaymentMockMvc: MockMvc

    private lateinit var payment: Payment

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val paymentResource = PaymentResource(paymentService)
        this.restPaymentMockMvc = MockMvcBuilders.standaloneSetup(paymentResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        payment = createEntity(em)
    }

    @Test
    @Transactional
    fun createPayment() {
        val databaseSizeBeforeCreate = paymentRepository.findAll().size

        // Create the Payment
        restPaymentMockMvc.perform(
            post("/api/payments")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(payment))
        ).andExpect(status().isCreated)

        // Validate the Payment in the database
        val paymentList = paymentRepository.findAll()
        assertThat(paymentList).hasSize(databaseSizeBeforeCreate + 1)
        val testPayment = paymentList[paymentList.size - 1]
        assertThat(testPayment.status).isEqualTo(DEFAULT_STATUS)
        assertThat(testPayment.creditCardNumber).isEqualTo(DEFAULT_CREDIT_CARD_NUMBER)
        assertThat(testPayment.paymentDate).isEqualTo(DEFAULT_PAYMENT_DATE)
    }

    @Test
    @Transactional
    fun createPaymentWithExistingId() {
        val databaseSizeBeforeCreate = paymentRepository.findAll().size

        // Create the Payment with an existing ID
        payment.id = 1L

        // An entity with an existing ID cannot be created, so this API call must fail
        restPaymentMockMvc.perform(
            post("/api/payments")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(payment))
        ).andExpect(status().isBadRequest)

        // Validate the Payment in the database
        val paymentList = paymentRepository.findAll()
        assertThat(paymentList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    fun getAllPayments() {
        // Initialize the database
        paymentRepository.saveAndFlush(payment)

        // Get all the paymentList
        restPaymentMockMvc.perform(get("/api/payments?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(payment.id?.toInt())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].creditCardNumber").value(hasItem(DEFAULT_CREDIT_CARD_NUMBER)))
            .andExpect(jsonPath("$.[*].paymentDate").value(hasItem(DEFAULT_PAYMENT_DATE.toString())))
    }

    @Test
    @Transactional
    fun getPayment() {
        // Initialize the database
        paymentRepository.saveAndFlush(payment)

        val id = payment.id
        assertNotNull(id)

        // Get the payment
        restPaymentMockMvc.perform(get("/api/payments/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(id.toInt()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.creditCardNumber").value(DEFAULT_CREDIT_CARD_NUMBER))
            .andExpect(jsonPath("$.paymentDate").value(DEFAULT_PAYMENT_DATE.toString()))
    }

    @Test
    @Transactional
    fun getNonExistingPayment() {
        // Get the payment
        restPaymentMockMvc.perform(get("/api/payments/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }

    @Test
    @Transactional
    fun updatePayment() {
        // Initialize the database
        paymentService.save(payment)

        val databaseSizeBeforeUpdate = paymentRepository.findAll().size

        // Update the payment
        val id = payment.id
        assertNotNull(id)
        val updatedPayment = paymentRepository.findById(id).get()
        // Disconnect from session so that the updates on updatedPayment are not directly saved in db
        em.detach(updatedPayment)
        updatedPayment.status = UPDATED_STATUS
        updatedPayment.creditCardNumber = UPDATED_CREDIT_CARD_NUMBER
        updatedPayment.paymentDate = UPDATED_PAYMENT_DATE

        restPaymentMockMvc.perform(
            put("/api/payments")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(updatedPayment))
        ).andExpect(status().isOk)

        // Validate the Payment in the database
        val paymentList = paymentRepository.findAll()
        assertThat(paymentList).hasSize(databaseSizeBeforeUpdate)
        val testPayment = paymentList[paymentList.size - 1]
        assertThat(testPayment.status).isEqualTo(UPDATED_STATUS)
        assertThat(testPayment.creditCardNumber).isEqualTo(UPDATED_CREDIT_CARD_NUMBER)
        assertThat(testPayment.paymentDate).isEqualTo(UPDATED_PAYMENT_DATE)
    }

    @Test
    @Transactional
    fun updateNonExistingPayment() {
        val databaseSizeBeforeUpdate = paymentRepository.findAll().size

        // Create the Payment

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPaymentMockMvc.perform(
            put("/api/payments")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(payment))
        ).andExpect(status().isBadRequest)

        // Validate the Payment in the database
        val paymentList = paymentRepository.findAll()
        assertThat(paymentList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    fun deletePayment() {
        // Initialize the database
        paymentService.save(payment)

        val databaseSizeBeforeDelete = paymentRepository.findAll().size

        val id = payment.id
        assertNotNull(id)

        // Delete the payment
        restPaymentMockMvc.perform(
            delete("/api/payments/{id}", id)
                .accept(APPLICATION_JSON_UTF8)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val paymentList = paymentRepository.findAll()
        assertThat(paymentList).hasSize(databaseSizeBeforeDelete - 1)
    }

    @Test
    @Transactional
    fun equalsVerifier() {
        equalsVerifier(Payment::class)
        val payment1 = Payment()
        payment1.id = 1L
        val payment2 = Payment()
        payment2.id = payment1.id
        assertThat(payment1).isEqualTo(payment2)
        payment2.id = 2L
        assertThat(payment1).isNotEqualTo(payment2)
        payment1.id = null
        assertThat(payment1).isNotEqualTo(payment2)
    }

    companion object {

        val DEFAULT_STATUS: StatusPayment = StatusPayment.COMPLETED
        val UPDATED_STATUS: StatusPayment = StatusPayment.CANCELED

        val DEFAULT_CREDIT_CARD_NUMBER: Int = 112312312
        val UPDATED_CREDIT_CARD_NUMBER: Int = 534512514

        val DEFAULT_PAYMENT_DATE: LocalDate = LocalDate.now(ZoneId.systemDefault())
        val UPDATED_PAYMENT_DATE: LocalDate = LocalDate.ofEpochDay(0L)

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Payment {
            val payment = Payment(
                status = DEFAULT_STATUS,
                creditCardNumber = DEFAULT_CREDIT_CARD_NUMBER,
                paymentDate = DEFAULT_PAYMENT_DATE
            )

            return payment
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Payment {
            val payment = Payment(
                status = UPDATED_STATUS,
                creditCardNumber = UPDATED_CREDIT_CARD_NUMBER,
                paymentDate = UPDATED_PAYMENT_DATE
            )

            return payment
        }
    }
}
