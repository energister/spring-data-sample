package energister.springdatasample

import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class SomeEntity(var value: Int) {
    @Id
    @GenericGenerator(
            name = "some_entity_id_seq",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = [Parameter(name = "sequence_name", value = "some_entity_id_seq"),
                Parameter(name = "initial_value", value = "1"),
                Parameter(name = "increment_size", value = "1")]
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "some_entity_id_seq")
    val id: Long? = null
}
