package ru.impression.syncable_entity

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import ru.impression.kotlin_delegate_concatenator.plus
import ru.impression.ui_generator_base.StateDelegate
import ru.impression.ui_generator_base.StateOwner
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.jvm.reflect

abstract class SyncableEntity : SyncableEntityParent,
    CoroutineScope by CoroutineScope(Dispatchers.IO) {

    open val id: Any? = null

    var isSingleton: Boolean
        get() = SingletonEntities.contains(this)
        set(value) {
            if (value) SingletonEntities.add(this) else SingletonEntities.remove(this)
        }

    @Volatile
    private var wasSingleton = false

    private val parentDelegates = CopyOnWriteArraySet<SyncableEntityDelegate<*, *>>()

    private val parents = CopyOnWriteArraySet<SyncableEntityParent>()

    var allIsSynced
        get() = this::class.members.fold(true) { acc, member ->
            acc && member.getSyncablePropertyDelegate(this)?.isSynced == true
        }
        private set(value) {
            if (value)
                this::class.members.forEach {
                    it.getSyncablePropertyDelegate(this)?.onCurrentValueSynced()
                }
        }

    var allIsSyncing = false
        private set(value) {
            field = value
            this::class.members.forEach { it.getSyncablePropertyDelegate(this)?.isSyncing = value }
            onStateChanged()
        }

    private val onSyncMultipleFuns = HashMap<List<KMutableProperty0<*>>, Function<*>>()

    protected fun <T> state(initialValue: T, onChanged: ((T) -> Unit)? = null) =
        StateDelegate(this, initialValue, onChanged)

    fun <T> syncableProperty(sourceValue: T, sync: (suspend (T) -> Unit)? = null) =
        SyncablePropertyDelegate(this, sourceValue, sync, false)

    fun <T : SyncableEntity?> StateDelegate<SyncableEntity, T>.andSyncableEntity() =
        (this + syncableEntity(value)) as ReadWriteProperty<SyncableEntity, T>

    fun <T : List<SyncableEntity>?> StateDelegate<SyncableEntity, T>.andSyncableEntities() =
        (this + (syncableEntities(value) as ReadWriteProperty<SyncableEntity, T>))
                as ReadWriteProperty<SyncableEntity, T>

    fun <T> StateDelegate<SyncableEntity, T>.andSyncableProperty(sync: (suspend (T) -> Unit)? = null) =
        (this + SyncablePropertyDelegate(parent, value, sync, true))
                as ReadWriteProperty<SyncableEntity, T>

    @Synchronized
    internal fun addParentDelegate(delegate: SyncableEntityDelegate<*, *>) {
        parentDelegates.add(delegate)
        if (wasSingleton) {
            isSingleton = true
            wasSingleton = false
        }
    }

    @Synchronized
    internal fun removeParentDelegate(delegate: SyncableEntityDelegate<*, *>) {
        parentDelegates.remove(delegate)
        if (parentDelegates.isEmpty()) {
            isSingleton = false
            wasSingleton = true
        }
    }

    @Synchronized
    internal fun addParent(parent: SyncableEntityParent) {
        parents.add(parent)
    }

    @Synchronized
    internal fun removeParent(parent: SyncableEntityParent) {
        parents.remove(parent)
    }

    @Synchronized
    override fun onStateChanged(renderImmediately: Boolean) {
        parents.forEach { it.onStateChanged(renderImmediately) }
    }

    @Synchronized
    internal fun replaceWith(newEntity: SyncableEntity) {
        parentDelegates.forEach { it.replace(this, newEntity) }
    }

    fun <T1, T2> doOnSyncMultiple(
        property1: KMutableProperty0<T1>,
        property2: KMutableProperty0<T2>,
        onSyncMultiple: suspend (value1: T1, value2: T2) -> Unit
    ) {
        onSyncMultipleFuns[listOf(property1, property2)] = onSyncMultiple
    }

    suspend fun syncMultiple(
        property1: KMutableProperty0<*>,
        property2: KMutableProperty0<*>
    ) {
        syncMultiple(
            property1 to property1.getSyncablePropertyDelegate()?.value,
            property2 to property2.getSyncablePropertyDelegate()?.value
        )
    }

    suspend fun <T1, T2> setAndSyncMultiple(
        propertyAndValue1: Pair<KMutableProperty0<T1>, T1>,
        propertyAndValue2: Pair<KMutableProperty0<T2>, T2>
    ) {
        propertyAndValue1.first.set(propertyAndValue1.second)
        propertyAndValue2.first.set(propertyAndValue2.second)
        syncMultiple(propertyAndValue1, propertyAndValue2)
    }

    suspend fun <T1, T2> syncAndSetMultiple(
        propertyAndValue1: Pair<KMutableProperty0<T1>, T1>,
        propertyAndValue2: Pair<KMutableProperty0<T2>, T2>
    ) {
        syncMultiple(propertyAndValue1, propertyAndValue2)
        propertyAndValue1.first.set(propertyAndValue1.second)
        propertyAndValue2.first.set(propertyAndValue2.second)
    }

    private suspend fun syncMultiple(vararg propertiesAndValues: Pair<KMutableProperty0<*>, Any?>) {
        var needSync = false
        propertiesAndValues.forEach {
            if (it.first.getSyncablePropertyDelegate()?.isSynced(it.second) == false) {
                needSync = true
                return@forEach
            }
        }
        if (!needSync) return
        val syncFun = onSyncMultipleFuns[propertiesAndValues.map { it.first }]
        propertiesAndValues.forEach { it.first.getSyncablePropertyDelegate()?.isSyncing = true }
        when (syncFun?.reflect()?.parameters?.size) {
            2 -> (syncFun as suspend (Any?, Any?) -> Unit).invoke(
                propertiesAndValues[0].second,
                propertiesAndValues[1].second
            )
        }
        propertiesAndValues.forEach {
            it.first.getSyncablePropertyDelegate()?.apply {
                onCurrentValueSynced()
                isSyncing = false
            }
        }
    }

    suspend fun syncAll() {
        if (allIsSynced) return
        allIsSyncing = true
        onSyncAll()
        allIsSynced = true
        allIsSyncing = false
    }

    open suspend fun onSyncAll() = Unit
}