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

    @Volatile
    private var wasGlobal = false

    private val delegates = ArrayList<SyncableEntityParentDelegate<SyncableEntity, *>>()

    private val parents = ConcurrentHashMap<SyncableEntityParent, Boolean>()

    var isFullySynced
        get() = this::class.members.fold(true) { acc, member ->
            acc && member.getSyncableDelegate(this)?.isSynced == true
        }
        private set(value) {
            if (value)
                this::class.members.forEach { it.getSyncableDelegate(this)?.onSyncCompleted() }
        }

    var isFullySyncing = false
        private set(value) {
            field = value
            this::class.members.forEach { it.getSyncableDelegate(this)?.isSyncing = value }
            onStateChanged(false)
        }

    private val propertySetSyncFuns = HashMap<List<KMutableProperty0<*>>, Function<*>>()

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
        sourceValues: List<T>?,
        observeState: Boolean
    ) = SyncableEntityParentDelegate(this, sourceValues, observeState)
        .also { delegates.add(it) }

    fun <T> syncableProperty(sourceValue: T, sync: (suspend (T) -> Unit)? = null) =
        SyncablePropertyDelegate(this, sourceValue, sync)

    @Synchronized
    internal fun bind(parent: SyncableEntityParent, observeState: Boolean) {
        if (!parents.contains(parent)) parents[parent] = observeState
        if (wasGlobal) {
            isGlobal = true
            wasGlobal = false
        }
    }

    @Synchronized
    internal fun unbind(parent: SyncableEntityParent) {
        parents.remove(parent)
        if (parents.isEmpty()) {
            wasGlobal = isGlobal
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

    fun <T1, T2> setSyncFun(
        property1: KMutableProperty0<T1>,
        property2: KMutableProperty0<T2>,
        syncFun: suspend (value1: T1, value2: T2) -> Unit
    ) {
        propertySetSyncFuns[listOf(property1, property2)] = syncFun
    }

    suspend fun sync(
        property1: KMutableProperty0<*>,
        property2: KMutableProperty0<*>
    ) {
        doSync(
            property1 to property1.getSyncableDelegate()?.value,
            property2 to property2.getSyncableDelegate()?.value
        )
    }

    suspend fun <T1, T2> setAndSync(
        propertyAndValue1: Pair<KMutableProperty0<T1>, T1>,
        propertyAndValue2: Pair<KMutableProperty0<T2>, T2>
    ) {
        propertyAndValue1.first.set(propertyAndValue1.second)
        propertyAndValue2.first.set(propertyAndValue2.second)
        doSync(propertyAndValue1, propertyAndValue2)
    }

    suspend fun <T1, T2> syncAndSet(
        propertyAndValue1: Pair<KMutableProperty0<T1>, T1>,
        propertyAndValue2: Pair<KMutableProperty0<T2>, T2>
    ) {
        doSync(propertyAndValue1, propertyAndValue2)
        propertyAndValue1.first.set(propertyAndValue1.second)
        propertyAndValue2.first.set(propertyAndValue2.second)
    }

    private suspend fun doSync(vararg propertiesAndValues: Pair<KMutableProperty0<*>, Any?>) {
        var needSync = false
        propertiesAndValues.forEach {
            if (it.first.getSyncableDelegate()?.isSynced(it.second) == false) {
                needSync = true
                return@forEach
            }
        }
        if (!needSync) return
        val syncFun = propertySetSyncFuns[propertiesAndValues.map { it.first }]
        propertiesAndValues.forEach { it.first.getSyncableDelegate()?.isSyncing = true }
        when (syncFun?.reflect()?.parameters?.size) {
            2 -> (syncFun as suspend (Any?, Any?) -> Unit).invoke(
                propertiesAndValues[0].second,
                propertiesAndValues[1].second
            )
        }
        propertiesAndValues.forEach {
            it.first.getSyncableDelegate()?.apply {
                onSyncCompleted()
                isSyncing = false
            }
        }
    }

    suspend fun doFullySync() {
        if (isFullySynced) return
        isFullySyncing = true
        fullySync()
        isFullySynced = true
        isFullySyncing = false
    }

    open suspend fun fullySync() = Unit
}