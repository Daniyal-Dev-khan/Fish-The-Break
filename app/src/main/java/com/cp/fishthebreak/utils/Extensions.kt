package com.cp.fishthebreak.utils

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import android.text.Html
import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.format.DateFormat.is24HourFormat
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.NavHostFragment
import com.android.billingclient.api.Purchase
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.cp.fishthebreak.R
import com.cp.fishthebreak.di.MyApplication
import com.cp.fishthebreak.models.LatLangToPickerData
import com.cp.fishthebreak.screens.bottom_sheets.locations.LatLangBottomSheet
import com.cp.fishthebreak.utils.Constants.Companion.DEEP_LINK_URL
import com.cp.fishthebreak.utils.Constants.Companion.FISH_LOG_TYPE
import com.cp.fishthebreak.utils.Constants.Companion.LOCATION_TYPE
import com.cp.fishthebreak.utils.Constants.Companion.ROUTE_TYPE
import com.cp.fishthebreak.utils.Constants.Companion.TROLLING_TYPE
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.tapadoo.alerter.Alerter
import org.json.JSONObject
import org.ocpsoft.prettytime.PrettyTime
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.math.abs


fun TextView.makeLinks(vararg links: Pair<String, View.OnClickListener>) {
    val spannableString = SpannableString(this.text)
    var startIndexOfLink = -1
    for (link in links) {
        val clickableSpan = object : ClickableSpan() {
            override fun updateDrawState(textPaint: TextPaint) {
                // use this to change the link color
                //textPaint.color = textPaint.linkColor
                // toggle below value to enable/disable
                // the underline shown below the clickable text
                //textPaint.isUnderlineText = true
            }

            override fun onClick(view: View) {
                Selection.setSelection((view as TextView).text as Spannable, 0)
                view.invalidate()
                link.second.onClick(view)
            }
        }
        startIndexOfLink = this.text.toString().indexOf(link.first, startIndexOfLink + 1)
//      if(startIndexOfLink == -1) continue // todo if you want to verify your texts contains links text
        spannableString.setSpan(
            clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    this.movementMethod =
        LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
}

fun CheckBox.makeLinks(vararg links: Pair<String, View.OnClickListener>) {
    val spannableString = SpannableString(this.text)
    var startIndexOfLink = -1
    for (link in links) {
        val clickableSpan = object : ClickableSpan() {
            override fun updateDrawState(textPaint: TextPaint) {
                // use this to change the link color
                textPaint.color =
                    ContextCompat.getColor(this@makeLinks.context, R.color.terms_color)
                // toggle below value to enable/disable
                // the underline shown below the clickable text
                //textPaint.isUnderlineText = true
            }

            override fun onClick(view: View) {
                view.cancelPendingInputEvents() // Prevent CheckBox state from being toggled when link is clicked
                Selection.setSelection((view as TextView).text as Spannable, 0)
                view.invalidate()
                link.second.onClick(view)
            }
        }
        startIndexOfLink = this.text.toString().indexOf(link.first, startIndexOfLink + 1)
//      if(startIndexOfLink == -1) continue // todo if you want to verify your texts contains links text
        spannableString.setSpan(
            clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    this.movementMethod =
        LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
}

fun CheckBox.addClickableLink(fullText: String, linkText: SpannableString, callback: () -> Unit) {
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            widget.cancelPendingInputEvents() // Prevent CheckBox state from being toggled when link is clicked
            callback.invoke()
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = true // Show links with underlines
        }
    }
    linkText.setSpan(clickableSpan, 0, linkText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    val fullTextWithTemplate = fullText.replace(linkText.toString(), "^1", false)
    val cs = TextUtils.expandTemplate(fullTextWithTemplate, linkText)

    text = cs
    movementMethod = LinkMovementMethod.getInstance() // Make link clickable
}

fun View.setOnSingleClickListener(wait: Long = 100L, block: () -> Unit) {
    setOnClickListener(OnSingleClickListener(block, wait))
}

fun EditText.applyAsteriskPasswordTransformation() {
    this.transformationMethod = AsteriskPasswordTransformationMethod()
}

fun EditText.hideShowPassword(toggle: ImageView) {
    if (this.transformationMethod.equals(HideReturnsTransformationMethod.getInstance())) {
        toggle.setImageResource(R.drawable.ic_password_show)
        this.transformationMethod = AsteriskPasswordTransformationMethod()
    } else {
        toggle.setImageResource(R.drawable.ic_password_hide)
        this.transformationMethod = HideReturnsTransformationMethod.getInstance()
    }
    this.setSelection(this.text.toString().length)
}

class GenericKeyEvent internal constructor(
    private val currentView: EditText,
    private val previousView: EditText?
) : View.OnKeyListener {
    override fun onKey(p0: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL
            && currentView.id != R.id.etCode1 && currentView.text.isEmpty()
        ) {
            //If current is empty then previous EditText's number will also be deleted
            previousView?.text = null
            previousView?.requestFocus()
            return true
        }
        return false
    }

}

// ViewExtensions

// TextViewExtensions

fun TextView.setTextAnimation(
    text: String,
    duration: Long = 300,
    completion: (() -> Unit)? = null
) {
    fadOutAnimation(duration) {
        this.text = text
        fadInAnimation(duration) {
            completion?.let {
                it()
            }
        }
    }
}

fun View.fadOutAnimation(
    duration: Long = 300,
    visibility: Int = View.INVISIBLE,
    completion: (() -> Unit)? = null
) {
    animate()
        .alpha(0.1f)
        .setDuration(duration)
        .withEndAction {
            //this.visibility = visibility
            completion?.let {
                it()
            }
        }
}

fun View.fadInAnimation(duration: Long = 300, completion: (() -> Unit)? = null) {
    //alpha = 0f
    //visibility = View.VISIBLE
    animate()
        .alpha(1f)
        .setDuration(duration)
        .withEndAction {
            completion?.let {
                it()
            }
        }
}

fun CharSequence?.isValidEmail() =
    !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isValidPasswordFormat(): Boolean {
    val passwordREGEX = Pattern.compile(
        "^" +
                "(?=.*[0-9])" +         //at least 1 digit
                "(?=.*[a-z])" +         //at least 1 lower case letter
                "(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +      //any letter
                "(?=.*[@#$%^&+=!?,.;/~*()<>:'_\"-])" +    //at least 1 special character
                "(?=\\S+$)" +           //no white spaces
                ".{8,}" +               //at least 8 characters
                "$"
    );
    return passwordREGEX.matcher(this).matches()
}

fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    return true
                }

                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    return true
                }

                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    return true
                }
            }
        }
    } else {
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
            return true
        }
    }
    return false
}

