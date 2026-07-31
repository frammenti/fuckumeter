package dev.frammenti.fuckumeter

import dev.frammenti.fuckumeter.auth.JwtConfig
import dev.frammenti.fuckumeter.auth.TokenProvider
import dev.frammenti.fuckumeter.db.Database
import dev.frammenti.fuckumeter.db.DatabaseConfig
import dev.frammenti.fuckumeter.repository.DeviceRepository
import dev.frammenti.fuckumeter.repository.GroupRepository
import dev.frammenti.fuckumeter.repository.InviteRepository
import dev.frammenti.fuckumeter.repository.UserRepository
import dev.frammenti.fuckumeter.routing.configureRouting
import dev.frammenti.fuckumeter.security.AesGcmCipher
import dev.frammenti.fuckumeter.security.HmacHasher
import dev.frammenti.fuckumeter.security.SecurityConfig
import dev.frammenti.fuckumeter.service.DeviceService
import dev.frammenti.fuckumeter.service.GroupService
import dev.frammenti.fuckumeter.service.InviteService
import dev.frammenti.fuckumeter.service.UserService
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val databaseConfig = DatabaseConfig(environment.config)
    val jwtConfig = JwtConfig(environment.config)
    val securityConfig = SecurityConfig(environment.config)

    val database = Database(databaseConfig)

    val inviteHasher = HmacHasher(securityConfig.inviteSecret)
    val inviteCipher = AesGcmCipher(securityConfig.inviteKey)
    val refreshHasher = HmacHasher(securityConfig.refreshTokenSecret)
    val tokenProvider = TokenProvider(jwtConfig)

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
