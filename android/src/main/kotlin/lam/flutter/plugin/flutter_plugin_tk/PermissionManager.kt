package lam.flutter.plugin.flutter_plugin_tk

import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import android.os.Build
import androidx.core.content.ContextCompat
import io.flutter.plugin.common.PluginRegistry
import lam.flutter.plugin.flutter_plugin.R

class PermissionManager(private var act: Activity?) :
    PluginRegistry.RequestPermissionsResultListener {

    fun checkPermissionGranted(
        permissions: Array<out String>,
        requestCode: Int,
        onAllPermissionGranted: () -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            act?.let {
                when {
                    hasPermission(permissions) -> {
                        onAllPermissionGranted()
                    }
                    shouldShowRequestPermissionRationale(
                        it,
                        permissions[0]
                    ) -> {
                        showUIRequestPermission(
                            it, requestCode, permissions[0],
                        )
                    }

                    else -> {
                        it.requestPermissions(
                            permissions, requestCode
                        )
                    }
                }
            }
        } else {
            onAllPermissionGranted()
        }
    }

    private fun showUIRequestPermission(
        it: Activity,
        requestCode: Int,
        permission: String,
    ) {
        AlertDialog.Builder(it).apply {
            setTitle("Permission Request")
            setMessage("You need to accept this permission to perform this function")
            setPositiveButton(R.string.yes) { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    it.requestPermissions(
                        arrayOf(
                            permission
                        ), requestCode
                    )
                }
            }
            setNegativeButton(R.string.no) { _, _ -> }
            show()
        }
    }

    private fun checkShouldShowRequestPermissionRationale(permissions: Array<out String>?): List<String> {
        val listPermissionDenied = arrayListOf<String>()
        act?.let {
            permissions?.forEach { permission ->
                if (!checkSinglePermission(it, permission)) {
                    listPermissionDenied.add(permission)
                }
            }
        }
        return listPermissionDenied
    }

    private fun hasPermission(permissions: Array<out String>?): Boolean {
        act?.let { act ->
            permissions?.forEach { permission ->
                if (!checkSinglePermission(act, permission)) {
                    return false
                }
            }
        }

        return true
    }

    private fun checkSinglePermission(act: Activity, permission: String): Boolean =
        ContextCompat.checkSelfPermission(
            act,
            permission
        ) == PackageManager.PERMISSION_GRANTED


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        when (requestCode) {
            FlutterImagePickerPlugin.PICK_CONTACT_REQUEST_CODE -> {
                if (hasPermission(permissions)) {
                    return true
                }
            }
            FlutterImagePickerPlugin.CAMERA_REQUEST_CODE -> {
                if (hasPermission(permissions)) {
                    return true
                }
            }
        }
        return false
    }

}