fun Context.getMyDeviceId(): String {
    return Settings.Secure.getString(
        this.contentResolver,
        Settings.Secure.ANDROID_ID
    )
}

fun Activity.showToast(text: String, isSuccess: Boolean) {
    val activity = this
    if (isSuccess) {
        Alerter.create(activity, R.layout.top_alert_layout)
            .setBackgroundColorRes(R.color.alert_success_bg)
            .also { alerter ->
                val tvTitle = alerter.getLayoutContainer()?.findViewById<TextView>(R.id.tvTitle)
                val tvBody = alerter.getLayoutContainer()?.findViewById<TextView>(R.id.tvBody)
                tvTitle?.text = activity.resources.getString(R.string.toast_success)
                tvBody?.text = text
            }
            .setDuration(1000)
            .show()
    } else {
        Alerter.create(activity, R.layout.top_alert_layout)
            .setBackgroundColorRes(R.color.alert_error_bg)
            .also { alerter ->
                val tvTitle = alerter.getLayoutContainer()?.findViewById<TextView>(R.id.tvTitle)
                val tvBody = alerter.getLayoutContainer()?.findViewById<TextView>(R.id.tvBody)
                tvTitle?.text = activity.resources.getString(R.string.toast_error)
                tvBody?.text = text
            }
            .setDuration(1000)
            .show()
    }
}

fun Dialog.showToast(context: Context, text: String, isSuccess: Boolean) {
    val activity = this
    if (isSuccess) {
        Alerter.create(activity, R.layout.top_alert_layout)
            .setBackgroundColorRes(R.color.alert_success_bg)
            .also { alerter ->
                val tvTitle = alerter.getLayoutContainer()?.findViewById<TextView>(R.id.tvTitle)
                val tvBody = alerter.getLayoutContainer()?.findViewById<TextView>(R.id.tvBody)
                tvTitle?.text = context.resources.getString(R.string.toast_success)
                tvBody?.text = text
            }
            .setDuration(1000)
            .show()
    } else {
        Alerter.create(activity, R.layout.top_alert_layout)
            .setBackgroundColorRes(R.color.alert_error_bg)
            .also { alerter ->
                val tvTitle = alerter.getLayoutContainer()?.findViewById<TextView>(R.id.tvTitle)
                val tvBody = alerter.getLayoutContainer()?.findViewById<TextView>(R.id.tvBody)
                tvTitle?.text = context.resources.getString(R.string.toast_error)
                tvBody?.text = text
            }
            .setDuration(1000)
            .show()
    }
}

fun String.isValidateUsername(): Boolean {
    val passwordREGEX = Pattern.compile(
        "^" +
                //"(?=.*[0-9])" +         //at least 1 digit
                //"(?=.*[a-z])" +         //at least 1 lower case letter
                //"(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[a-zA-Z0-9_])" +      //any letter
                //"(?=.*[@#$%^&+=!?,.;/~*()<>:'_\"-])" +    //at least 1 special character
                "(?=\\S+$)" +           //no white spaces
                ".{4,20}" +               //at least 8 characters max 20 characters
                "$"
    )
    return passwordREGEX.matcher(this).matches()
    /*
    if (Regex.IsMatch(text, @"
    # Validate username with 5 constraints.
    ^                          # Anchor to start of string.
    # 1- only contains alphanumeric characters , underscore and dot.
    # 2- underscore and dot can't be at the end or start of username,
    # 3- underscore and dot can't come next to each other.
    # 4- each time just one occurrence of underscore or dot is valid.
    (?=[A-Za-z0-9]+(?:[_.][A-Za-z0-9]+)*$)
    # 5- number of characters must be between 8 to 20.
    [A-Za-z0-9_.]{8,20}        # Apply constraint 5.
    $                          # Anchor to end of string.
    ", RegexOptions.IgnorePatternWhitespace))
{
    // Successful match
} else {
    // Match attempt failed
}
     */
}

fun View.viewVisible() {
    this.visibility = View.VISIBLE
}

fun View.viewInVisible() {
    this.visibility = View.INVISIBLE
}

fun View.viewGone() {
    this.visibility = View.GONE
}

fun String?.getOnErrorMessage(): String {
    return try {
        val jObjError =
            JSONObject(this.toString())
        jObjError.get("message").toString()
    } catch (e: Exception) {
        this ?: MyApplication.appContext.resources.getString(
            R.string.something_went_wrong
        )
    }
}

