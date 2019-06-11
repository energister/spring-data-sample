package energister.springdatasample.concurrenttransactions

import energister.springdatasample.OneOffFlag
import energister.springdatasample.SomeEntity
import energister.springdatasample.SomeEntityRepository
import mu.KotlinLogging
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.junit4.SpringRunner
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread

@RunWith(SpringRunner::class)
@SpringBootTest
class ConcurrentTransactionsTests {

    private val logger = KotlinLogging.logger {}

    @TestConfiguration
    class Config {
        @Bean
        fun createNewRowService(repository: SomeEntityRepository): CreateNewRowService {
            return CreateNewRowService(repository)
        }

        @Bean
        fun updateRowService(repository: SomeEntityRepository): UpdateRowService {
            return UpdateRowService(repository)
        }
    }

    @Autowired
    internal lateinit var repository: SomeEntityRepository

    @Autowired
    internal lateinit var createService: CreateNewRowService

    @Autowired
    internal lateinit var updateService: UpdateRowService

    private fun <T> executeInThread(code: () -> T): CompletableFuture<T> {
        val future = CompletableFuture<T>()
        thread {
            try {
                val result = code()
                future.complete(result)
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }
        return future
    }

    @Test
    fun createNewEntity() {
        repository.deleteAll()

        val firstOperationTransactionStarted = OneOffFlag()
        val secondOperationCompleted = OneOffFlag()

        val firstOperation = executeInThread {
            createService.firstOperation(firstOperationTransactionStarted, secondOperationCompleted)
            logger.debug { "First operation has committed it's result to DB" }
        }

        firstOperationTransactionStarted.await()

        createService.secondOperation()
        logger.debug { "Second operation has committed it's result to DB" }
        secondOperationCompleted.set()

        println(firstOperation.get())
    }

    @Test
    fun updateExistingEntity() {
        // create entity if it doesn't exist
        val entity = repository.findAll().firstOrNull()
        val entityId: Long = if (entity == null) {
            repository.save(SomeEntity(1)).id!!
        } else {
            entity.id!!
        }

        val firstOperationTransactionStarted = OneOffFlag()
        val secondOperationCompleted = OneOffFlag()

        val firstOperation = executeInThread {
            updateService.firstOperation(entityId, firstOperationTransactionStarted, secondOperationCompleted)
            logger.debug { "First operation has committed it's result to DB" }
        }

        firstOperationTransactionStarted.await()

        updateService.secondOperation(entityId)
        logger.debug { "Second operation has committed it's result to DB" }
        secondOperationCompleted.set()

        println(firstOperation.get())
    }
}
