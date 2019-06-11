package energister.springdatasample.concurrenttransactions

import energister.springdatasample.OneOffFlag
import energister.springdatasample.SomeEntity
import energister.springdatasample.SomeEntityRepository
import mu.KotlinLogging
import org.springframework.transaction.annotation.Transactional

open class CreateNewRowService(private val repository: SomeEntityRepository) {
    private val logger = KotlinLogging.logger {}

    @Transactional
    open fun firstOperation(transactionStarted: OneOffFlag, runOperation: OneOffFlag) {
        logger.debug { "T1 has been opened" }

        logger.debug { "T1: there are ${repository.findAll().count()} entities in DB" }
        transactionStarted.set()

        runOperation.await()
        logger.debug { "T1 continue to do its job" }

        logger.debug { "T1: there are ${repository.findAll().count()} entities in DB" }
        createTickerIfAbsent()
    }

    @Transactional
    open fun secondOperation() {
        logger.debug { "T2 has been opened" }
        createTickerIfAbsent()
    }

    private fun createTickerIfAbsent() {
        val existingEntity = repository.findAll()
            .singleOrNull()

        if (existingEntity == null) {
            repository.save(SomeEntity(1))
            logger.info { "New row has been added" }
        } else {
            logger.info { "Row already exists" }
        }
    }
}