fun Context.hideKeyboardFrom(view: View) {
    val imm = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Context.showKeyboard(view: View) {
    val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

fun EditText.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun EditText.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}

fun ImageView.loadImage(
    url: String?,
    @DrawableRes placeholder: Int = R.drawable.place_holder_square,
    @DrawableRes errorDrawable: Int = R.drawable.place_holder_square
) {
    if (!url.isNullOrEmpty() && url != Constants.IMG_URL && !url.contains("null")) {
        Glide.with(this)
            .load(
                if (url.contains("/storage/emulated")) {//load photo from phone storage
                    "/storage/emulated" + url.split("/storage/emulated")[1]
                } else {
                    url
                }
            )
            .error(errorDrawable)
            .placeholder(placeholder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(this)
    } else {
        Glide.with(this)
            .load(placeholder)
            .error(errorDrawable)
            .placeholder(placeholder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(this)
    }
}

fun Activity.back(navHostFragment: NavHostFragment?) {
    try {
        if (navHostFragment != null && navHostFragment.childFragmentManager.backStackEntryCount == 0) {
            // First fragment is open, backstack is empty
            this.finish()
        } else {
            // there are fragments in backstack
            navHostFragment?.navController?.navigateUp()
        }
    } catch (ex: Exception) {

    }
}

fun EditText.onSearch(callback: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            callback.invoke()
            return@setOnEditorActionListener true
        }
        false
    }
}

fun EditText.onDone(callback: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            callback.invoke()
            return@setOnEditorActionListener true
        }
        false
    }
}

fun Bitmap.compressMyImage(cacheDir: File, f_name: String): File? {
    val f = File(cacheDir, "user$f_name.jpg")
    f.createNewFile()
    ByteArrayOutputStream().use { stream ->
        compress(Bitmap.CompressFormat.JPEG, 50, stream)
        val bArray = stream.toByteArray()
        FileOutputStream(f).use { os -> os.write(bArray) }
    }//stream
    return f
}

fun isNeedToCompressImage(filepath: String): Boolean {
    val file: File = File(filepath)
    val fileSizeInBytes = file.length()
    val fileSizeInKB: Float = fileSizeInBytes / 1024.0f
    // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
    // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
    Log.i("file_size->", fileSizeInKB.toString())
    val fileSizeInMB = fileSizeInKB / 1024.0f
    return fileSizeInMB > 1
}

fun Context.clearAppData() {
    try {
        Runtime.getRuntime().exec("pm clear " + this.packageName)
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}

fun Context.deleteAppCache() {
    try {
        val dir = this.cacheDir
        deleteDir(dir)
    } catch (e: java.lang.Exception) {
    }
}

fun Context.deleteAppDownloadCache() {
    try {
        val dir = File(
            Environment.getExternalStorageDirectory(),
            "/Download/FishTheBreak"
        )
        deleteDir(dir)
    } catch (e: java.lang.Exception) {
    }
}

fun Context.deleteMapData(date: Long) {
    try {
        val dir = File(
            Environment.getExternalStorageDirectory(),
            "/Download/FishTheBreak/map_${date}"
        )
        deleteDir(dir)
    } catch (e: java.lang.Exception) {
    }
}

fun Context.getMapPathToLoadOffline(path: String): String {
    try {
        val dir = File(
            path
        )
        if (dir.isDirectory) {
            val children = dir.list()
            if (children != null) {
                for (i in children.indices) {
                    val file = File(dir, children[i])
                    if (file.path.contains(".vtpk")) {
                        return file.path
                    }
                }
            }
        }
    } catch (e: java.lang.Exception) {
    }
    return ""
}

fun Context.getMapPathToLoadOfflineDb(path: String): List<String> {
    val list = ArrayList<String>()
    try {
        val dir = File(
            path
        )
        if (dir.isDirectory) {
            val children = dir.list()
            if (children != null) {
                for (i in children.indices) {
                    val file = File(dir, children[i])
                    if (file.path.contains(".geodatabase")) {
                        list.add(file.path)
                    }
                }
            }
        }
    } catch (e: java.lang.Exception) {
    }
    return list
}

private fun deleteDir(dir: File?): Boolean {
    return if (dir != null && dir.isDirectory) {
        val children = dir.list()
        if (children != null) {
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
        }
        dir.delete()
    } else if (dir != null && dir.isFile) {
        dir.delete()
    } else {
        false
    }
}

fun EditText.transformIntoDatePicker(context: Context, format: String, maxDate: Date? = null) {
    isFocusableInTouchMode = false
    isClickable = true
    isFocusable = false
    val myCalendar = Calendar.getInstance()
    val datePickerOnDataSetListener =
        DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val sdf = SimpleDateFormat(format, Locale.UK)
            setText(sdf.format(myCalendar.time))
        }
    val cal = Calendar.getInstance()
    cal.time = maxDate
    cal.add(Calendar.DAY_OF_MONTH, -1)
    maxDate?.time = cal.time.time
    setOnClickListener {
        val selectedDate: Date? = this.text.toString().toDate(format)
        if (selectedDate != null) {
            myCalendar.time = selectedDate
        }
        DatePickerDialog(
            context, datePickerOnDataSetListener, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
            myCalendar.get(Calendar.DAY_OF_MONTH)
        ).run {
            maxDate?.time?.also { datePicker.maxDate = it.minus(1) }
            show()
        }
    }
}

fun EditText.transformIntoYearPicker(activity: Activity) {
    isFocusableInTouchMode = false
    isClickable = true
    isFocusable = false
    setOnSingleClickListener {
        createDialogWithoutDateField(activity, this)
    }
}

private fun createDialogWithoutDateField(activity: Activity, editText: EditText) {

    val alertDialog: AlertDialog?
    val builder = AlertDialog.Builder(activity, R.style.AlertDialogStyle)
    val inflater = activity.layoutInflater

    val cal = Calendar.getInstance()

    val dialog = inflater.inflate(R.layout.year_picker_dialog, null)
    val yearPicker = dialog.findViewById(R.id.pickerYear) as NumberPicker


    val year = cal.get(Calendar.YEAR)
    yearPicker.minValue = 1900
    yearPicker.maxValue = year
    yearPicker.value = year

    builder.setView(dialog)
        .setPositiveButton(Html.fromHtml("<font color='#2DA0FA'>Ok</font>")) { dialogInterface, which ->
            //Toast.makeText(applicationContext,"clicked yes",Toast.LENGTH_LONG).show()

            val value = yearPicker.value
            editText.setText(value.toString())
            dialogInterface.cancel()
        }

    builder.setNegativeButton(Html.fromHtml("<font color='#2DA0FA'>Cancel</font>")) { dialogInterface, which ->
        dialogInterface.cancel()
    }

    alertDialog = builder.create()
    // Set other dialog properties
    alertDialog.setCancelable(true)
    alertDialog.show()
}

