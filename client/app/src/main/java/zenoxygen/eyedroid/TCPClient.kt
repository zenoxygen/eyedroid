package zenoxygen.eyedroid

import android.os.AsyncTask
import java.io.IOError
import java.io.PrintWriter
import java.net.Socket
import org.json.JSONException
import org.json.JSONObject

class TCPClient(host: String, port: Int): AsyncTask<Void, Void, String>() {

    private var host: String = ""
    private var port: Int = 0
    private var data: JSONObject = JSONObject()

    init {
        this.host = host
        this.port = port
    }

    override fun doInBackground(vararg params: Void?): String? {

        var socket: Socket? = null

        try {
            socket = Socket(this.host, this.port)
            val output = PrintWriter(socket.getOutputStream(), true)
            output.println(data)
            output.close()
        } catch (error: Exception) {
            error.printStackTrace()
        } catch (error: IOError) {
            error.printStackTrace()
        } finally {
            socket?.close()
        }

        return null
    }

    fun sendData(jsonData: JSONObject) {
        try {
            this.data = jsonData
        } catch (error: JSONException) {
            error.printStackTrace()
        }
    }

}