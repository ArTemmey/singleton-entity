import junit.framework.Assert.assertTrue
import ru.impression.singleton_entity.SingletonEntity
import ru.impression.singleton_entity.SingletonEntityImpl
import java.util.*

class SimpleTest {
    @org.junit.Test
    fun test() {
        var sample: Sample? = Sample()
        val map: WeakHashMap<Sample, Any> = WeakHashMap()

        map[sample] = Any()
        assertTrue(map.containsKey(sample))

        sample = null
        System.gc()

        Thread.sleep(10000)

        println(map)
    }

}

class Sample {
    val key = Key()

    class Key
}


class DemoEntity(override val id: String) : SingletonEntity by SingletonEntityImpl(id) {
    init {
        instance = this
    }
}