package cn.jgayb.fenixplugin

class CollectionUtils private constructor() {
    init {
        throw UnsupportedOperationException()
    }

    companion object {
        fun isEmpty(collection: Collection<*>?): Boolean {
            return null == collection || collection.isEmpty()
        }

        fun isNotEmpty(collection: Collection<*>?): Boolean {
            return !isEmpty(collection)
        }
    }
}