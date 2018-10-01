package zenoxygen.eyedroid

import android.Manifest
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.telephony.TelephonyManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private val READ_PHONE_STATE_REQUEST_CODE = 10001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputHost = findViewById<EditText>(R.id.inputHost)
        val inputPort = findViewById<EditText>(R.id.inputPort)
        val buttonStart = findViewById<Button>(R.id.buttonStart)

        buttonStart.setOnClickListener {

            if (inputHost.text.isEmpty() || inputPort.text.isEmpty())
                return@setOnClickListener

            val host: String = inputHost.text.toString()
            val port: Int = Integer.parseInt(inputPort.text.toString())

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED)
                makeRequestPermission("READ_PHONE_STATE")

                Thread(Runnable {
                    getDeviceImei(host, port)
                    getInstalledApps(host, port)
                    getDeviceInfo(host, port)
                }).start()
        }
    }

    private fun makeRequestPermission(permission: String) {
        if (permission.toLowerCase() == "read_phone_state")
            ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.READ_PHONE_STATE), READ_PHONE_STATE_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            READ_PHONE_STATE_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED))
                    Toast.makeText(this, "The application needs to access phone's information.",
                            Toast.LENGTH_LONG).show()
                return
            }
        }
    }

    private fun getDeviceImei(host: String, port: Int) {
        try {
            val telManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val deviceImei = JSONObject()
            deviceImei.put("device_imei", telManager.getImei())
            val client = TCPClient(host, port)
            client.sendData(deviceImei)
            client.execute()
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }

    private fun getDeviceInfo(host: String, port: Int) {
        val deviceInfo = JSONObject()

        deviceInfo.put("version_release", Build.VERSION.RELEASE)
        deviceInfo.put("version_incremental", Build.VERSION.INCREMENTAL)
        deviceInfo.put("version_sdk", Build.VERSION.SDK_INT.toString())
        deviceInfo.put("board", Build.BOARD)
        deviceInfo.put("bootloader", Build.BOOTLOADER)
        deviceInfo.put("brand", Build.BRAND)
        deviceInfo.put("display", Build.DISPLAY)
        deviceInfo.put("fingerprint", Build.FINGERPRINT)
        deviceInfo.put("hardware", Build.HARDWARE)
        deviceInfo.put("host", Build.HOST)
        deviceInfo.put("id", Build.ID)
        deviceInfo.put("manufacturer", Build.MANUFACTURER)
        deviceInfo.put("model", Build.MODEL)
        deviceInfo.put("product", Build.PRODUCT)
        deviceInfo.put("tags", Build.TAGS)
        deviceInfo.put("time", Build.TIME.toString())
        deviceInfo.put("type", Build.TYPE)
        deviceInfo.put("user", Build.USER)
        deviceInfo.put("device", Build.DEVICE)

        try {
            val telephonyManager = applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val simOperatorName = telephonyManager.simOperatorName
            deviceInfo.put("sim_number", telephonyManager.line1Number)
            deviceInfo.put("sim_operator", simOperatorName)
        } catch (error: SecurityException) {
            error.printStackTrace()
        }

        val client = TCPClient(host, port)
        client.sendData(deviceInfo)
        client.execute()
    }

    private fun getAppInfo(packageManager: PackageManager, packageInfo: ApplicationInfo): JSONObject {
        val appInfo = JSONObject()

        try {
            val appName = packageInfo.loadLabel(getPackageManager()).toString()
            val appPackage = packageInfo.processName
            val appUid = Integer.toString(packageInfo.uid)
            val appVersionName = packageManager.getPackageInfo(appPackage, 0).versionName.toString()
            val appVersionCode = (packageManager.getPackageInfo(appPackage, 0).versionCode).toString()
            appInfo.put("app_name", appName)
            appInfo.put("app_package", appPackage)
            appInfo.put("app_uid", appUid)
            appInfo.put("app_vname", appVersionName)
            appInfo.put("app_vcode", appVersionCode)
        } catch (error: Exception) {
            error.printStackTrace()
        }

        return appInfo
    }

    private fun getInstalledApps(host: String, port: Int) {
        val packageManager = getPackageManager()
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        for (packageInfo: ApplicationInfo in packages) {
            if (packageManager.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                val appInfo = getAppInfo(packageManager, packageInfo)
                val client = TCPClient(host, port)
                client.sendData(appInfo)
                client.execute()
            }
        }
    }

}