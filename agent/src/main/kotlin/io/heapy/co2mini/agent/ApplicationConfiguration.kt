package io.heapy.co2mini.agent

data class DeviceConfiguration(
    private val vendor: Int,
    private val product: Int
) {
    val vendorId = vendor.toShort()
    val productId = product.toShort()
}

data class ApplicationConfiguration(
    val device: DeviceConfiguration
)
