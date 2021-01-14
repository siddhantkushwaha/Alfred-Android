package com.siddhantkushwaha.alfred.entity

import com.siddhantkushwaha.alfred.common.GsonUtil
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class Notification : RealmObject() {

    @PrimaryKey
    public var id: String? = null

    public var timestamp: Long? = null
    public var packageName: String? = null
    public var appName: String? = null

    // properties is going to be a JSON string
    public var properties: String? = null

    public fun setProperties(properties: HashMap<String, String?>) {
        this.properties = GsonUtil.toJson(properties)
    }

    public fun getProperties(): HashMap<String, String?>? {
        val propertiesL = properties ?: return null
        return GsonUtil.fromJson(
            propertiesL,
            HashMap::class.java
        )
    }

    public fun getProperty(key: String): String? {
        val propertiesMap = getProperties()
        return propertiesMap?.get(key)
    }
}