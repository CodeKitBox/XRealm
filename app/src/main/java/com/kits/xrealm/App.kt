package com.kits.xrealm

import android.app.Application
import com.kits.xrealm.migration.DbMigration

import io.realm.Realm
import io.realm.RealmConfiguration


class App : Application(){
    override fun onCreate() {
        super.onCreate()
        dbInit()
    }

    private fun dbInit(){
        Realm.init(this)
        val config = RealmConfiguration.Builder()
            .name("test.realm")
            .migration(DbMigration())
            .schemaVersion(2)
            .build()
        Realm.setDefaultConfiguration(config)
    }



}