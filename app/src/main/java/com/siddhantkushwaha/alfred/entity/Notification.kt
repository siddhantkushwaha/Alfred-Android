package com.siddhantkushwaha.alfred.entity

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class Notification : RealmObject() {

    @PrimaryKey
    var id: String? = null
    var key: String? = null

    var timestamp: Long? = null
    var packageName: String? = null
    var appName: String? = null

    var appIconUri: String? = null
    var largeIconUri: String? = null
    var pictureUri: String? = null

    var textTitle: String? = null
    var textContent: String? = null
}