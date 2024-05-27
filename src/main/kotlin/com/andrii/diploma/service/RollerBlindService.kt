package com.andrii.diploma.service

import com.andrii.diploma.dto.DeviceStateDto
import com.andrii.diploma.model.RollerBlindEntity
import com.andrii.diploma.repository.RollerBlindRepository
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Service
class RollerBlindService(
    private val rollerBlindRepository: RollerBlindRepository,
    private val scheduledExecutorService: ScheduledExecutorService
) {

    fun addRollerBlind(name: String, deviceId: String, length: Float): RollerBlindEntity {
        val rollerBlind = RollerBlindEntity(
            name = name,
            deviceId = deviceId,
            length = length,
            lengthToRotate = 0f,
            openedLength = 0f,
            command = "done"
        )
        return rollerBlindRepository.save(rollerBlind)
    }

    fun getRollerBlindByDeviceId(deviceId: String): RollerBlindEntity? {
        return rollerBlindRepository.findByDeviceId(deviceId)
    }

    fun save(rollerBlind: RollerBlindEntity): RollerBlindEntity {
        return rollerBlindRepository.save(rollerBlind)
    }

    fun updateState(state: String, deviceId: String): RollerBlindEntity {
        val rollerBlind = getRollerBlindByDeviceId(deviceId)
        rollerBlind?.command = state
        save(rollerBlind!!)
        return rollerBlind
    }

    fun open(deviceId: String, percentageToRotate: Int) {
        val rollerBlind = getRollerBlindByDeviceId(deviceId)
        val prevOpenedLength = rollerBlind?.openedLength
        if (rollerBlind?.command == "done") {
            rollerBlind.command = "open"
            rollerBlind.openedLength = rollerBlind.length * (percentageToRotate / 100.0).toFloat()
            rollerBlind.lengthToRotate = rollerBlind.openedLength - prevOpenedLength!!
            save(rollerBlind)
        }
    }

    fun close(deviceId: String, percentageToRotate: Int) {
        val rollerBlind = getRollerBlindByDeviceId(deviceId)
        val prevOpenedLength = rollerBlind?.openedLength
        val currentPercentage = (rollerBlind!!.openedLength*100)/rollerBlind.length
        var neededPercentage: Int
        if (rollerBlind.command == "done") {
            neededPercentage = if (currentPercentage.toInt() == percentageToRotate) {
                100
            } else {
                100 - percentageToRotate
            }
            rollerBlind.command = "close"
            rollerBlind.openedLength = (((100 - neededPercentage) / 100.0) * rollerBlind.length).toFloat()
            rollerBlind.lengthToRotate = prevOpenedLength!! - rollerBlind.openedLength
            if (rollerBlind.lengthToRotate >= 0) {
                save(rollerBlind)
            } else {
                throw IllegalArgumentException("Can't close roller blinds on bigger length than it open")
            }
        }
    }

    fun getCurrentTimeInUkraine(): ZonedDateTime {
        return ZonedDateTime.now(ZoneId.of("Europe/Kiev"))
    }

    fun scheduleOperation(deviceId: String, command: String, time: String, percentageToRotate: Int) {
        val task = Runnable {
            if (command == "open") {
                open(deviceId, percentageToRotate)
            } else if (command == "close") {
                close(deviceId, percentageToRotate)
            }
        }

        val triggerTime = LocalTime.parse(time)
        val ukraineNow = getCurrentTimeInUkraine()
        val delay = Duration.between(ukraineNow.toLocalTime(), triggerTime).seconds

        if (delay > 0) {
            scheduledExecutorService.schedule(task, delay, TimeUnit.SECONDS)
        } else {
            throw IllegalArgumentException("Scheduled time must be in the future.")
        }
    }

    fun getDeviceState(deviceId: String): DeviceStateDto {
        val rollerBlind = getRollerBlindByDeviceId(deviceId)
        return when (rollerBlind?.openedLength) {
            0.0f -> {
                DeviceStateDto("Closed", (rollerBlind.openedLength*100)/rollerBlind.length)
            }
            rollerBlind?.length -> {
                DeviceStateDto("Opened", (rollerBlind!!.openedLength*100)/rollerBlind.length)
            }
            else -> {
                DeviceStateDto("Partly opened", (rollerBlind!!.openedLength*100)/rollerBlind.length)
            }
        }
    }
}

