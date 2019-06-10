package energister.springdatasample

import org.springframework.data.repository.CrudRepository

interface SomeEntityRepository : CrudRepository<SomeEntity, Long>