private fun createDialogWithoutDateField_(context: Context): DatePickerDialog? {
    val dpd = DatePickerDialog(context, null, 2014, 1, 24)
    try {
        val datePickerDialogFields = dpd.javaClass.declaredFields
        for (datePickerDialogField in datePickerDialogFields) {
            if (datePickerDialogField.name == "mDatePicker") {
                datePickerDialogField.isAccessible = true
                val datePicker = datePickerDialogField[dpd] as DatePicker
                val datePickerFields = datePickerDialogField.type.declaredFields
                for (datePickerField in datePickerFields) {
                    Log.i("test", datePickerField.name)
                    if ("mDaySpinner" == datePickerField.name) {
                        datePickerField.isAccessible = true
                        val dayPicker = datePickerField[datePicker]
                        (dayPicker as View).visibility = View.GONE
                    }
                }
            }
        }
    } catch (ex: java.lang.Exception) {
    }
    return dpd
}

fun TextInputEditText.transformIntoDatePicker(
    context: Context,
    format: String,
    fragmentManager: FragmentManager
) {
    isFocusableInTouchMode = false
    isClickable = true
    isFocusable = false

    val calendar = Calendar.getInstance()
    val mCalendar = Calendar.getInstance()
    calendar.timeInMillis = mCalendar.time.time
    calendar[Calendar.YEAR] = 1950
    val startDate = calendar.timeInMillis
    calendar.set(Calendar.YEAR, mCalendar.get(Calendar.YEAR))
    calendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH))
    calendar.set(Calendar.DAY_OF_MONTH, mCalendar.get(Calendar.DAY_OF_MONTH))
    val endDate = calendar.timeInMillis
    setOnSingleClickListener {
        val selectedDate: Date? = this.text.toString().toDate(format)
        if (selectedDate != null) {
            mCalendar.time = selectedDate
            mCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        val constraints: CalendarConstraints = CalendarConstraints.Builder()
            .setOpenAt(mCalendar.timeInMillis)
            .setStart(startDate)
            .setEnd(endDate)
            .setValidator(DateValidatorPointBackward.now())
            .build()

        val picker = MaterialDatePicker
            .Builder
            .datePicker()
            .setCalendarConstraints(constraints)
            .setSelection(mCalendar.timeInMillis)
            .setTitleText("Select date")
            .build()
            .apply {
                addOnCancelListener { }
                addOnDismissListener { }
                addOnPositiveButtonClickListener {
                    val sdf = SimpleDateFormat(format, Locale.getDefault())
                    val date = sdf.format(it)
                    setText(date ?: "")
                }
                addOnNegativeButtonClickListener { }
            }
        picker.show(fragmentManager, "MaterialDatePicker")
    }
}

fun TextInputEditText.transformIntoTimePicker(
    context: Context,
    fragmentManager: FragmentManager,
    maxDate: Date? = null
) {
    isFocusableInTouchMode = false
    isClickable = true
    isFocusable = false
    val cal = Calendar.getInstance()
//    val isSystem24Hour = is24HourFormat(context)
    val isSystem24Hour = true
    val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
    val picker =
        MaterialTimePicker.Builder()
            .setTheme(R.style.My_MaterialTimePickerTheme)
            .setTimeFormat(clockFormat)
            .setHour(cal.get(Calendar.HOUR_OF_DAY))
            .setMinute(cal.get(Calendar.MINUTE))
            .setTitleText("Select time")
            .build().apply {
                addOnCancelListener { /* on cancel */ }
                addOnDismissListener { /* on dismiss */ }
                addOnPositiveButtonClickListener {
//                    setText("$hour : $minute")
                    setText(String.format("%02d : %02d", hour, minute))
                }
                addOnNegativeButtonClickListener { /* on negative button click */ }
            }

    setOnSingleClickListener {
        picker.show(fragmentManager, "MaterialTimePicker")
    }
}

fun String?.isSubscriptionExpired(): Boolean {
    if (this?.trim().isNullOrEmpty()) {
        return true
    }
    val subscriptionExpireDate = this?.toDate("yyyy-MM-dd")
    return (subscriptionExpireDate?.time?:-1) < (Date().toFormat("yyyy-MM-dd").toDate("yyyy-MM-dd")?.time?:0)
}

fun String.toDate(format: String = "yyyy-MM-dd HH:mm:ss"): Date? {
    if (this.trim().isEmpty()) {
        return null
    }
    val format = SimpleDateFormat(format, Locale.getDefault())
    return try {
        format.parse(this)
    } catch (e: Exception) {
        null
    }
}

fun Date.getRemainingTrialDays(): Int {
    val currentDate = Date()
    return if (this.toFormat("yyyy-MM-dd") == currentDate.toFormat("yyyy-MM-dd")) {
        1
    } else {
        TimeUnit.MILLISECONDS.toDays((this.time - currentDate.time)).toInt() + 1
    }
}

fun Context.getNotificationIcon(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        R.drawable.ic_notification
    } else {
        R.drawable.ic_notification
    }
}

fun Long.toDate(productId: String): Date {
    val date = Date(this)
    val cal = Calendar.getInstance()
    cal.time = date
    if (productId == "one_month_sub") {
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1)
        return cal.time
    } else if (productId == "one_year_sub") {
        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 1)
        return cal.time
    }
    return Date()
}

