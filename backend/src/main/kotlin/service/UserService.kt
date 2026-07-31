package dev.frammenti.fuckumeter.service

import dev.frammenti.fuckumeter.auth.TokenProvider
import dev.frammenti.fuckumeter.domain.Device
import dev.frammenti.fuckumeter.domain.User
import dev.frammenti.fuckumeter.dto.UsersResponse
import dev.frammenti.fuckumeter.repository.DeviceRepository
import dev.frammenti.fuckumeter.repository.UserRepository

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
            )
        val refreshToken = tokens.refreshToken()

        users.transaction<Unit> {
            users.insert(user)
            devices.insert(device, refreshToken)
        }

        val token = tokens.accessToken(user.id, device.id)

        return UsersResponse(
            user.id,
            device.id,
            token,
            refreshToken,
        )
    }
}
