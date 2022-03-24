package lam.flutter.plugin.flutter_plugin

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.ContactsContract
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import com.google.gson.Gson

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import kotlinx.coroutines.*

/** FlutterPlugin */
/**
 *  - implements ActivityAware,PluginRegistry.ActivityResultListener to retrieve
 *  the activity that will be used to show about activity
 */
class FlutterPlugin : FlutterPlugin, MethodCallHandler, ActivityAware,
    PluginRegistry.ActivityResultListener, PluginRegistry.RequestPermissionsResultListener {
    /** MethodChannel to contact with Flutter*/
    private lateinit var channel: MethodChannel
    private var act: Activity? = null
    private lateinit var result: Result

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_plugin")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        this.result = result
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${Build.VERSION.RELEASE}")
            }
            "getAllContact" -> {
                checkPermissionGranted()
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private fun checkPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            act?.let {
                when {
                    ContextCompat.checkSelfPermission(
                        it,
                        android.Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        getAllContact()
                    }
                    shouldShowRequestPermissionRationale(
                        it,
                        android.Manifest.permission.READ_CONTACTS
                    ) -> {
                        showUIRequestPermission(it)
                    }

                    else -> {
                        it.requestPermissions(
                            arrayOf(
                                android.Manifest.permission.READ_CONTACTS
                            ), PICK_CONTACT_RESULT_CODE
                        )
                    }
                }
            }
        } else {
            getAllContact()
        }
    }

    private fun showUIRequestPermission(it: Activity) {
        AlertDialog.Builder(it).apply {
            setTitle("Permission Request")
            setMessage("You need this permission to get all contact")
            setPositiveButton(R.string.yes) { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    it.requestPermissions(
                        arrayOf(
                            android.Manifest.permission.READ_CONTACTS
                        ), PICK_CONTACT_RESULT_CODE
                    )
                }
            }
            setNegativeButton(R.string.no) { _, _ -> }
            show()
        }
    }


    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    /** Binding with that activity */
    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        act = binding.activity
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        act = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        act = binding.activity
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivity() {
        act = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return false
    }

    private fun getAllContact() {
        val listContact = arrayListOf<Any>()
        act?.contentResolver?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val cursorContact =
                    it.query(
                        ContactsContract.Contacts.CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                    )
                try {
                    cursorContact?.let { ct ->
                        while (ct.moveToNext()) {
                            val id =
                                ct.getString(ct.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                            val name =
                                ct.getString(ct.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                            if (ct.getInt(ct.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                                val cursor = it.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    arrayOf(id),
                                    null
                                )
                                cursor?.let { cr ->
                                    while (cr.moveToNext()) {
                                        val phoneNo =
                                            cr.getString(cr.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                        val gson = Gson()
                                        val contact = Contact(name = name, number = phoneNo)
                                        withContext(Dispatchers.Default) {
                                            listContact.add(gson.toJson(contact))
                                        }
                                    }
                                    cr.close()
                                }
                            }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        result.success(listContact)
                    }
                } catch (ex: Exception) {
                    Log.d("FlutterPlugin105", ex.toString())
                }
                cursorContact?.close()
            }
        }

    }


    /** Response after request permission */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        if (requestCode == PICK_CONTACT_RESULT_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("AppLog", "Permission Granted")
            getAllContact()
            return true
        }
        Log.d("AppLog", "No Permission Granted")
        return false
    }

    companion object {
        const val PICK_CONTACT_RESULT_CODE = 54324
    }

}
