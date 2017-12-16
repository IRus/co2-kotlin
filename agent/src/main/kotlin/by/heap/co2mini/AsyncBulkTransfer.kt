package by.heap.co2mini

import org.usb4java.BufferUtils
import org.usb4java.DeviceHandle
import org.usb4java.LibUsb
import org.usb4java.LibUsbException
import org.usb4java.TransferCallback
import java.nio.ByteOrder

/**
 * TODO.

 * @author Ibragimov Ruslan
 * *
 * @since TODO
 */
object AsyncBulkTransfer {
    /**
     * Bytes for a CONNECT ADB message header.
     */
    internal val CONNECT_HEADER = byteArrayOf(0x43, 0x4E, 0x58, 0x4E, 0x00, 0x00, 0x00, 0x01, 0x00, 0x10, 0x00, 0x00, 0x17, 0x00, 0x00, 0x00, 0x42, 0x06, 0x00, 0x00, 0xBC.toByte(), 0xB1.toByte(), 0xA7.toByte(), 0xB1.toByte())
    /**
     * Bytes for a CONNECT ADB message body.
     */
    internal val CONNECT_BODY = byteArrayOf(0x68, 0x6F, 0x73, 0x74, 0x3A, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x3A, 0x41, 0x44, 0x42, 0x20, 0x44, 0x65, 0x6D, 0x6F, 0x00)


    val INTERFACE: Byte = 1
    /**
     * The ADB input endpoint of the Samsung Galaxy Nexus.
     */
    private val IN_ENDPOINT = 0x0.toByte()
    /**
     * The ADB output endpoint of the Samsung Galaxy Nexus.
     */
    private val OUT_ENDPOINT: Byte = 0x03
    /**
     * The communication timeout in milliseconds.
     */
    private val TIMEOUT = 5000
    /**
     * Flag set during the asynchronous transfers to indicate the program is
     * finished.
     */
    @Volatile internal var exit = false

    /**
     * Asynchronously reads some data from the device.

     * @param handle   The device handle.
     * *
     * @param size     The number of bytes to read from the device.
     * *
     * @param callback The callback to execute when data has been received.
     */
    fun read(handle: DeviceHandle, size: Int,
             callback: TransferCallback) {
        val buffer = BufferUtils.allocateByteBuffer(size).order(
            ByteOrder.LITTLE_ENDIAN)
        val transfer = LibUsb.allocTransfer()
        LibUsb.fillBulkTransfer(transfer, handle, IN_ENDPOINT, buffer,
            callback, null, TIMEOUT.toLong())
        println("Reading $size bytes from device")
        val result = LibUsb.submitTransfer(transfer)
        if (result != LibUsb.SUCCESS) {
            throw LibUsbException("Unable to submit transfer", result)
        }
    }

    /**
     * Main method.

     * @param args Command-line arguments (Ignored)
     * *
     * @throws Exception When something goes wrong.
     */
    @Throws(Exception::class)
    @JvmStatic fun main(args: Array<String>) {



    }

    /**
     * This is the event handling thread. libusb doesn't start threads by its
     * own so it is our own responsibility to give libusb time to handle the
     * events in our own thread.
     */
    internal class EventHandlingThread : Thread() {
        /**
         * If thread should abort.
         */
        @Volatile private var abort: Boolean = false

        /**
         * Aborts the event handling thread.
         */
        fun abort() {
            this.abort = true
        }

        override fun run() {
            while (!this.abort) {
                // Let libusb handle pending events. This blocks until events
                // have been handled, a hotplug callback has been deregistered
                // or the specified time of 0.5 seconds (Specified in
                // Microseconds) has passed.
                val result = LibUsb.handleEventsTimeout(null, 500000)
                if (result != LibUsb.SUCCESS)
                    throw LibUsbException("Unable to handle events", result)
            }
        }
    }
}
