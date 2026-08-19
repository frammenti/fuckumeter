package dev.frammenti.fuckumeter.service

import dev.frammenti.fuckumeter.auth.TokenProvider
import dev.frammenti.fuckumeter.domain.Device
import dev.frammenti.fuckumeter.dto.CredentialsResponse
import dev.frammenti.fuckumeter.dto.RefreshTokenResponse
import dev.frammenti.fuckumeter.exceptions.InvalidRefreshTokenException
import dev.frammenti.fuckumeter.exceptions.PermissionDeniedException
import dev.frammenti.fuckumeter.repository.DeviceRepository
import java.util.UUID

class DeviceService(
    private val devices: DeviceRepository,
    private val tokens: TokenProvider,
) {
    suspend fun create(
        userId: UUID,
        name: String,
    ): CredentialsResponse {
        val device =
            Device(
                userId = userId,
                name = name,
            )
        val refreshToken = tokens.refreshToken()

        devices.insert(device, refreshToken)

        return CredentialsResponse(
            userId,
            device.id,
            tokens.accessToken(userId, device.id),
            refreshToken,
        )
    }

    suspend fun refreshToken(
        deviceId: UUID,
        refreshToken: String,
    ): RefreshTokenResponse {

        val newRefreshToken = tokens.refreshToken()

        val userId =
            devices.updateRefreshToken(
                deviceId,
                refreshToken,
                newRefreshToken,
            ) ?: throw InvalidRefreshTokenException()

        return RefreshTokenResponse(
            token = tokens.accessToken(userId, deviceId),
            refreshToken = newRefreshToken,
        )
    }

    suspend fun delete(
        deviceId: UUID,
        userId: UUID,
    ) {
        if (!devices.belongsToUser(deviceId, userId))
            throw PermissionDeniedException(
                "You are not allowed to delete this device"
            )

        devices.delete(deviceId)
    }

    suspend fun logout(deviceId: UUID) {
        devices.delete(deviceId)
    }
}
