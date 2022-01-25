import ru.impression.singleton_entity.SingletonEntity
import ru.impression.singleton_entity.SingletonEntityImpl

class SimpleTest {
    @org.junit.Test
    fun test() {
        DemoEntity("123")
    }

}


class DemoEntity(override val id: String) : SingletonEntity by SingletonEntityImpl(id) {
    init {
        instance = this
    }
}