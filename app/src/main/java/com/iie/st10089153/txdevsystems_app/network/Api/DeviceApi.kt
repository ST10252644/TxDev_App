package com.iie.st10089153.txdevsystems_app.network.Api

import com.iie.st10089153.txdevsystems_app.ui.device.models.ConfigResponse
import com.iie.st10089153.txdevsystems_app.ui.device.models.TempThresholdRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

data class ConfigByImeiRequest(
    val imei: String
)

data class UpdateConfigRequest(
    val imei: String,
    val unit_id: String,
    val temp_max: String,
    val temp_min: String,
    val door_alarm_hour: String,
    val door_alarm_min: String,
    val switch_polarity: String
)

data class UpdateUnitNameRequest(
    val imei: String,
    val new_name: String
)

interface DeviceApi {

    @POST("/config_by_imei/")
    suspend fun getConfigByImei(@Body body: ConfigByImeiRequest): Response<ConfigResponse>

    @PUT("/update_unit_name/")
    suspend fun updateUnitName(@Body body: UpdateUnitNameRequest): Response<ConfigResponse>

    @PUT("/set_temp_thresholds/")
    suspend fun setTempThresholds(@Body body: TempThresholdRequest): Response<ConfigResponse>
}
