package com.siddhantkushwaha.alfred.common

import com.google.gson.Gson
import java.lang.reflect.Type

object GsonUtil {

    private val gson = Gson()

    public fun toJson(content: Any): String {
        return gson.toJson(content)
    }

    fun <T> fromJson(json: String, type: Type): T {
        return gson.fromJson(json, type)
    }
}