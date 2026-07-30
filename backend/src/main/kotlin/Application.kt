package dev.frammenti.fuckumeter

import dev.frammenti.fuckumeter.auth.TokenProvider
import dev.frammenti.fuckumeter.db.Database
import dev.frammenti.fuckumeter.repository.DeviceRepository
import dev.frammenti.fuckumeter.repository.GroupRepository
import dev.frammenti.fuckumeter.repository.InviteRepository
import dev.frammenti.fuckumeter.repository.UserRepository
import dev.frammenti.fuckumeter.routing.configureRouting
import dev.frammenti.fuckumeter.security.AesGcmCipher
import dev.frammenti.fuckumeter.security.HmacHasher
import dev.frammenti.fuckumeter.service.DeviceService
import dev.frammenti.fuckumeter.service.GroupService
import dev.frammenti.fuckumeter.service.InviteService
import dev.frammenti.fuckumeter.service.UserService
import io.ktor.server.application.Application

fun Application.module() {
    val config = AppConfig(environment.config)

    val database = Database(config.database)

    val inviteHasher = HmacHasher(config.security.inviteSecret)
    val inviteCipher = AesGcmCipher(config.security.inviteKey)
    val refreshHasher = HmacHasher(config.security.refreshTokenSecret)
    val tokenProvider = TokenProvider(config.jwt)

    val deviceRepository = DeviceRepository(database, refreshHasher)
    val groupRepository = GroupRepository(database)
    val inviteRepository =
        InviteRepository(database, inviteHasher, inviteCipher)
    val userRepository = UserRepository(database)

    val deviceService = DeviceService(deviceRepository, tokenProvider)
    val groupService = GroupService(groupRepository)
    val inviteService = InviteService(inviteRepository)
    val userService =
        UserService(userRepository, deviceRepository, tokenProvider)

    configureRouting(deviceService, groupService, inviteService, userService)
}
