package com.example.demo.receiver

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.demo.models.FileTransfer
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.ObjectInputStream
import java.net.InetSocketAddress
import java.net.ServerSocket

class FileReceiverViewModel(context: Application) :
    AndroidViewModel(context) {

    private val _viewState = MutableSharedFlow<ViewState>()

    val viewState: SharedFlow<ViewState> = _viewState

    private val _log = MutableSharedFlow<String>()

    val log: SharedFlow<String> = _log

    private var job: Job? = null

    fun startListener() {
        if (job != null) {
            return
        }
        job = viewModelScope.launch(context = Dispatchers.IO) {
            _viewState.emit(value = ViewState.Idle)

            var serverSocket: ServerSocket? = null
            var clientInputStream: InputStream? = null
            var objectInputStream: ObjectInputStream? = null
            var fileOutputStream: FileOutputStream? = null
            try {
                _viewState.emit(value = ViewState.Connecting)
                log(log = "turn on Socket")

                serverSocket = ServerSocket()
                serverSocket.bind(InetSocketAddress(Constants.PORT))
                serverSocket.reuseAddress = true
                serverSocket.soTimeout = 30000

                log(log = "socket accept，Disconnect if not successful within thirty seconds")

                val client = serverSocket.accept()

                _viewState.emit(value = ViewState.Receiving)

                clientInputStream = client.getInputStream()
                objectInputStream = ObjectInputStream(clientInputStream)
                val fileTransfer = objectInputStream.readObject() as FileTransfer
                val file = File(getCacheDir(context = getApplication()), fileTransfer.fileName)

                log(log = "Connection successful, files to be received: $fileTransfer")
                log(log = "The file will be saved to the following location: $file")
                log(log = "Start file transfer")

                fileOutputStream = FileOutputStream(file)
                val buffer = ByteArray(1024 * 100)
                while (true) {
                    val length = clientInputStream.read(buffer)
                    if (length > 0) {
                        fileOutputStream.write(buffer, 0, length)
                    } else {
                        break
                    }
                    log(log = "Transferring files，length : $length")
                }
                _viewState.emit(value = ViewState.Success(file = file))
                log(log = "File received successfully")
            } catch (e: Throwable) {
                log(log = "exception: " + e.message)
                _viewState.emit(value = ViewState.Failed(throwable = e))
            } finally {
                serverSocket?.close()
                clientInputStream?.close()
                objectInputStream?.close()
                fileOutputStream?.close()
            }
        }
        job?.invokeOnCompletion {
            job = null
        }
    }

    private fun getCacheDir(context: Context): File {
        val cacheDir = File(context.cacheDir, "FileTransfer")
        cacheDir.mkdirs()
        return cacheDir
    }

    private suspend fun log(log: String) {
        _log.emit(value = log)
    }

}