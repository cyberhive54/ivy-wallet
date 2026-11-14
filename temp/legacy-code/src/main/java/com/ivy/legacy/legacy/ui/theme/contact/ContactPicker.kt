package com.ivy.legacy.ui.theme.contact

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.design.l0_system.UI
import com.ivy.ui.R

/**
 * A contact picked from the system picker.
 *
 * We deliberately use the system contact picker (ACTION_PICK): the returned URI
 * comes with a temporary read grant, so the app needs **no READ_CONTACTS**
 * permission and never sees the whole address book.
 */
data class PickedContact(
    val name: String? = null,
    val phone: String? = null,
)

/**
 * Remembers a launcher that opens the system contact picker.
 * Returns a lambda that starts the picker; [onPicked] receives null when the
 * user cancels or the device has no contacts app.
 */
@Composable
fun rememberContactPicker(
    onPicked: (PickedContact?) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        onPicked(uri?.let { readPickedContact(context, it) })
    }

    return remember {
        {
            try {
                launcher.launch(null)
            } catch (e: Exception) {
                // No contacts app / no provider — manual entry still works
                onPicked(null)
            }
        }
    }
}

/**
 * "Pick contact" affordance for the ledger flows (create customer / add entry).
 * Manual typing always stays available next to it.
 */
@Composable
fun ContactPickButton(
    modifier: Modifier = Modifier,
    onPicked: (PickedContact?) -> Unit,
) {
    val pick = rememberContactPicker(onPicked = onPicked)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(UI.colors.pureInverse.copy(alpha = 0.06f), RoundedCornerShape(percent = 50))
            .clickable { pick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_vue_people_person),
            contentDescription = null,
            tint = UI.colors.pureInverse,
            modifier = Modifier.size(18.dp)
        )
        androidx.compose.foundation.layout.Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.pick_contact),
            style = androidx.compose.ui.text.TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = UI.colors.pureInverse
            )
        )
    }
}

/**
 * Reads name + number from the URI the system picker returned.
 * Only that single contact is readable (temporary grant).
 */
private fun readPickedContact(
    context: Context,
    uri: Uri,
): PickedContact {
    var name: String? = null
    var phone: String? = null
    try {
        context.contentResolver.query(
            uri,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
            ),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                if (nameIndex >= 0) name = cursor.getString(nameIndex)
                if (numberIndex >= 0) phone = cursor.getString(numberIndex)
            }
        }
    } catch (e: Exception) {
        // Denied or provider gone — manual entry still works
    }
    return PickedContact(name = name, phone = phone)
}
