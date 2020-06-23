package io.heapy.co2mini.agent

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import javax.usb.UsbDevice
import javax.usb.UsbHostManager
import javax.usb.UsbHub

fun main() {
    val config = ConfigFactory.load().extract<ApplicationConfiguration>()
    println("Running agent with config: $config")
    val usbServices = UsbHostManager.getUsbServices()
    val device = findDevice(usbServices.rootUsbHub, config.device)

    device?.let {
        println(it)
    }
}

fun findDevice(hub: UsbHub, configuration: DeviceConfiguration): UsbDevice? {
    return hub.attachedUsbDevices
        .filterIsInstance<UsbDevice>()
        .mapNotNull { device ->
            if (device.isUsbHub) {
                findDevice(device as UsbHub, configuration)
            } else {
                device
            }
        }
        .find { device ->
            val desc = device.usbDeviceDescriptor
            desc.idVendor() == configuration.vendorId && desc.idProduct() == configuration.productId
        }
}
