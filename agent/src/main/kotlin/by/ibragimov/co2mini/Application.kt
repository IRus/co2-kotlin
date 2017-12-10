package by.ibragimov.co2mini

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.usb4java.DeviceHandle
import org.usb4java.LibUsb
import org.usb4java.LibUsbException
import org.usb4java.TransferCallback


object Application {
    @JvmStatic fun main(args: Array<String>) {
//        Bootique.app(*args).autoLoadModules().run()

        try {
            val (device, context) = UsbInteraction.getDevice()

            if (device != null) {
                val handle = DeviceHandle()
                LibUsb.open(device, handle)

                // Start event handling thread
                val thread = AsyncBulkTransfer.EventHandlingThread()
                thread.start()

                // Claim the ADB interface
//                var result = LibUsb.claimInterface(handle, 1)
//                if (result != LibUsb.SUCCESS) {
//                    throw LibUsbException("Unable to claim interface", result)
//                }

                // This callback is called after the ADB answer body has been
                // received. The asynchronous transfer chain ends here.
                val bodyReceived = TransferCallback { transfer ->
                    println(transfer.actualLength().toString() + " bytes received")
                    LibUsb.freeTransfer(transfer)
                    println("Asynchronous communication finished")
                    AsyncBulkTransfer.exit = true
                }

                AsyncBulkTransfer.read(handle, 5, bodyReceived)

                // Fake application loop
                while (!AsyncBulkTransfer.exit) {
                    Thread.yield()
                }

                // Release the ADB interface
                var result = LibUsb.releaseInterface(handle, AsyncBulkTransfer.INTERFACE.toInt())
                if (result != LibUsb.SUCCESS) {
                    throw LibUsbException("Unable to release interface", result)
                }

                // Close the device
                LibUsb.close(handle)

                // Stop event handling thread
                thread.abort()
                thread.join()

                // Deinitialize the libusb context
                LibUsb.exit(null)

                println("Program finished")


            }
        } catch (e: Exception) {
            UsbInteraction.close()
        }
    }
}


private val info = """
Bus 002 Device 001: ID 1d6b:0003 Linux Foundation 3.0 root hub
Bus 001 Device 002: ID 0cf3:e300 Atheros Communications, Inc.
Bus 001 Device 007: ID 0909:001c Audio-Technica Corp.
Bus 001 Device 003: ID 1bcf:2b95 Sunplus Innovation Technology Inc.
Bus 001 Device 008: ID 04d9:a052 Holtek Semiconductor, Inc.
Bus 001 Device 001: ID 1d6b:0002 Linux Foundation 2.0 root hub

https://github.com/maizy/ambient7
https://github.com/dmage/co2mon/blob/master/udevrules/99-co2mon.rules

http://www.co2meter.com/collections/data-loggers/products/co2mini-co2-indoor-air-quality-monitor
надо повесить датчик на СО2, соединить с гитом и смотреть зависимость коммитов от показаний СО2
"""

fun main(args: Array<String>) {
}

fun main1(args: Array<String>) = runBlocking<Unit> {
    val job = launch(CommonPool) {
        var nextPrintTime = 0L
        var i = 0
        while (i < 10) { // computation loop
            val currentTime = System.currentTimeMillis()
            if (currentTime >= nextPrintTime) {
                println("I'm sleeping ${i++} ...")
                nextPrintTime = currentTime + 500L
            }
        }
    }
    delay(1300L) // delay a bit
    println("main: I'm tired of waiting!")
    job.cancel() // cancels the job
    delay(1300L) // delay a bit to see if it was cancelled....
    println("main: Now I can quit.")
}
