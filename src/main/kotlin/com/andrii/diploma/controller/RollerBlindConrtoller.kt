package com.andrii.diploma.controller

import com.andrii.diploma.dto.DeviceStateDto
import com.andrii.diploma.dto.RollerBlindDto
import com.andrii.diploma.model.RollerBlindEntity
import com.andrii.diploma.service.RollerBlindService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/rollerblinds")
class RollerBlindController(
    private val rollerBlindService: RollerBlindService
) {
    @PostMapping("/scheduleOperation")
    fun scheduleOperation(
        @RequestParam deviceId: String,
        @RequestParam command: String,
        @RequestParam time: String,
        @RequestParam percentageToRotate: Int
    ): ResponseEntity<String> {
        return try {
            rollerBlindService.scheduleOperation(deviceId, command, time, percentageToRotate)
            ResponseEntity("Operation scheduled successfully for $time", HttpStatus.ACCEPTED)
        } catch (e: Exception) {
            ResponseEntity(e.message ?: "Error scheduling operation", HttpStatus.BAD_REQUEST)
        }
    }

    @PostMapping("/add")
    fun addRollerBlind(
        @RequestParam name: String,
        @RequestParam deviceId: String,
        @RequestParam length: Float
    ): ResponseEntity<RollerBlindEntity> {
        val rollerBlind = rollerBlindService.addRollerBlind(name, deviceId, length)
        return ResponseEntity(rollerBlind, HttpStatus.CREATED)
    }

    @GetMapping("/getMotorState/{deviceId}")
    fun getMotorState(@PathVariable deviceId: String): ResponseEntity<RollerBlindDto> {
        val rollerBlind = rollerBlindService.getRollerBlindByDeviceId(deviceId)
        return if (rollerBlind != null) {
            when (rollerBlind.command) {
                "close" -> ResponseEntity(
                    RollerBlindDto(
                        command = "close",
                        lengthToRotate = rollerBlind.lengthToRotate
                    ), HttpStatus.OK
                )

                "open" -> ResponseEntity(
                    RollerBlindDto(
                        command = "open",
                        lengthToRotate = rollerBlind.lengthToRotate
                    ), HttpStatus.OK
                )

                else -> ResponseEntity(
                    RollerBlindDto(
                        command = "done",
                        lengthToRotate = rollerBlind.lengthToRotate
                    ), HttpStatus.OK
                )
            }
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    @PostMapping("/device/{deviceId}/{percentageToRotate}/open")
    fun openRollerBlind(
        @PathVariable deviceId: String,
        @PathVariable percentageToRotate: Int
    ): ResponseEntity<String> {
        rollerBlindService.open(deviceId, percentageToRotate)
        return ResponseEntity.ok("Roller blind successfully opened")
    }

    @PostMapping("/device/{deviceId}/{percentageToRotate}/close")
    fun closeRollerBlind(
        @PathVariable deviceId: String,
        @PathVariable percentageToRotate: Int
    ): ResponseEntity<String> {
        return try {
            rollerBlindService.close(deviceId, percentageToRotate)
            ResponseEntity.ok("Roller blind successfully closed")
        } catch (e: IllegalArgumentException) {
            ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
        }
    }

    @PostMapping("/updateState/{deviceId}/{state}")
    fun updateCommandState(
        @PathVariable deviceId: String,
        @PathVariable state: String
    ): ResponseEntity<RollerBlindEntity> {
        return ResponseEntity.ok(rollerBlindService.updateState(state, deviceId))
    }

    @GetMapping("/getDeviceState/{deviceId}")
    fun getDeviceState(@PathVariable deviceId: String): ResponseEntity<DeviceStateDto> {
        return ResponseEntity.ok(rollerBlindService.getDeviceState(deviceId))
    }
}
