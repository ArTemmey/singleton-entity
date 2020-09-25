package ru.impression.syncable_entity

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0

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

    var isFullySynced = false
        internal set(value) {
            field = value
            if (value)
                this::class.members.forEach { it.getSyncableDelegate(this)?.onSyncCompleted() }
        }

    var isFullySyncing = false
        private set(value) {
            field = value
            onStateChanged(false)
        }

    private val propertySetSyncFuns = HashMap<Set<KMutableProperty0<*>>, Function<*>>()

    protected fun <T> state(
        initialValue: T,
        immediatelyBindChanges: Boolean = false,
        onChanged: ((T) -> Unit)? = null
    ): ReadWriteProperty<SyncableEntity, T> =
        SyncableEntityStateDelegate(this, initialValue, null, immediatelyBindChanges, onChanged)

    protected fun <T> state(
        getInitialValue: suspend () -> T,
        immediatelyBindChanges: Boolean = false,
        onChanged: ((T?) -> Unit)? = null
    ): ReadWriteProperty<SyncableEntity, T?> = SyncableEntityStateDelegate(
        this,
        null,
        getInitialValue,
        immediatelyBindChanges,
        onChanged
    )

    protected fun <T : SyncableEntity?> syncableEntity(
        initialValue: T,
        observeChanges: Boolean = true
    ) = SyncableEntityParentDelegate(this, initialValue, observeChanges)
        .also { delegates.add(it) }

    protected fun <T : SyncableEntity> syncableEntities(
        initialValue: List<T>?,
        observeChanges: Boolean = true
    ) = SyncableEntityParentDelegate(this, initialValue, observeChanges)
        .also { delegates.add(it) }

    fun <T> syncable(sourceValue: T, sync: (suspend (T) -> Unit)) =
        SyncableDelegate(this, sourceValue, sync)

    @Synchronized
    internal fun bind(parent: SyncableEntityParent, observeChanges: Boolean) {
        if (!parents.contains(parent)) parents[parent] = observeChanges
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

    fun <T0, T1> TwoPropertySet<T0, T1>.setSyncFun(syncFun: suspend (value0: T0, value1: T1) -> Unit) {
        propertySetSyncFuns[properties] = syncFun
    }

    internal fun PropertySet.getSyncFun() = propertySetSyncFuns[properties]

    suspend fun doFullySync() {
        if (isFullySynced) return
        isFullySyncing = true
        fullySync()
        isFullySyncing = false
        isFullySynced = true
    }

    open suspend fun fullySync() = Unit
}