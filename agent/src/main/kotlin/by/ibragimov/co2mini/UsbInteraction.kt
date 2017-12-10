package by.ibragimov.co2mini

import org.usb4java.Context
import org.usb4java.Device
import org.usb4java.DeviceDescriptor
import org.usb4java.DeviceList
import org.usb4java.LibUsb
import org.usb4java.LibUsbException

/**
 * Class encapsulates interaction with CO2 Device.
 *
 * @author Ibragimov Ruslan
 * @since 0.1
 */
object UsbInteraction {

    private val context by lazy {
        // Create the libusb context
        Context()
    }

    fun getDevice(): Pair<Device?, Context> {
        // Initialize the libusb context
        var result = LibUsb.init(context)
        if (result < 0) {
            throw LibUsbException("Unable to initialize libusb", result)
        }

        // Read the USB device list
        val list = DeviceList()
        result = LibUsb.getDeviceList(context, list)
        if (result < 0) {
            throw LibUsbException("Unable to get device list", result)
        }

        try {
            return getDeviceFromList(list) to context
        } finally {
            // Ensure the allocated device list is freed
//            LibUsb.freeDeviceList(list, true)
        }
    }

    fun close() {
        // Deinitialize the libusb context
        LibUsb.exit(context)
    }

    private fun getDeviceFromList(list: DeviceList, config: ApplicationConfiguration = defaultConfiguration): Device? {
        return list.find { device ->
            val descriptor = DeviceDescriptor()
            val result = LibUsb.getDeviceDescriptor(device, descriptor)
            if (result < 0) {
                println("Unable to read device descriptor" + result)
                false
            } else {
                config.device.product == descriptor.idProduct() &&
                    config.device.vendor == descriptor.idVendor()
            }
        }
    }

}