fun String.getProductNameFromProductId(): String {
    if (this == "one_month_sub") {
        return "Monthly"
    } else if (this == "one_year_sub") {
        return "Yearly"
    }
    return this
}

fun String.getProductTimePeriod(): String {
    if (this == "one_month_sub") {
        return "1"
    } else if (this == "one_year_sub") {
        return "12"
    }
    return "0"
}

fun Date.toFormat(format: String = "yyyy-MM-dd"): String {
    val format = SimpleDateFormat(format)
    return try {
        format.format(this)
    } catch (e: Exception) {
        ""
    }
}

fun Date?.getTrollingTime(pauseTime: Long): String? {
    val startTime = this
//    val expectedTime = "2023-01-04 17:33:13".toDate()
    val currentTime = Date()
    if (startTime != null) {
        val time = getDiff(currentTime, startTime, pauseTime)
        val timePartList = time?.split(":")
        if (!timePartList.isNullOrEmpty()) {
            var remaining = ""
            timePartList.forEachIndexed { index, s ->
                when (index) {
                    0 -> {
                        if (s.trim() != "00") {
                            remaining += "${s}:"
                        }
                    }

                    1 -> {
                        //if (s.trim() != "00m") {
                        remaining += "${s}:"
                        //}
                    }

                    2 -> {
                        //if (s.trim() != "00s") {
                        remaining += s
                        //}
                    }
                }
            }
            return if (remaining.isNotEmpty()) {
                remaining.trim()
            } else {
                time.trim()
            }
        }
        return time?.trim()
    }
    return "00:00"
}

fun Long.getTrollingTime(endTime: Long): String? {
    val startTime = Date()
    startTime.time = this
//    val expectedTime = "2023-01-04 17:33:13".toDate()
    val currentTime = Date()
    currentTime.time = endTime
    if (startTime != null) {
        val time = getDiff(currentTime, startTime)
        val timePartList = time?.split(":")
        if (!timePartList.isNullOrEmpty()) {
            var remaining = ""
            timePartList.forEachIndexed { index, s ->
                when (index) {
                    0 -> {
                        if (s.trim() != "00") {
                            remaining += "${s}:"
                        }
                    }

                    1 -> {
                        //if (s.trim() != "00m") {
                        remaining += "${s}:"
                        //}
                    }

                    2 -> {
                        //if (s.trim() != "00s") {
                        remaining += s
                        //}
                    }
                }
            }
            return if (remaining.isNotEmpty()) {
                remaining.trim()
            } else {
                time.trim()
            }
        }
        return time?.trim()
    }
    return "00:00"
}

private fun getDiff(date1: Date, date2: Date, pauseTime: Long): String? {
//    val diffInTime = ((date1.time - date2.time) / (1000 * 60 * 60 *
//            24)) as Long
    val diffInTime: Long = abs((date1.time - date2.time - pauseTime))
//    return TimeUnit.MILLISECONDS.toMinutes(diffInTime).toInt().toString()
    return parseTime(
        if (diffInTime < 0) {
            0
        } else {
            diffInTime
        }
    )
}

private fun getDiff(date1: Date, date2: Date): String? {
//    val diffInTime = ((date1.time - date2.time) / (1000 * 60 * 60 *
//            24)) as Long
    val diffInTime: Long = abs((date1.time - date2.time))
//    return TimeUnit.MILLISECONDS.toMinutes(diffInTime).toInt().toString()
    return parseTime(
        if (diffInTime < 0) {
            0
        } else {
            diffInTime
        }
    )
}

private fun parseTime(milliseconds: Long): String? {
//    val FORMAT = "%02dh:%02dm:%02ds"
    val FORMAT = "%02d:%02d:%02d"
    return java.lang.String.format(
        Locale.getDefault(),
        FORMAT,
        TimeUnit.MILLISECONDS.toHours(milliseconds),
        TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(
            TimeUnit.MILLISECONDS.toHours(milliseconds)
        ),
        TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(
            TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        )
    )
}

fun String.timeAgo(format: String = "yyyy-MM-dd kk:mm:ss"): String {
    val p = PrettyTime(Locale.getDefault())
    val localDate = onUtcToLocal(this, format)
    val dtObj = convertToDateObj(localDate, format)
//    val dtObj = this.toDate(format)
    return p.format(dtObj)
}

private fun onUtcToLocal(s: String, format: String = "yyyy-MM-dd kk:mm:ss"): String {
    try {
//        val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
        val utcFormat = SimpleDateFormat(format, Locale.getDefault())
//        val utcFormat = SimpleDateFormat(format, Locale.getDefault())
        utcFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date: Date?
        date = utcFormat.parse(s)
        val localTimeFormate = SimpleDateFormat(format, Locale.getDefault())
        localTimeFormate.timeZone = TimeZone.getDefault()
        localTimeFormate.parse(localTimeFormate.format(date ?: Date()))
        //            prettyTime.
        return localTimeFormate.format(date ?: Date())
    } catch (e: ParseException) {
        try {
            val utcFormat = SimpleDateFormat(format, Locale.getDefault())
            utcFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date: Date?
            date = utcFormat.parse(s)
            val localTimeFormate = SimpleDateFormat(format, Locale.getDefault())
            localTimeFormate.timeZone = TimeZone.getDefault()
            localTimeFormate.parse(localTimeFormate.format(date ?: Date()))
            //            prettyTime.
            return localTimeFormate.format(date ?: Date())
        } catch (ex: ParseException) {
            ex.printStackTrace()
        }
        //
    }

    return ""
}

