package energister.springdatasample.concurrenttransactions

import energister.springdatasample.OneOffFlag
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
    }

    @Autowired
    internal lateinit var someEntityRepository: SomeEntityRepository

    @Autowired
    internal lateinit var newRowService: CreateNewRowService

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
        someEntityRepository.deleteAll()

        val firstOperationTransactionStarted = OneOffFlag()
        val secondOperationCompleted = OneOffFlag()

        val firstOperation = executeInThread {
            newRowService.firstOperation(firstOperationTransactionStarted, secondOperationCompleted)
            logger.debug { "First operation has committed it's result to DB" }
        }

        firstOperationTransactionStarted.await()

        newRowService.secondOperation()
        logger.debug { "Second operation has committed it's result to DB" }
        secondOperationCompleted.set()

        println(firstOperation.get())
    }
}
