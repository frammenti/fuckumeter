package repository

import dev.frammenti.fuckumeter.domain.Device
import dev.frammenti.fuckumeter.repository.DeviceRepository
import fixtures.TestCrypto.hasher
import fixtures.TestFixtures.insertUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class DeviceRepositoryTest : RepositoryTest() {
    private val repository = DeviceRepository(database, hasher)

    @Test
    fun `insert persists device`() {

        val device =
            Device(
                name = "Pixel",
                userId = insertUser()
            )

        repository.insert(device, "token")

        val stored = repository.find(device.id)

        assertNotNull(stored)
        assertEquals(device, stored)
    }

}
