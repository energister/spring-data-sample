package energister.springdatasample.concurrenttransactions

import energister.springdatasample.OneOffFlag
import energister.springdatasample.SomeEntity
import energister.springdatasample.SomeEntityRepository
import mu.KotlinLogging
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronizationManager

open class CreateNewRowService(private val repository: SomeEntityRepository) {
    private val logger = KotlinLogging.logger {}

    @Transactional
    open fun firstOperation(transactionStarted: OneOffFlag, runOperation: OneOffFlag) {
        logger.debug { "First operation has open transaction" }

        logger.debug { "isActualTransactionActive ${TransactionSynchronizationManager.isActualTransactionActive()}" }
        logger.debug { "Isolation level is ${TransactionSynchronizationManager.getCurrentTransactionIsolationLevel()}" }

        logger.debug { "First operation: number of entities in DB is ${repository.findAll().count()}" }
        transactionStarted.set()

        runOperation.await()
        logger.debug { "First operation continue to do its job" }

        logger.debug { "First operation: number of entities in DB is ${repository.findAll().count()}" }
        createTickerIfAbsent()
    }

    @Transactional
    open fun secondOperation() {
        logger.debug { "Second operation has open transaction" }
        logger.debug { "Second operation: number of entities in DB is ${repository.findAll().count()}" }
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