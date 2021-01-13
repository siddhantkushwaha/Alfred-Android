package com.siddhantkushwaha.alfred.entity

import com.siddhantkushwaha.alfred.common.GsonUtil
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Notification : RealmObject() {

    @PrimaryKey
    public var id: String? = null

    public var timestamp: Long? = null
    public var packageName: String? = null

    // properties is going to be a JSON string
    public var properties: String? = null

    public fun setProperties(properties: HashMap<String, Pair<String, String?>>) {
        this.properties = GsonUtil.toJson(properties)
    }

    public fun getProperty(key: String): String? {
        val propertiesL = properties ?: return null
        val propertiesMap = GsonUtil.fromJson<HashMap<String, Pair<String, String?>>>(
            propertiesL,
            HashMap::class.java
        )
        return propertiesMap[key]?.second
    }
}