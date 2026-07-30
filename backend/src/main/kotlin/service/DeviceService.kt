package dev.frammenti.fuckumeter.service

import dev.frammenti.fuckumeter.dto.RefreshTokenResponse
import dev.frammenti.fuckumeter.exceptions.InvalidRefreshTokenException
import dev.frammenti.fuckumeter.repository.DeviceRepository
import dev.frammenti.fuckumeter.auth.TokenProvider
import java.util.UUID

class DeviceService(
    private val devices: DeviceRepository,
    private val tokens: TokenProvider,
) {
    fun refreshToken(deviceId: UUID, refreshToken: String): RefreshTokenResponse {

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

    fun logout(userId: UUID, deviceId: UUID) {
        devices.delete(deviceId)
    }
}
