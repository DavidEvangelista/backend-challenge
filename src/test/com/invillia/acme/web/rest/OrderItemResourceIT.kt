package invillia.acme.web.rest

import com.invillia.acme.InvilliaApplication
import com.invillia.acme.domain.OrderItem
import com.invillia.acme.repository.OrderItemRepository
import com.invillia.acme.service.OrderItemService
import com.invillia.acme.web.rest.OrderItemResource
import com.invillia.acme.web.rest.errors.ExceptionTranslator
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
 * Integration tests for the [OrderItemResource] REST controller.
 *
 * @see OrderItemResource
 */
@SpringBootTest(classes = [InvilliaApplication::class])
class OrderItemResourceIT {

    @Autowired
    private lateinit var orderItemRepository: OrderItemRepository

    @Autowired
    private lateinit var orderItemService: OrderItemService

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

    private lateinit var restOrderItemMockMvc: MockMvc

    private lateinit var orderItem: OrderItem

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val orderItemResource = OrderItemResource(orderItemService)
        this.restOrderItemMockMvc = MockMvcBuilders.standaloneSetup(orderItemResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        orderItem = createEntity(em)
    }

    @Test
    @Transactional
    fun createOrderItem() {
        val databaseSizeBeforeCreate = orderItemRepository.findAll().size

        // Create the OrderItem
        restOrderItemMockMvc.perform(
            post("/api/order-items")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(orderItem))
        ).andExpect(status().isCreated)

        // Validate the OrderItem in the database
        val orderItemList = orderItemRepository.findAll()
        assertThat(orderItemList).hasSize(databaseSizeBeforeCreate + 1)
        val testOrderItem = orderItemList[orderItemList.size - 1]
        assertThat(testOrderItem.description).isEqualTo(DEFAULT_DESCRIPTION)
        assertThat(testOrderItem.unitPrice).isEqualTo(DEFAULT_UNIT_PRICE)
        assertThat(testOrderItem.quantity).isEqualTo(DEFAULT_QUANTITY)
    }

    @Test
    @Transactional
    fun createOrderItemWithExistingId() {
        val databaseSizeBeforeCreate = orderItemRepository.findAll().size

        // Create the OrderItem with an existing ID
        orderItem.id = 1L

        // An entity with an existing ID cannot be created, so this API call must fail
        restOrderItemMockMvc.perform(
            post("/api/order-items")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(orderItem))
        ).andExpect(status().isBadRequest)

        // Validate the OrderItem in the database
        val orderItemList = orderItemRepository.findAll()
        assertThat(orderItemList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    fun getAllOrderItems() {
        // Initialize the database
        orderItemRepository.saveAndFlush(orderItem)

        // Get all the orderItemList
        restOrderItemMockMvc.perform(get("/api/order-items?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(orderItem.id?.toInt())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].unitPrice").value(hasItem(DEFAULT_UNIT_PRICE)))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY)))
    }

    @Test
    @Transactional
    fun getOrderItem() {
        // Initialize the database
        orderItemRepository.saveAndFlush(orderItem)

        val id = orderItem.id
        assertNotNull(id)

        // Get the orderItem
        restOrderItemMockMvc.perform(get("/api/order-items/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(id.toInt()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.unitPrice").value(DEFAULT_UNIT_PRICE))
            .andExpect(jsonPath("$.quantity").value(DEFAULT_QUANTITY))
    }

    @Test
    @Transactional
    fun getNonExistingOrderItem() {
        // Get the orderItem
        restOrderItemMockMvc.perform(get("/api/order-items/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }

    @Test
    @Transactional
    fun updateOrderItem() {
        // Initialize the database
        orderItemService.save(orderItem)

        val databaseSizeBeforeUpdate = orderItemRepository.findAll().size

        // Update the orderItem
        val id = orderItem.id
        assertNotNull(id)
        val updatedOrderItem = orderItemRepository.findById(id).get()
        // Disconnect from session so that the updates on updatedOrderItem are not directly saved in db
        em.detach(updatedOrderItem)
        updatedOrderItem.description = UPDATED_DESCRIPTION
        updatedOrderItem.unitPrice = UPDATED_UNIT_PRICE
        updatedOrderItem.quantity = UPDATED_QUANTITY

        restOrderItemMockMvc.perform(
            put("/api/order-items")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(updatedOrderItem))
        ).andExpect(status().isOk)

        // Validate the OrderItem in the database
        val orderItemList = orderItemRepository.findAll()
        assertThat(orderItemList).hasSize(databaseSizeBeforeUpdate)
        val testOrderItem = orderItemList[orderItemList.size - 1]
        assertThat(testOrderItem.description).isEqualTo(UPDATED_DESCRIPTION)
        assertThat(testOrderItem.unitPrice).isEqualTo(UPDATED_UNIT_PRICE)
        assertThat(testOrderItem.quantity).isEqualTo(UPDATED_QUANTITY)
    }

    @Test
    @Transactional
    fun updateNonExistingOrderItem() {
        val databaseSizeBeforeUpdate = orderItemRepository.findAll().size

        // Create the OrderItem

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restOrderItemMockMvc.perform(
            put("/api/order-items")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(orderItem))
        ).andExpect(status().isBadRequest)

        // Validate the OrderItem in the database
        val orderItemList = orderItemRepository.findAll()
        assertThat(orderItemList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    fun deleteOrderItem() {
        // Initialize the database
        orderItemService.save(orderItem)

        val databaseSizeBeforeDelete = orderItemRepository.findAll().size

        val id = orderItem.id
        assertNotNull(id)

        // Delete the orderItem
        restOrderItemMockMvc.perform(
            delete("/api/order-items/{id}", id)
                .accept(APPLICATION_JSON_UTF8)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val orderItemList = orderItemRepository.findAll()
        assertThat(orderItemList).hasSize(databaseSizeBeforeDelete - 1)
    }

    @Test
    @Transactional
    fun equalsVerifier() {
        equalsVerifier(OrderItem::class)
        val orderItem1 = OrderItem()
        orderItem1.id = 1L
        val orderItem2 = OrderItem()
        orderItem2.id = orderItem1.id
        assertThat(orderItem1).isEqualTo(orderItem2)
        orderItem2.id = 2L
        assertThat(orderItem1).isNotEqualTo(orderItem2)
        orderItem1.id = null
        assertThat(orderItem1).isNotEqualTo(orderItem2)
    }

    companion object {

        val DEFAULT_DESCRIPTION: String = "Item 01"
        val UPDATED_DESCRIPTION = "Item 02"

        val DEFAULT_UNIT_PRICE: Double = 1.0
        val UPDATED_UNIT_PRICE: Double = 2.0

        val DEFAULT_QUANTITY: Int = 1
        val UPDATED_QUANTITY: Int = 2

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): OrderItem {
            val orderItem = OrderItem(
                description = DEFAULT_DESCRIPTION,
                unitPrice = DEFAULT_UNIT_PRICE,
                quantity = DEFAULT_QUANTITY
            )

            return orderItem
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): OrderItem {
            val orderItem = OrderItem(
                description = UPDATED_DESCRIPTION,
                unitPrice = UPDATED_UNIT_PRICE,
                quantity = UPDATED_QUANTITY
            )

            return orderItem
        }
    }
}
