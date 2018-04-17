package de.rfnbrgr.kitchenheatseeker

import org.junit.rules.ExternalResource

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


