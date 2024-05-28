package com.andrii.diploma.repository

import com.andrii.diploma.model.RollerBlindEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface RollerBlindRepository : MongoRepository<RollerBlindEntity, String> {
    fun findByDeviceId(deviceId: String): RollerBlindEntity?
    fun deleteByDeviceId(deviceId: String)
}
