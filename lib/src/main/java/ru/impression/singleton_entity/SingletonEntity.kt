package ru.impression.singleton_entity

interface SingletonEntity : SingletonEntityParent {
    val id: Any

    fun replaceWith(newEntity: SingletonEntity)

    fun addParent(parent: SingletonEntityParent)

    fun removeParent(parent: SingletonEntityParent)
}

class SingletonEntityImpl(override val id: Any) : SingletonEntity,
    SingletonEntityParent by SingletonEntityParentImpl() {

    private val parents = ArrayList<SingletonEntityParent>()

    init {
        EntityStore.replaceIfExists(this)
    }

    override fun addParent(parent: SingletonEntityParent) {
        if (!parents.contains(parent)) {
            parents.add(parent)
            EntityStore.put(this)
        }
    }

    override fun removeParent(parent: SingletonEntityParent) {
        parents.remove(parent)
        if (parents.isEmpty()) {
            EntityStore.remove(this)
            detachFromEntities()
        }
    }

    override fun replaceWith(newEntity: SingletonEntity) {
        parents.forEach { it.replace(this, newEntity) }
    }
}

//class Cart(override val id: String) : SingletonEntity by SingletonEntityImpl(id)
//
//class OrderDraft(override val id: String, cart: Cart) : SingletonEntity by SingletonEntityImpl(id) {
//
//    var cart by singletonEntity(cart)
//
//}
//
//class ViewModel : SingletonEntityParent by SingletonEntityParentImpl() {
//    var orderDraft by singletonEntity<OrderDraft?>(null)
//
//    fun f() {
//        orderDraft = OrderDraft("123", Cart("123"))
//    }
//
//    fun clear() {
//        detachFromEntities()
//    }
//}
