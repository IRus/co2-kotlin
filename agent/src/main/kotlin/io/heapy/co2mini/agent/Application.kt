package io.heapy.co2mini.agent

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import javax.usb.UsbDevice
import javax.usb.UsbEndpoint
import javax.usb.UsbHostManager
import javax.usb.UsbHub

fun main() {
    val config = ConfigFactory.load().extract<ApplicationConfiguration>()
    println("Running agent with config: $config")
    val usbServices = UsbHostManager.getUsbServices()
    val device = findDevice(usbServices.rootUsbHub, config.device)

    device?.let {
        val configuration = device.activeUsbConfiguration
        val iface = configuration.getUsbInterface(0)
        iface.claim()
        try {
            val endpoint: UsbEndpoint = iface.getUsbEndpoint(0x83.toByte())
            val pipe = endpoint.usbPipe
            pipe.open()
            try {
                val data = ByteArray(8)
                val received = pipe.syncSubmit(data)
                println("$received bytes received")
            } finally {
                pipe.close()
            }
        } finally {
            iface.release()
        }
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
