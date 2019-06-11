package energister.springdatasample.concurrenttransactions

import energister.springdatasample.OneOffFlag
import energister.springdatasample.SomeEntity
import energister.springdatasample.SomeEntityRepository
import mu.KotlinLogging
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

open class UpdateRowService(private val repository: SomeEntityRepository) {
    private val logger = KotlinLogging.logger {}

    @Transactional
    open fun firstOperation(key: Long, transactionStarted: OneOffFlag, startOperationSignal: OneOffFlag) {
        logger.debug { "T1 has been opened" }

//        val entity = getEntity()
//        logger.debug { "T1: entity is $entity1" }
        transactionStarted.set()

        startOperationSignal.await()
        logger.debug { "T1 continue to do its job" }

        val entity2 = getEntity2(key)
        logger.debug { "T1: entity is $entity2" }
        increaseValue(entity2)
    }

    @Transactional
    open fun secondOperation(key: Long) {
        logger.debug { "T2 has been opened" }

        val entity = getEntity(key)
        logger.debug { "T2: entity is $entity" }
        increaseValue(entity)
    }

    private fun increaseValue(entity: SomeEntity) {
        entity.value++
        val saved = repository.save(entity)
        logger.info { "Value has been increased by one. Result is ${saved.value}" }
    }

    private fun getEntity(id: Long): SomeEntity {
        return repository.findById(id).get()
    }

    private fun getEntity2(id: Long): SomeEntity {
        return repository.findAll().single {
            it.id == id
        }
    }
}