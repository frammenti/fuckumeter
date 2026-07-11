package dev.frammenti.fuckumeter.service

import dev.frammenti.fuckumeter.db.Database.transaction
import dev.frammenti.fuckumeter.domain.Device
import dev.frammenti.fuckumeter.domain.User
import dev.frammenti.fuckumeter.dto.UsersResponse
import dev.frammenti.fuckumeter.repository.DeviceRepository
import dev.frammenti.fuckumeter.repository.UserRepository

class UserService(
    private val users: UserRepository,
    private val devices: DeviceRepository,
) {
    fun new(
        name: String,
        deviceName: String,
    ): UsersResponse {
        val user = User(name = name)
        val device =
            Device(
                userId = user.id,
                name = deviceName,
            )

        transaction<Unit> {
            users.insert(user)
            devices.insert(device)
        }

        return UsersResponse(user.id, device.id)
    }
}
