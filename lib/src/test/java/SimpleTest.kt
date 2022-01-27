import junit.framework.Assert.assertTrue
import ru.impression.singleton_entity.SingletonEntity
import ru.impression.singleton_entity.SingletonEntityImpl
import ru.impression.singleton_entity.SingletonEntityList
import java.util.*
import kotlin.collections.LinkedHashMap

class SimpleTest {
    @org.junit.Test
    fun test() {
        SingletonEntityList<DemoEntity>().apply { add(DemoEntity("")) }
    }

}


class DemoEntity(override val id: String) : SingletonEntity by SingletonEntityImpl(id) {
    init {
        instance = this
    }
}