package com.citrus.mCitrusTablet.view

import android.annotation.TargetApi
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import cn.pedant.SweetAlert.SweetAlertDialog
import com.citrus.mCitrusTablet.BuildConfig
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.di.prefs
import com.citrus.mCitrusTablet.util.*
import com.citrus.mCitrusTablet.util.Constants.KEY_LANGUAGE
import com.citrus.mCitrusTablet.util.ui.CustomAlertDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
@Suppress("DEPRECATED_IDENTITY_EQUALS", "DEPRECATION")
class MainActivity : AppCompatActivity() {
    private var currentApiVersion: Int = 0
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        currentApiVersion = Build.VERSION.SDK_INT
        val flags: Int = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            window.decorView.systemUiVisibility = flags
            val decorView: View = window.decorView
            decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN === 0) {
                    decorView.systemUiVisibility = flags
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateLanguage(this)
        setContentView(R.layout.activity_main)

        val navController: NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment?
        navController = navHostFragment!!.navController


        if(prefs.rsno == ""|| prefs.severDomain == ""){
            navController.navigate(R.id.settingFragment)
        }

        reservation_block.setOnClickListener{
            wait_block.setBackgroundResource(0)
            reservation_block.setBackgroundResource(R.drawable.bg_menu_select)
            navigateToTarget(R.id.reservationFragment)
        }

        wait_block.setOnClickListener{
            reservation_block.setBackgroundResource(0)
            wait_block.setBackgroundResource(R.drawable.bg_menu_select)
            navigateToTarget(R.id.waitFragment)
        }

        setting_block.setOnClickListener {
            navigateToTarget(R.id.settingFragment)
        }

        sharedViewModel.tvClock.observe(this, { TimeStr ->
            findViewById<TextView>(R.id.tvTime).text = TimeStr
        })

        sharedViewModel.versionUpdateTrigger.observe(this, {
            updateDialog()
        })


        sharedViewModel.setLanguageTrigger.observe(this, {
            val intent = intent
            finish()
            startActivity(intent)
        })

    }

    private fun navigateToTarget(id: Int){
        if (!findNavController(R.id.navHost).popBackStack(id, false)){
            findNavController(R.id.navHost).navigate(id)
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var newLocale = Locale.getDefault()
            when (prefs.languagePos) {
                0 -> newLocale = Locale.US
                1 -> newLocale = Locale.TRADITIONAL_CHINESE
                2 -> newLocale = Locale.SIMPLIFIED_CHINESE
            }
            newBase?.let {
                val context: Context = MyContextWrapper.wrap(it, newLocale)
                super.attachBaseContext(context)
            }
        } else {
            super.attachBaseContext(newBase)
        }
    }

    private fun updateLanguage(context: Context): Context {
        val newLocale = when (prefs.languagePos) {
            0 -> Locale.ENGLISH
            1 -> Locale.TRADITIONAL_CHINESE
            2 -> Locale.SIMPLIFIED_CHINESE
            else -> {
                when (Locale.getDefault().country) {
                    "TW" -> {
                        prefs.languagePos = 1
                        Locale.TRADITIONAL_CHINESE
                    }
                    "CN" -> {
                        prefs.languagePos = 2
                        Locale.SIMPLIFIED_CHINESE
                    }
                    else -> {
                        prefs.languagePos = 0
                        Locale.ENGLISH
                    }
                }
            }
        }
        Locale.setDefault(newLocale)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResourcesLocale(context, newLocale)
        } else updateResourcesLocaleLegacy(context, newLocale)
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResourcesLocale(context: Context, locale: Locale): Context {
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    @SuppressWarnings("deprecation")
    private fun updateResourcesLocaleLegacy(context: Context, locale: Locale): Context {
        val resources = context.resources
        val configuration = resources.configuration
        configuration.locale = locale
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val v = currentFocus
        if (v != null && (ev.action == MotionEvent.ACTION_UP || ev.action == MotionEvent.ACTION_MOVE) &&
            v is EditText
        ) {
            val sourceCoordinates = IntArray(2)
            v.getLocationOnScreen(sourceCoordinates)
            val x = ev.rawX + v.getLeft() - sourceCoordinates[0]
            val y = ev.rawY + v.getTop() - sourceCoordinates[1]
            if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom()) {
                hideKeyboard(this)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun hideKeyboard(activity: Activity?) {
        if (activity != null && activity.window != null) {
            activity.window.decorView
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(activity.window.decorView.windowToken, 0)
            findViewById<View>(android.R.id.content).clearFocus()
        }
    }

    private fun updateDialog() {
        val item: View = LayoutInflater.from(this@MainActivity).inflate(
            R.layout.dialog_setting_code,
            null
        )
        val etCode = item.findViewById<EditText>(R.id.etCode)
        val btnOk = item.findViewById<Button>(R.id.btn_ok)
        val btnCancel = item.findViewById<Button>(R.id.btn_cancel)
        etCode.isFocusable = true
        etCode.isFocusableInTouchMode = true
        etCode.requestFocus()
        //        etCode.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        val dialog = SweetAlertDialog(this)
        Objects.requireNonNull(dialog.window)?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.setTitleText(getString(R.string.enter_version_number)).setCustomView(item).hideConfirmButton().show()
        btnOk.setOnClickListener {
            if (etCode.text.toString() != "") {
                downloadApk(etCode.text.toString())
                dialog.dismissWithAnimation()
            } else {
                Toast.makeText(this@MainActivity, getString(R.string.input_err), Toast.LENGTH_SHORT)
            }
        }
        btnCancel.setOnClickListener { v: View? -> dialog.dismissWithAnimation() }
    }

    private fun downloadApk(name: String) {

        // instantiate it within the onCreate method
        val mProgressDialog = ProgressDialog(this@MainActivity)
        mProgressDialog.setMessage(getString(R.string.title_file_download))
        mProgressDialog.isIndeterminate = true
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        mProgressDialog.setCancelable(false)


        // execute this when the downloader must be fired
        val downloadTask = DownloadTask(this@MainActivity, mProgressDialog) { o ->
            if (o != null) {
                showAlertDialog(this, getString(R.string.error_occurred), o.toString())
            } else {
                Toast.makeText(this, getString(R.string.download_completed), Toast.LENGTH_SHORT).show()
                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(path, "mOrderReady.apk")
                val apkUri = FileProvider.getUriForFile(
                    this@MainActivity,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file
                )
                val install = Intent(Intent.ACTION_INSTALL_PACKAGE)
                install.data = apkUri
                install.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                startActivity(install)
            }
        }
        mProgressDialog.setButton(Dialog.BUTTON_NEGATIVE, getString(R.string.cancel)) { dialog: DialogInterface?, which: Int -> downloadTask.cancel(
            true
        ) }
        downloadTask.execute("http://cms.citrus.tw/apk/mCitrusTablet_signed_v$name.apk")
    }


    private fun showAlertDialog(context: Context?, title: String?, message: String) {
        val item: View = LayoutInflater.from(context).inflate(R.layout.dialog_alert_download, null)
        val btnSubmit = item.findViewById<Button>(R.id.btn_submit)
        val tvContent = item.findViewById<TextView>(R.id.tv_contentLeft)
        if (message == "") tvContent.visibility = View.GONE
        tvContent.text = message
        val sweetAlertDialog = SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
        sweetAlertDialog.setCanceledOnTouchOutside(false)
        sweetAlertDialog.setOnKeyListener { _, keyCode, _ ->
            if (keyCode === KeyEvent.KEYCODE_BACK) {
                // do nothing
            }
            true
        }
        sweetAlertDialog.setTitleText(title)
                .setCustomView(item)
                .hideConfirmButton()
                .show()
        btnSubmit.setOnClickListener { sweetAlertDialog.dismissWithAnimation() }
    }


    /*
        In Android 10, if you press back to finish an activity which is a task root,
    that activity will leak, This leak is happening because IRequestFinishCallback$Stub
    is an IPC callback that ends up being held by the activity manager process.
        We can "fix" this leak by overriding Activity.onBackPressed() and calling
    Activity.finishAfterTransition() instead of super if the activity is task root and
    the fragment stack is empty.
     */
    override fun onBackPressed() {
        var dialog = CustomAlertDialog(
            this,
            "Do you want to close the app?",
            "",
            R.drawable.ic_baseline_error_24,
            onConfirmListener = {
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHost)!!
                if (isTaskRoot
                    && navHostFragment.childFragmentManager.backStackEntryCount == 0
                    && supportFragmentManager.backStackEntryCount == 0
                    && Build.VERSION.SDK_INT == Build.VERSION_CODES.Q
                ) {
                    finishAfterTransition()
                } else {
                    finish()
                }
            })
        dialog!!.show()
    }


}