package lam.flutter.plugin.flutter_plugin

import android.app.Activity
import android.provider.ContactsContract
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ContactUtils {
    fun getAllContact(act: Activity?, onReturnAllContact: (ArrayList<Any>) -> Unit) {
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
                        onReturnAllContact(listContact)

                    }
                } catch (ex: Exception) {
                    Log.d("FlutterPlugin105", ex.toString())
                }
                cursorContact?.close()
            }
        }

    }
}