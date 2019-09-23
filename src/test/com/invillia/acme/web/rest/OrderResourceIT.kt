package invillia.acme.web.rest

import com.invillia.acme.InvilliaApplication
import com.invillia.acme.domain.Order
import com.invillia.acme.domain.OrderItem
import com.invillia.acme.domain.Payment
import com.invillia.acme.domain.enumeration.Status
import com.invillia.acme.repository.OrderRepository
import com.invillia.acme.service.OrderService
import com.invillia.acme.web.rest.OrderResource
import com.invillia.acme.web.rest.errors.ExceptionTranslator
import java.time.LocalDate
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
 * Integration tests for the [OrderResource] REST controller.
 *
 * @see OrderResource
 */
@SpringBootTest(classes = [InvilliaApplication::class])
class OrderResourceIT {

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var orderService: OrderService

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

    private lateinit var restOrderMockMvc: MockMvc

    private lateinit var order: Order

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val orderResource = OrderResource(orderService)
        this.restOrderMockMvc = MockMvcBuilders.standaloneSetup(orderResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        order = createEntity(em)
    }

    @Test
    @Transactional
    fun createOrder() {
        val databaseSizeBeforeCreate = orderRepository.findAll().size

        // Create the Order
        restOrderMockMvc.perform(
            post("/api/orders")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(order))
        ).andExpect(status().isCreated)

        // Validate the Order in the database
        val orderList = orderRepository.findAll()
        assertThat(orderList).hasSize(databaseSizeBeforeCreate + 1)
        val testOrder = orderList[orderList.size - 1]
        assertThat(testOrder.address).isEqualTo(DEFAULT_ADDRESS)
        assertThat(testOrder.confirmationDate).isEqualTo(DEFAULT_CONFIRMATION_DATE)
        assertThat(testOrder.status).isEqualTo(DEFAULT_STATUS)
    }

    @Test
    @Transactional
    fun createOrderWithExistingId() {
        val databaseSizeBeforeCreate = orderRepository.findAll().size

        // Create the Order with an existing ID
        order.id = 1L

        // An entity with an existing ID cannot be created, so this API call must fail
        restOrderMockMvc.perform(
            post("/api/orders")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(order))
        ).andExpect(status().isBadRequest)

        // Validate the Order in the database
        val orderList = orderRepository.findAll()
        assertThat(orderList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    fun getAllOrders() {
        // Initialize the database
        orderRepository.saveAndFlush(order)

        // Get all the orderList
        restOrderMockMvc.perform(get("/api/orders?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(order.id?.toInt())))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].confirmationDate").value(hasItem(DEFAULT_CONFIRMATION_DATE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
    }

    @Test
    @Transactional
    fun getOrder() {
        // Initialize the database
        orderRepository.saveAndFlush(order)

        val id = order.id
        assertNotNull(id)

        // Get the order
        restOrderMockMvc.perform(get("/api/orders/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(id.toInt()))
            .andExpect(jsonPath("$.address").value(DEFAULT_ADDRESS))
            .andExpect(jsonPath("$.confirmationDate").value(DEFAULT_CONFIRMATION_DATE.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
    }

    @Test
    @Transactional
    fun getNonExistingOrder() {
        // Get the order
        restOrderMockMvc.perform(get("/api/orders/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }

    @Test
    @Transactional
    fun updateOrder() {
        // Initialize the database
        orderService.save(order)

        val databaseSizeBeforeUpdate = orderRepository.findAll().size

        // Update the order
        val id = order.id
        assertNotNull(id)
        val updatedOrder = orderRepository.findById(id).get()
        // Disconnect from session so that the updates on updatedOrder are not directly saved in db
        em.detach(updatedOrder)
        updatedOrder.address = UPDATED_ADDRESS
        updatedOrder.confirmationDate = UPDATED_CONFIRMATION_DATE
        updatedOrder.status = UPDATED_STATUS

        restOrderMockMvc.perform(
            put("/api/orders")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(updatedOrder))
        ).andExpect(status().isOk)

        // Validate the Order in the database
        val orderList = orderRepository.findAll()
        assertThat(orderList).hasSize(databaseSizeBeforeUpdate)
        val testOrder = orderList[orderList.size - 1]
        assertThat(testOrder.address).isEqualTo(UPDATED_ADDRESS)
        assertThat(testOrder.confirmationDate).isEqualTo(UPDATED_CONFIRMATION_DATE)
        assertThat(testOrder.status).isEqualTo(UPDATED_STATUS)
    }

    @Test
    @Transactional
    fun updateNonExistingOrder() {
        val databaseSizeBeforeUpdate = orderRepository.findAll().size

        // Create the Order

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restOrderMockMvc.perform(
            put("/api/orders")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(order))
        ).andExpect(status().isBadRequest)

        // Validate the Order in the database
        val orderList = orderRepository.findAll()
        assertThat(orderList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    fun deleteOrder() {
        // Initialize the database
        orderService.save(order)

        val databaseSizeBeforeDelete = orderRepository.findAll().size

        val id = order.id
        assertNotNull(id)

        // Delete the order
        restOrderMockMvc.perform(
            delete("/api/orders/{id}", id)
                .accept(APPLICATION_JSON_UTF8)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val orderList = orderRepository.findAll()
        assertThat(orderList).hasSize(databaseSizeBeforeDelete - 1)
    }

    @Test
    @Transactional
    fun refoundOrder() {
        // Initialize the database
        orderService.save(order)
    }

    @Test
    @Transactional
    fun equalsVerifier() {
        equalsVerifier(Order::class)
        val order1 = Order()
        order1.id = 1L
        val order2 = Order()
        order2.id = order1.id
        assertThat(order1).isEqualTo(order2)
        order2.id = 2L
        assertThat(order1).isNotEqualTo(order2)
        order1.id = null
        assertThat(order1).isNotEqualTo(order2)
    }

    companion object {

        private const val DEFAULT_ADDRESS: String = "Rua 1 Lote 4 Bloco B, 70000-123"
        private const val UPDATED_ADDRESS = "CCSW 1 Lote 4 Bloco B, 70000-123"

        private val DEFAULT_CONFIRMATION_DATE: LocalDate = LocalDate.now()
        private val UPDATED_CONFIRMATION_DATE: LocalDate = LocalDate.ofEpochDay(1L)
        private val SMALLER_CONFIRMATION_DATE: LocalDate = LocalDate.ofEpochDay(-1L)

        private val DEFAULT_STATUS: Status = Status.NEW
        private val UPDATED_STATUS: Status = Status.PAYMENT_CONFIRMED

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Order {
            val order = Order(
                address = DEFAULT_ADDRESS,
                confirmationDate = DEFAULT_CONFIRMATION_DATE,
                status = DEFAULT_STATUS
            )

            var payment = Payment(
                status = PaymentResourceIT.DEFAULT_STATUS,
                creditCardNumber = PaymentResourceIT.DEFAULT_CREDIT_CARD_NUMBER,
                paymentDate = PaymentResourceIT.DEFAULT_PAYMENT_DATE
            )

            val orderItem = OrderItem(
                description = OrderItemResourceIT.DEFAULT_DESCRIPTION,
                unitPrice = OrderItemResourceIT.DEFAULT_UNIT_PRICE,
                quantity = OrderItemResourceIT.DEFAULT_QUANTITY
            )

            payment.order = order

            return order
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Order {
            val order = Order(
                address = UPDATED_ADDRESS,
                confirmationDate = UPDATED_CONFIRMATION_DATE,
                status = UPDATED_STATUS
            )

            return order
        }
    }
}
