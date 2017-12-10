package by.ibragimov.co2mini

/**
 * TODO.
 *
 * @author Ibragimov Ruslan
 * @since TODO
 */
data class DeviceConfiguration(val vendor: Short, val product: Short)
data class ApplicationConfiguration(val device: DeviceConfiguration)

val defaultConfiguration = ApplicationConfiguration(DeviceConfiguration(0x04d9.toShort(), 0xa052.toShort()))