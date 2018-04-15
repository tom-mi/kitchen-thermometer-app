package de.rfnbrgr.kitchenheatseeker

import org.junit.rules.ExternalResource
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException

class TestDeviceRule : ExternalResource() {

    val server = TestDevice(5000)
    private var serverThread: Thread? = null

    @Throws(Throwable::class)
    override fun before() {
        super.before()
        serverThread = Thread(server)
        serverThread!!.start()
    }

    override fun after() {
        super.after()
        server.stop()
        serverThread!!.join()
    }
}


class TestDevice(val port: Int) : Runnable {

    companion object {
        val SOCKET_TIMEOUT_MS = 500
    }

    private var running: Boolean = true
    private var connections: MutableList<Socket> = mutableListOf()

    fun stop() {
        running = false
    }

    override fun run() {
        val socket = ServerSocket(port)
        socket.soTimeout = SOCKET_TIMEOUT_MS
        while (running) {
            try {
                val connectionSocket = socket.accept()
                connections.add(connectionSocket)
            } catch(e: SocketTimeoutException) {
                // retry
            }
        }
    }

    fun send(frame: String) {
        connections.forEach {
            it.getOutputStream().write("$frame\n".toByteArray())
        }
    }

}