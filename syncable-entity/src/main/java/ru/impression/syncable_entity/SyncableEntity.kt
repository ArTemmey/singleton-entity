package ru.impression.syncable_entity

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.jvm.reflect

abstract class SyncableEntity : SyncableEntityParent,
    CoroutineScope by CoroutineScope(Dispatchers.IO) {

    open val primaryProperty: Any? = null

    var isGlobal: Boolean
        get() = GlobalStore.contains(this)
        set(value) {
            if (value) GlobalStore.add(this) else GlobalStore.remove(this)
        }

    private val delegates = ArrayList<SyncableEntityParentDelegate<SyncableEntity, *>>()

    private val parents = ConcurrentHashMap<SyncableEntityParent, Boolean>()

    var allIsSynced
        get() = this::class.members.fold(true) { acc, member ->
            acc && member.getSyncableDelegate(this)?.isSynced == true
        }
        private set(value) {
            if (value)
                this::class.members.forEach {
                    it.getSyncableDelegate(this)?.onCurrentValueSynced()
                }
        }

    var allIsSyncing = false
        private set(value) {
            field = value
            this::class.members.forEach { it.getSyncableDelegate(this)?.isSyncing = value }
            onStateChanged()
        }

    private val onSyncMultipleFuns = HashMap<List<KMutableProperty0<*>>, Function<*>>()

    protected fun <T> state(
        initialValue: T,
        immediatelyBindChanges: Boolean = false,
        onChanged: ((T) -> Unit)? = null
    ) = SyncableEntityStateDelegate(this, initialValue, null, immediatelyBindChanges, onChanged)

    protected fun <T> state(
        getInitialValue: suspend () -> T,
        immediatelyBindChanges: Boolean = false,
        onChanged: ((T?) -> Unit)? = null
    ) = SyncableEntityStateDelegate(
        this,
        null,
        getInitialValue,
        immediatelyBindChanges,
        onChanged
    )

    override fun <T : SyncableEntity?> syncableEntity(
        sourceValue: T,
        observeState: Boolean
    ) = SyncableEntityParentDelegate(this, sourceValue, observeState)
        .also { delegates.add(it) }

    override fun <T : SyncableEntity> syncableEntities(
        sourceValue: List<T>?,
        observeState: Boolean
    ) = SyncableEntityParentDelegate(this, sourceValue, observeState)
        .also { delegates.add(it) }

    fun <T> syncableProperty(sourceValue: T, sync: (suspend (T) -> Unit)? = null) =
        SyncablePropertyDelegate(this, sourceValue, sync, false)

    @Synchronized
    internal fun bind(parent: SyncableEntityParent, observeState: Boolean) {
        if (!parents.contains(parent)) parents[parent] = observeState
    }

    @Synchronized
    internal fun unbind(parent: SyncableEntityParent) {
        parents.remove(parent)
        if (parents.isEmpty()) {
            isGlobal = false
        }
    }

    @Synchronized
    override fun onStateChanged(immediatelyBindChanges: Boolean) {
        parents.forEach { if (it.value) it.key.onStateChanged(immediatelyBindChanges) }
    }

    @Synchronized
    internal fun replaceWith(newEntity: SyncableEntity) {
        parents.keys.forEach { it.replace(this, newEntity) }
    }

    @Synchronized
    override fun replace(oldEntity: SyncableEntity, newEntity: SyncableEntity) {
        delegates.forEach { it.replace(oldEntity, newEntity) }
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
            property1 to property1.getSyncableDelegate()?.value,
            property2 to property2.getSyncableDelegate()?.value
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
            if (it.first.getSyncableDelegate()?.isSynced(it.second) == false) {
                needSync = true
                return@forEach
            }
        }
        if (!needSync) return
        val syncFun = onSyncMultipleFuns[propertiesAndValues.map { it.first }]
        propertiesAndValues.forEach { it.first.getSyncableDelegate()?.isSyncing = true }
        when (syncFun?.reflect()?.parameters?.size) {
            2 -> (syncFun as suspend (Any?, Any?) -> Unit).invoke(
                propertiesAndValues[0].second,
                propertiesAndValues[1].second
            )
        }
        propertiesAndValues.forEach {
            it.first.getSyncableDelegate()?.apply {
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