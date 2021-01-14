package com.siddhantkushwaha.alfred.activity


import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.siddhantkushwaha.alfred.common.CommonUtil.checkPermissions

open class ActivityBase : AppCompatActivity() {

    protected lateinit var TAG: String

    // saves callbacks to be used in onRequestPermissionsResult
    private val requestPermissionCallbacks = HashMap<Int, (Boolean) -> Unit>()
    private val startActivityForResultCallbacks = HashMap<Int, (Intent?) -> Unit>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TAG = this::class.java.toString()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val callback = requestPermissionCallbacks[requestCode]
        if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
            callback?.invoke(true)
        else
            callback?.invoke(false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val callback = startActivityForResultCallbacks[requestCode]
        callback?.invoke(data)
    }

    protected fun requestPermissions(
        permissions: Array<String>,
        requestCode: Int,
        callback: (Boolean) -> Unit
    ) {
        val requiredPermissions = checkPermissions(this, permissions)
        if (requiredPermissions.isNotEmpty()) {
            requestPermissionCallbacks[requestCode] = callback
            ActivityCompat.requestPermissions(this, requiredPermissions, requestCode)
        } else {
            callback(true)
        }
    }

    protected fun startActivityForResult(
        intent: Intent,
        requestCode: Int,
        callback: (Intent?) -> Unit
    ) {
        startActivityForResultCallbacks[requestCode] = callback
        startActivityForResult(intent, requestCode)
    }
}