private fun convertToDateObj(date_string: String, format: String = "yyyy-MM-dd kk:mm:ss"): Date? {
    val dateFormat = SimpleDateFormat(format, Locale.getDefault())
    try {
        return dateFormat.parse(date_string)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return Date()
}

fun Context.spanTextColor(
    text1: String,
    @ColorInt text1Color: Int,
    typeface1: Int,
    text2: String,
    @ColorInt text2Color: Int,
    typeface2: Int
): SpannableString {
    val context = this
    val spannableString = SpannableString(text1 + text2)
    spannableString.setSpan(
        ForegroundColorSpan(text1Color),
        0,
        text1.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    spannableString.setSpan(
        object : TypefaceSpan(null) {
            override fun updateDrawState(ds: TextPaint) {
                ds.typeface = Typeface.create(
                    ResourcesCompat.getFont(context, typeface1),
                    Typeface.NORMAL
                ) // To change according to your need
            }
        },
        0,
        text1.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    spannableString.setSpan(
        object : TypefaceSpan(null) {
            override fun updateDrawState(ds: TextPaint) {
                ds.typeface = Typeface.create(
                    ResourcesCompat.getFont(context, typeface2),
                    Typeface.NORMAL
                ) // To change according to your need
            }
        },
        text1.length,
        text1.length + text2.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    spannableString.setSpan(
        ForegroundColorSpan(text2Color),
        text1.length,
        text1.length + text2.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    return spannableString
}

fun String.timeToAmPm(format: String): String {
    return try {
        val formatParser = SimpleDateFormat(format, Locale.getDefault())
        formatParser.timeZone = TimeZone.getTimeZone("UTC")
        val formatShort = SimpleDateFormat("hh:mm aa", Locale.getDefault())
//        formatShort.format(formatParser.parse(this).gmtToLocalDate())
        (formatShort.format(formatParser.parse(this) ?: Date())).replace(".", "").uppercase()
    } catch (ex: Exception) {
        this
    }
//    return try {
//        val formatParser = SimpleDateFormat("kk:mm:ss")
//        formatParser.timeZone = TimeZone.getTimeZone("UTC")
//        val formatShort = SimpleDateFormat("hh:mm aa")
//        formatShort.format(formatParser.parse(this))
//    }
//    catch (ex: Exception){
//        this
//    }
}

fun String.getTrollingTimeInSeconds(): Long {
    try {
        val time = this.split(":")
        var seconds = 0L
        when (time.size) {
            0 -> {}
            1 -> {
                seconds = this.toLong()
            }

            2 -> {
                seconds += (time[0].toLong() * 60L) + time[1].toLong()
            }

            3 -> {
                seconds += (time[0].toLong() * 60L * 60L) + (time[1].toLong() * 60L) + time[2].toLong()
            }
        }
        return seconds
    } catch (ex: Exception) {
        return 0L
    }
}

fun String.roundOffDecimal(): Double? {
    val df = DecimalFormat("#.##")
    df.roundingMode = RoundingMode.CEILING
    return df.format(this.toDouble()).toDouble()
}

fun Float.roundOffDecimal(): Double? {
    val df = DecimalFormat("#.##")
    df.roundingMode = RoundingMode.CEILING
    return df.format(this.toDouble()).toDouble()
}

fun Double.roundOffDecimal(): Double {
    val df = DecimalFormat("#.##")
    df.roundingMode = RoundingMode.CEILING
    return df.format(this).toDouble()
}

fun kAndMFormatter(number: Int): String {
    var numberString = ""
    numberString = if (Math.abs(number / 1000000) >= 1) {
        (number / 1000000).toString() + "m"
    } else if (Math.abs(number / 1000) >= 1) {
        (number / 1000).toString() + "k"
    } else {
        number.toString()
    }
    return numberString
}

private fun Context.shareLink(text: String) {

    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    this.startActivity(shareIntent)
}

fun Context.createUrlANdShare(type: String, pointId: Int) {
    val preference = SharePreferenceHelper.getInstance(MyApplication.appContext)
    val sharedType = when (type) {
        FISH_LOG_TYPE -> {
            "Fish Log"
        }

        LOCATION_TYPE -> {
            "Location"
        }

        TROLLING_TYPE -> {
            "Trolling"
        }

        ROUTE_TYPE -> {
            "Route"
        }

        else -> {
            type
        }
    }
    val data =
        "@${preference.getUser()?.username} shared a $sharedType with you\n\n${DEEP_LINK_URL}${type}/${
            pointId.toString().encodeToBase64()
        }"
    this.shareLink(data)
}
fun Context.createUrlANdInvite() {
    this.shareLink("Hey! I am inviting you to join/download Fish the Break. Install the app and let's plan our next fishing trip together!\n\n${DEEP_LINK_URL}")
}

fun String.decodeFromBase64(): String {
    return Base64.decode(this, Base64.DEFAULT).toString(charset("UTF-8"))
}

fun String.encodeToBase64(): String {
    return Base64.encodeToString(this.toByteArray(charset("UTF-8")), Base64.DEFAULT)
}

fun View.showSnack(msg: String) {
    Snackbar.make(this, msg, Snackbar.LENGTH_INDEFINITE).apply {
        setAction(resources.getString(R.string.dismiss)) {
            dismiss()
        }
        setTextMaxLines(5)
        setActionTextColor(resources.getColor(android.R.color.holo_red_light, null))
        show()
    }
}

fun String.formatStringAsPhoneNumber(): String {
    try {
        return when (this.length) {
            7 -> String.format("%s-%s", this.substring(0, 3), this.substring(3, 7))
            10 -> String.format(
                "%s %s%s",
                this.substring(0, 3),
                this.substring(3, 6),
                this.substring(6, 10)
            )

            11 -> String.format(
                "%s %s %s%s",
                this.substring(0, 1),
                this.substring(1, 4),
                this.substring(4, 7),
                this.substring(7, 11)
            )

            12 -> String.format(
//                "+%s %s %s%s",
                "%s %s %s%s",
                this.substring(0, 2),
                this.substring(2, 5),
                this.substring(5, 8),
                this.substring(8, 12)
            )

            13 -> String.format(
                "%s %s %s%s",
                this.substring(0, 3),
                this.substring(3, 6),
                this.substring(6, 9),
                this.substring(9, 13)
            )

            else -> return this
        }
    } catch (ex: Exception) {
        return this
    }
}

fun String.isValidLatLang(): Boolean {
    //Modified to accept white-space on both sides of the comma: ^[-+]?([1-8]?\d(\.\d+)?|90(\.0+)?)\s*,\s*[-+]?(180(\.0+)?|((1[0-7]\d)|([1-9]?\d))(\.\d+)?)$
    val passwordREGEX = Pattern.compile(
        "^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?)\\s*,\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)\$"
    )
    return passwordREGEX.matcher(this).matches()
}

/**
 * return true if in App's Battery settings "Not optimized" and false if "Optimizing battery use"
 */
fun Context.isIgnoringBatteryOptimizations(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val pwrm = this.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = this.applicationContext.packageName
        return pwrm.isIgnoringBatteryOptimizations(name)
    }else {
        return true
    }
}

fun Purchase?.getProductIdFromPurchase(): String{
    if(this == null){
        return ""
    }
    return JSONObject(this.originalJson).get("productId").toString()
}
fun ArrayList<Purchase>.getProductIdFromPurchase(): String{
    if(this.isEmpty()){
        return ""
    }
    return JSONObject(this.first().originalJson).get("productId").toString()
}
fun String?.getProductIdFromPurchase(): String{
    if(this.isNullOrEmpty()){
        return ""
    }
    return JSONObject(this).get("productId").toString()
}
fun String.updateCapitalizedTextByRemovingUnderscore(
    specialChar: String  = "_") : String {
    var tabName = this
    // removing the special character coming in parameter & if exist
    if (specialChar.isNotEmpty() && this.contains(specialChar)) {
        tabName = this.replace(specialChar, " ")
    }
    return tabName.lowercase().split(' ').joinToString(" ") { data ->
        data.replaceFirstChar { if (it.isLowerCase())
            it.titlecase(Locale.getDefault()) else it.toString() } }
}

fun getNauticalLatitude(latitude: Double): String {
    val latDegree = latitude.toInt()
    val latMinute = ((latitude - latDegree) * 60).toInt()
    val latSecond = ((latitude - latDegree - latMinute.toDouble() / 60) * 3600).toInt()

    val latDir = if (latitude >= 0) "N" else "S"

    return "${abs(latDegree)}°${abs(latMinute)}'${abs(latSecond)}\"$latDir"
}
/*fun getNauticalLatitude(latitude: Double): String {
    var latSeconds = (latitude * 3600).toInt()

    val latDegrees = latSeconds / 3600

    latSeconds = abs(latSeconds % 3600)

    val latMinutes = latSeconds / 60

    latSeconds %= 60

    return String.format("%d°%d'%d\"%s",
        abs(latDegrees),
        latMinutes,
        latSeconds,
        if (latDegrees >= 0)  {"N"} else {"S"}
    )

}*/

fun getNauticalLongitude(longitude: Double): String {
    val lonDegree = longitude.toInt()
    val lonMinute = ((longitude - lonDegree) * 60).toInt()
    val lonSecond = ((longitude - lonDegree - lonMinute.toDouble() / 60) * 3600).toInt()

    val lonDir = if (longitude >= 0) "E" else "W"

    return "${abs(lonDegree)}°${abs(lonMinute)}'${abs(lonSecond)}\"$lonDir"

}
/*fun getNauticalLongitude(longitude: Double): String {
    var longSeconds = (longitude * 3600).toInt()

    val longDegrees = longSeconds / 3600

    longSeconds = abs(longSeconds % 3600)

    val longMinutes = longSeconds / 60

    longSeconds %= 60

    return String.format("%d°%d'%d\"%s",
        abs(longDegrees),
        longMinutes,
        longSeconds,
        if(longDegrees >= 0) {"E"} else {"W"}
    )

}*/

fun convertCoordinatesToLatLng(coordinateString: String): Pair<Double, Double>? {
    try {
        val regex = """(-?\d+)°(\d+)'(\d+)"([NS]),(-?\d+)°(\d+)'(\d+)"([EW])""".toRegex()
        val matchResult = regex.find(coordinateString.replace(" ",""))
        if (matchResult != null) {
            val (latDeg, latMin, latSec, latDir, lonDeg, lonMin, lonSec, lonDir) = matchResult.destructured
            val latitude = latDeg.toDouble() + latMin.toDouble() / 60 + latSec.toDouble() / 3600
            val longitude = lonDeg.toDouble() + lonMin.toDouble() / 60 + lonSec.toDouble() / 3600
            return when (latDir) {
                "S" -> Pair(-latitude, if (lonDir == "W") -longitude else longitude)
                "N" -> Pair(latitude, if (lonDir == "W") -longitude else longitude)
                else -> null
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

/*
fun convertCoordinatesToLatLng(coordinateString: String): Pair<Double, Double>? {
    try {
        val regex = """(-?\d+)° (\d+)' (\d+)" ([NS]),\s*(-?\d+)° (\d+)' (\d+)" ([EW])""".toRegex()
        val matchResult = regex.find(coordinateString)
        if (matchResult != null) {
            val (latDeg, latMin, latSec, latDir, lonDeg, lonMin, lonSec, lonDir) = matchResult.destructured
            val latitude = latDeg.toDouble() + latMin.toDouble() / 60 + latSec.toDouble() / 3600
            val longitude = lonDeg.toDouble() + lonMin.toDouble() / 60 + lonSec.toDouble() / 3600
            return when (latDir) {
                "S" -> Pair(-latitude, if (lonDir == "W") -longitude else longitude)
                "N" -> Pair(latitude, if (lonDir == "W") -longitude else longitude)
                else -> null
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}
 */

fun String.getLatLangFromCoordinates(){
    val coordinateString = "48° 6' 59\" N, 122° 46' 31\" W"
    val latLng = convertCoordinatesToLatLng(this)
    if (latLng != null) {
        val (latitude, longitude) = latLng
        Log.i("getLatLangFromCoordinates","Latitude: $latitude, Longitude: $longitude")
    } else {
        Log.i("getLatLangFromCoordinates","Invalid coordinate string format.")
    }
}
fun String.isValidLatLangNauticalFormat(): Boolean {
    val regex = """^(-?\d{1,2})°(\d{1,2})'(\d{1,2})"([NS]),\s*(-?\d{1,3})°(\d{1,2})'(\d{1,2})"([EW])$""".toRegex()
    return regex.matches(this)
}
fun String.isValidLatNauticalFormat(): Boolean {
    val regex = """^(-?\d{1,2})°(\d{1,2})'(\d{1,2})"([NS])$""".toRegex()
    return regex.matches(this)
}
fun String.isValidLngNauticalFormat(): Boolean {
    val regex = """^(-?\d{1,3})°(\d{1,2})'(\d{1,2})"([EW])$""".toRegex()
    return regex.matches(this)
}
/*fun String.isValidLatNauticalFormat(): Boolean {
    val regex = """(-?\d+)°(\d+)'(\d+)"([NS])""".toRegex()
    val matchResult = regex.find(this.replace(" ",""))
    return matchResult != null
}
fun String.isValidLngNauticalFormat(): Boolean {
    val regex = """(-?\d+)°(\d+)'(\d+)"([EW])""".toRegex()
    val matchResult = regex.find(this.replace(" ",""))
    return matchResult != null
}*/

fun TextInputEditText.transformIntoLatPicker(
    fragmentManager: FragmentManager,
    isLongitude: Boolean
) {
    isFocusableInTouchMode = false
    isClickable = true
    isFocusable = false

    setOnSingleClickListener {
        val dialog = LatLangBottomSheet(text = this.text.toString().trim(), isLongitude = isLongitude, object: LatLangBottomSheet.OnItemClickListener{
            override fun onSelect(data: String) {
                setText(data)
            }

        })
        dialog.show(fragmentManager,"LatLangBottomSheet")
    }
}
fun EditText.transformIntoLatPicker(
    fragmentManager: FragmentManager,
    isLongitude: Boolean
) {
    isFocusableInTouchMode = false
    isClickable = true
    isFocusable = false

    setOnSingleClickListener {
        val dialog = LatLangBottomSheet(text = this.text.toString().trim(), isLongitude = isLongitude, object: LatLangBottomSheet.OnItemClickListener{
            override fun onSelect(data: String) {
                setText(data)
            }

        })
        dialog.show(fragmentManager,"LatLangBottomSheet")
    }
}

fun Float.KmToNauticalMiles(): Float{
    return (this / 1.852).toFloat()
}
fun Float.milesToNauticalMiles(): Float{
    return (this / 1.15078).toFloat()
}
fun Double.nauticalMilesToMiles(): Double{
    return (this*1.15078)
}
fun Float.KmToKnots(): Float{
    return (this *  0.539957).toFloat()
}

private fun String.uppercaseLetterRegEx(): Boolean {
    val regex = ".*[A-Z]+.*".toRegex()
    return regex.matches(this)
}
private fun String.lowercaseLetterRegEx(): Boolean {
    val regex = ".*[a-z]+.*".toRegex()
    return regex.matches(this)
}
private fun String.digitRegEx(): Boolean {
    val regex = ".*[0-9]+.*".toRegex()
    return regex.matches(this)
}
private fun String.specialCharacterRegEx(): Boolean {
    val regex = ".*[^A-Za-z0-9]+.*".toRegex()
    return regex.matches(this)
}
fun String.getPasswordValidationError(): String? {
    if (this.isEmpty()) {
        return "Password cannot be empty"
    } else  {
        var errorMessages = ""

        val minLength = 8

        if (!this.uppercaseLetterRegEx()) {
            errorMessages += "at least one uppercase letter"
        }

        if (!this.lowercaseLetterRegEx()) {
            errorMessages += " at least one lowercase letter"
        }

        if (!this.digitRegEx())  {
            errorMessages += " at least one digit"
        }

        if (!this.specialCharacterRegEx())  {
            errorMessages += " at least one special character"
        }

        if (this.count() < minLength) {
            errorMessages += " at least $minLength characters long"
        }

        if (errorMessages.isEmpty()) {
            return null
        } else {
            return "Password must contain $errorMessages."
        }
    }
}

fun String?.stringToDouble(): Double?{
    return try {
        if(this.isNullOrEmpty()){
            return null
        }
        this.toDouble()
    }catch (ex: Exception){
        null
    }
}

fun convertCoordinatesLatToPickerData(coordinateString: String): LatLangToPickerData? {
    try {
        val regex = """(-?\d+)°(\d+)'(\d+)"([NS])""".toRegex()
        val matchResult = regex.find(coordinateString.replace(" ", ""))
        if (matchResult != null) {
            val (latDeg, latMin, latSec, latDir) = matchResult.destructured
            return LatLangToPickerData(latDeg.toInt(), latMin.toInt(), latSec.toInt(), latDir)
        }
        return null
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun convertCoordinatesLangToPickerData(coordinateString: String): LatLangToPickerData? {
    try {
        val regex = """(-?\d+)°(\d+)'(\d+)"([EW])""".toRegex()
        val matchResult = regex.find(coordinateString.replace(" ", ""))
        if (matchResult != null) {
            val (lonDeg, lonMin, lonSec, lonDir) = matchResult.destructured
            return LatLangToPickerData(lonDeg.toInt(), lonMin.toInt(), lonSec.toInt(), lonDir)
        }
        return null
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}