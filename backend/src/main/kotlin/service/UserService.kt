package dev.frammenti.fuckumeter.service

import dev.frammenti.fuckumeter.db.Database.transaction
import dev.frammenti.fuckumeter.domain.Device
import dev.frammenti.fuckumeter.domain.User
import dev.frammenti.fuckumeter.dto.UsersResponse
import dev.frammenti.fuckumeter.repository.DeviceRepository
import dev.frammenti.fuckumeter.repository.UserRepository
import dev.frammenti.fuckumeter.security.TokenProvider

class UserService(
    private val users: UserRepository,
    private val devices: DeviceRepository,
    private val tokens: TokenProvider,
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
                refreshToken = tokens.refreshToken(),
            )

        transaction<Unit> {
            users.insert(user)
            devices.insert(device)
        }

        return UsersResponse(
            user.id,
            device.id,
            tokens.accessToken(user.id, device.id),
            device.refreshToken,
        )
    }
}
