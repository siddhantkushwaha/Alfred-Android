package com.siddhantkushwaha.alfred.common

import android.content.Context
import io.realm.Realm
import io.realm.RealmConfiguration


object RealmUtil {

    public fun getCustomRealmInstance(context: Context): Realm {
        Realm.init(context)
        val config = RealmConfiguration.Builder()
            .name("db.realm")
            .deleteRealmIfMigrationNeeded()
            .build()
        return Realm.getInstance(config)
    }

    public fun clearData(realm: Realm) {
        realm.executeTransactionAsync { realmL: Realm -> realmL.deleteAll() }
    }
}