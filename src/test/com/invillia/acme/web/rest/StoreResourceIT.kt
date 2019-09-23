package invillia.acme.web.rest

import com.invillia.acme.InvilliaApplication
import com.invillia.acme.domain.Store
import com.invillia.acme.repository.StoreRepository
import com.invillia.acme.service.StoreService
import com.invillia.acme.web.rest.StoreResource
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
 * Integration tests for the [StoreResource] REST controller.
 *
 * @see StoreResource
 */
@SpringBootTest(classes = [InvilliaApplication::class])
class StoreResourceIT {

    @Autowired
    private lateinit var storeRepository: StoreRepository

    @Autowired
    private lateinit var storeService: StoreService

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

    private lateinit var restStoreMockMvc: MockMvc

    private lateinit var store: Store

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val storeResource = StoreResource(storeService)
        this.restStoreMockMvc = MockMvcBuilders.standaloneSetup(storeResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        store = createEntity(em)
    }

    @Test
    @Transactional
    fun createStore() {
        val databaseSizeBeforeCreate = storeRepository.findAll().size

        // Create the Store
        restStoreMockMvc.perform(
            post("/api/stores")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(store))
        ).andExpect(status().isCreated)

        // Validate the Store in the database
        val storeList = storeRepository.findAll()
        assertThat(storeList).hasSize(databaseSizeBeforeCreate + 1)
        val testStore = storeList[storeList.size - 1]
        assertThat(testStore.name).isEqualTo(DEFAULT_NAME)
        assertThat(testStore.address).isEqualTo(DEFAULT_ADDRESS)
    }

    @Test
    @Transactional
    fun createStoreWithExistingId() {
        val databaseSizeBeforeCreate = storeRepository.findAll().size

        // Create the Store with an existing ID
        store.id = 1L

        // An entity with an existing ID cannot be created, so this API call must fail
        restStoreMockMvc.perform(
            post("/api/stores")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(store))
        ).andExpect(status().isBadRequest)

        // Validate the Store in the database
        val storeList = storeRepository.findAll()
        assertThat(storeList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    fun getAllStores() {
        // Initialize the database
        storeRepository.saveAndFlush(store)

        // Get all the storeList
        restStoreMockMvc.perform(get("/api/stores?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(store.id?.toInt())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
    }

    @Test
    @Transactional
    fun getStore() {
        // Initialize the database
        storeRepository.saveAndFlush(store)

        val id = store.id
        assertNotNull(id)

        // Get the store
        restStoreMockMvc.perform(get("/api/stores/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(id.toInt()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.address").value(DEFAULT_ADDRESS))
    }

    @Test
    @Transactional
    fun getNonExistingStore() {
        // Get the store
        restStoreMockMvc.perform(get("/api/stores/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }

    @Test
    @Transactional
    fun updateStore() {
        // Initialize the database
        storeService.save(store)

        val databaseSizeBeforeUpdate = storeRepository.findAll().size

        // Update the store
        val id = store.id
        assertNotNull(id)
        val updatedStore = storeRepository.findById(id).get()
        // Disconnect from session so that the updates on updatedStore are not directly saved in db
        em.detach(updatedStore)
        updatedStore.name = UPDATED_NAME
        updatedStore.address = UPDATED_ADDRESS

        restStoreMockMvc.perform(
            put("/api/stores")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(updatedStore))
        ).andExpect(status().isOk)

        // Validate the Store in the database
        val storeList = storeRepository.findAll()
        assertThat(storeList).hasSize(databaseSizeBeforeUpdate)
        val testStore = storeList[storeList.size - 1]
        assertThat(testStore.name).isEqualTo(UPDATED_NAME)
        assertThat(testStore.address).isEqualTo(UPDATED_ADDRESS)
    }

    @Test
    @Transactional
    fun updateNonExistingStore() {
        val databaseSizeBeforeUpdate = storeRepository.findAll().size

        // Create the Store

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restStoreMockMvc.perform(
            put("/api/stores")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(store))
        ).andExpect(status().isBadRequest)

        // Validate the Store in the database
        val storeList = storeRepository.findAll()
        assertThat(storeList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    fun deleteStore() {
        // Initialize the database
        storeService.save(store)

        val databaseSizeBeforeDelete = storeRepository.findAll().size

        val id = store.id
        assertNotNull(id)

        // Delete the store
        restStoreMockMvc.perform(
            delete("/api/stores/{id}", id)
                .accept(APPLICATION_JSON_UTF8)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val storeList = storeRepository.findAll()
        assertThat(storeList).hasSize(databaseSizeBeforeDelete - 1)
    }

    @Test
    @Transactional
    fun equalsVerifier() {
        equalsVerifier(Store::class)
        val store1 = Store()
        store1.id = 1L
        val store2 = Store()
        store2.id = store1.id
        assertThat(store1).isEqualTo(store2)
        store2.id = 2L
        assertThat(store1).isNotEqualTo(store2)
        store1.id = null
        assertThat(store1).isNotEqualTo(store2)
    }

    companion object {

        private const val DEFAULT_NAME: String = "AAAAAAAAAA"
        private const val UPDATED_NAME = "BBBBBBBBBB"

        private const val DEFAULT_ADDRESS: String = "AAAAAAAAAA"
        private const val UPDATED_ADDRESS = "BBBBBBBBBB"

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Store {
            val store = Store(
                name = DEFAULT_NAME,
                address = DEFAULT_ADDRESS
            )

            return store
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Store {
            val store = Store(
                name = UPDATED_NAME,
                address = UPDATED_ADDRESS
            )

            return store
        }
    }
}
