package lam.flutter.plugin.flutter_plugin_tk

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.NonNull
import com.google.gson.Gson
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry


/** FlutterPlugin */
/**
 *  - implements ActivityAware,PluginRegistry.ActivityResultListener to retrieve
 *  the activity that will be used to show about activity
 */
class FlutterImagePickerPlugin : FlutterPlugin, MethodCallHandler, ActivityAware,
    PluginRegistry.ActivityResultListener {
    /** MethodChannel to contact with Flutter*/
    private lateinit var channel: MethodChannel
    private var act: Activity? = null
    private lateinit var result: Result
    private lateinit var permissionManager: PermissionManager

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_plugin")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        this.result = result
        permissionManager = PermissionManager(act)
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${Build.VERSION.RELEASE}")
            }
            "getAllContact" -> {
                permissionManager.checkPermissionGranted(
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    PICK_CONTACT_REQUEST_CODE
                ) {
                    ContactUtils.getAllContact(act) { listContact ->
                        result.success(listContact)
                    }
                }
            }
            "getImageFromGallery" -> {
                val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
                imagePickerIntent.type = "image/*"
                act?.startActivityForResult(
                    Intent.createChooser(
                        imagePickerIntent,
                        "Select Picture"
                    ), PICK_IMAGE_RESULT_CODE
                )
            }

            "getMultiImageFromGallery" -> {
                val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
                imagePickerIntent.type = "image/*"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    imagePickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
                act?.startActivityForResult(
                    Intent.createChooser(
                        imagePickerIntent,
                        "Select Picture"
                    ), PICK_MULTI_IMAGE_RESULT_CODE
                )
            }

            "getImageFromCamera" -> {
                permissionManager.checkPermissionGranted(
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_REQUEST_CODE
                ) {
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    act?.startActivityForResult(takePictureIntent, TAKE_IMAGE_RESULT_CODE)
                }
            }
            else -> {
                result.notImplemented()
            }
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
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_RESULT_CODE -> {
                    data?.let {
                        val uri: Uri? = it.data
                        uri?.let {
                            act?.let { act ->
                                val file = FileUtils.createFileFromUri(
                                    act.contentResolver,
                                    uri,
                                    act.cacheDir
                                )
                                result.success(file.path.toString())
                            }
                        }
                    }
                }
                PICK_MULTI_IMAGE_RESULT_CODE -> {
                    data?.clipData?.let {
                        val count = it.itemCount
                        val listPath = arrayListOf<Any>()
                        val gson = Gson()
                        for (i in 0 until count) {
                            val uri = it.getItemAt(i).uri
                            uri?.let {
                                act?.let { act ->
                                    val file = FileUtils.createFileFromUri(
                                        act.contentResolver,
                                        uri,
                                        act.cacheDir
                                    )
                                    listPath.add(gson.toJson(file.path.toString()))
                                }
                            }
                        }
                        result.success(listPath)
                    }
                }
                TAKE_IMAGE_RESULT_CODE -> {
                    data?.let {
                        val imageBitmap = it.extras?.get("data") as Bitmap
                        act?.cacheDir?.let { it1 ->
                            FileUtils.storeImageToAppCache(imageBitmap, it1) { path ->
                                result.success(path)
                            }
                        }
                    }
                }
            }
        }
        return false
    }

    companion object {
        const val PICK_CONTACT_REQUEST_CODE = 54324
        const val PICK_IMAGE_RESULT_CODE = 71243
        const val PICK_MULTI_IMAGE_RESULT_CODE = 71244
        const val TAKE_IMAGE_RESULT_CODE = 81287
        const val CAMERA_REQUEST_CODE = 34231
    }

}
