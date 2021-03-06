package com.citrus.mCitrusTablet.view

import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.RingtoneManager
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
import androidx.navigation.navOptions
import cn.pedant.SweetAlert.SweetAlertDialog
import com.citrus.mCitrusTablet.BuildConfig
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.di.prefs
import com.citrus.mCitrusTablet.model.vo.ReservationGuests
import com.citrus.mCitrusTablet.model.vo.Wait
import com.citrus.mCitrusTablet.util.*
import com.citrus.mCitrusTablet.view.dialog.CustomAlertDialog
import com.noober.background.BackgroundLibrary
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*
import kotlin.system.exitProcess


@AndroidEntryPoint
@Suppress("DEPRECATED_IDENTITY_EQUALS", "DEPRECATION")
class MainActivity : AppCompatActivity() {
    private var currentApiVersion: Int = 0
    private val sharedViewModel: SharedViewModel by viewModels()

    lateinit var alert:CustomAlertDialog
    private fun isAlertInit()=::alert.isInitialized

    private lateinit var storageWait:Wait
    private fun isWaitInit()=::storageWait.isInitialized

    private lateinit var storageRes:ReservationGuests
    private fun isResInit()=::storageRes.isInitialized


    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

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
        BackgroundLibrary.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navController: NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment?
        navController = navHostFragment!!.navController


        if (prefs.storeId == "" || prefs.severDomain == "") {
            navController.navigate(R.id.settingFragment)
        }

        reservation_block.setOnClickListener {
            resAlert.visibility = View.INVISIBLE
            report_block.setBackgroundResource(0)
            wait_block.setBackgroundResource(0)
            reservation_block.setBackgroundResource(R.drawable.bg_menu_select2)
            navigateToTarget(R.id.reservationFragment)
        }

        wait_block.setOnClickListener {
            waitAlert.visibility = View.INVISIBLE
            report_block.setBackgroundResource(0)
            reservation_block.setBackgroundResource(0)
            wait_block.setBackgroundResource(R.drawable.bg_menu_select2)
            navigateToTarget(R.id.waitFragment)
        }

        report_block.setOnClickListener {
            wait_block.setBackgroundResource(0)
            reservation_block.setBackgroundResource(0)
            report_block.setBackgroundResource(R.drawable.bg_menu_select2)
            navigateToTarget(R.id.reportFragment)
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

        sharedViewModel.newDataTrigger.observe(this, { map ->
            var msg = ""

            when (map.keys.first()) {
                Constants.KEY_WAIT_NUM -> {


                    var wait = map[Constants.KEY_WAIT_NUM] as Wait

                    if (isWaitInit()) {
                        if (wait.tkey == storageWait.tkey)
                            return@observe
                    }

                    storageWait = wait
                    msg = "候位名單已新增來自 " + wait.mName + " 的候位資料"
                    waitAlert.visibility = View.VISIBLE
                }

                Constants.KEY_RESERVATION_NUM -> {
                    var res = map[Constants.KEY_RESERVATION_NUM] as ReservationGuests

                    if (isResInit()) {
                        if (res.tkey == storageRes.tkey)
                            return@observe
                    }

                    storageRes = res

                    msg = "本日訂位名單已新增來自 " + res.mName + " 的訂位資料"
                    resAlert.visibility = View.VISIBLE
                }
            }


            if (isAlertInit()) {
                alert.dismiss()
            }

            alert = CustomAlertDialog(
                this,
                "新增通知",
                msg,
                0,
                onConfirmListener = {

                }
            )
            alert.show()

            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(applicationContext, notification)
            r.play()

        })

        sharedViewModel.setLanguageTrigger.observe(this, {
            val intent = intent
            finish()
            overridePendingTransition(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
            startActivity(intent)
        })

    }

    private fun navigateToTarget(id: Int) {
        if(findNavController(R.id.navHost).isFragmentRemovedFromBackStack(id)){
            findNavController(R.id.navHost).navigate(id, null, navOptions {
                anim {
                    enter = android.R.animator.fade_in
                    exit = android.R.animator.fade_out
                }
            })
        }else{
            findNavController(R.id.navHost).popBackStack(id, false)
        }
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
        Objects.requireNonNull(dialog.window)
            ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.setTitleText(getString(R.string.enter_version_number)).setCustomView(item)
            .hideConfirmButton().show()
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
                Toast.makeText(this, getString(R.string.download_completed), Toast.LENGTH_SHORT)
                    .show()
                val path =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(path, "catch.apk")
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
        mProgressDialog.setButton(
            Dialog.BUTTON_NEGATIVE,
            getString(R.string.cancel)
        ) { dialog: DialogInterface?, which: Int ->
            downloadTask.cancel(
                true
            )
        }
        downloadTask.execute("http://hq.citrus.tw/apk/catch_signed_v$name.apk")
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
            resources.getString(R.string.close_msg),
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


    private fun NavController.isFragmentRemovedFromBackStack(destinationId: Int) =
        try {
            getBackStackEntry(destinationId)
            false
        } catch (e: Exception) {
            true
        }


    /**重啟application*/
    private fun triggerRestart(context: Activity) {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        if (context is Activity) {
            (context as Activity).finish()
        }
        Runtime.getRuntime().exit(0)
    }

}