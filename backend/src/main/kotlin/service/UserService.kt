package dev.frammenti.fuckumeter.service

import dev.frammenti.fuckumeter.auth.TokenProvider
import dev.frammenti.fuckumeter.domain.Device
import dev.frammenti.fuckumeter.domain.User
import dev.frammenti.fuckumeter.dto.CredentialsResponse
import dev.frammenti.fuckumeter.dto.UserResponse
import dev.frammenti.fuckumeter.exceptions.ResourceNotFoundException
import dev.frammenti.fuckumeter.repository.DeviceRepository
import dev.frammenti.fuckumeter.repository.UserRepository
import java.util.UUID

class UserService(
    private val users: UserRepository,
    private val devices: DeviceRepository,
    private val tokens: TokenProvider,
) {
    fun get(id: UUID): UserResponse {
        val user = users.find(id) ?: throw ResourceNotFoundException("user")
        return UserResponse(user.id, user.name, user.status())
    }

    fun create(
        name: String,
        deviceName: String,
    ): CredentialsResponse {

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

        return CredentialsResponse(
            user.id,
            device.id,
            tokens.accessToken(user.id, device.id),
            refreshToken,
        )
    }
}
