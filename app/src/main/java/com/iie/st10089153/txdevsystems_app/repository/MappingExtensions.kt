package com.iie.st10089153.txdevsystems_app.repository

import com.iie.st10089153.txdevsystems_app.database.entities.CachedConfig
import com.iie.st10089153.txdevsystems_app.database.entities.CachedDashboard
import com.iie.st10089153.txdevsystems_app.database.entities.CachedRangeData
import com.iie.st10089153.txdevsystems_app.database.entities.CachedUnit
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnit
import com.iie.st10089153.txdevsystems_app.network.Api.RangePoint  // ✅ CHANGED
import com.iie.st10089153.txdevsystems_app.ui.dashboard.models.DashboardItem
import com.iie.st10089153.txdevsystems_app.ui.device.models.ConfigResponse
import android.content.Context
import android.util.Log
import com.iie.st10089153.txdevsystems_app.database.*
import com.iie.st10089153.txdevsystems_app.network.Api.*
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// AvailableUnit mappings
fun AvailableUnit.toCachedUnit() = CachedUnit(
    imei = imei,
    name = name,
    status = status,
    last_seen = last_seen,
    timestamp = System.currentTimeMillis()
)

fun CachedUnit.toAvailableUnit() = AvailableUnit(
    imei = imei,
    name = name,
    status = status,
    last_seen = last_seen
)

// DashboardItem mappings
// DashboardItem mappings - Updated to match actual fields
fun DashboardItem.toCachedDashboard() = CachedDashboard(
    imei = imei,
    temp_now = temp_now.toString(),
    temp_min = temp_min.toString(),
    temp_max = temp_max.toString(),
    door_status = door_status,
    door_status_bool = door_status_bool,
    supply_status = supply_status,
    bat_volt = bat_volt.toString(),
    bat_status = bat_status,
    timestamp = System.currentTimeMillis()
)

fun CachedDashboard.toDashboardItem() = DashboardItem(
    id = 0,  // ✅ Default value
    imei = imei,
    temp_max = temp_max.toFloatOrNull() ?: 0f,  // ✅ Changed to Float
    temp_now = temp_now.toFloatOrNull() ?: 0f,  // ✅ Changed to Float
    temp_min = temp_min.toFloatOrNull() ?: 0f,  // ✅ Changed to Float
    supply_volt = 0f,  // ✅ Not stored in cache, default value
    bat_volt = bat_volt.toFloatOrNull() ?: 0f,  // ✅ Changed to Float
    supply_status = supply_status,
    bat_status = bat_status,
    door_status = door_status,
    door_status_bool = door_status_bool,
    signal_strength = "",  // ✅ Not stored in cache, default value
    timestamp = "",  // ✅ Not stored in cache, default value
    active = 1  // ✅ Default value
)

// ConfigResponse mappings
fun ConfigResponse.toCachedConfig() = CachedConfig(
    imei = imei,
    unit_id = unit_id,
    temp_max = temp_max,
    temp_min = temp_min,
    door_alarm_hour = door_alarm_hour,
    door_alarm_min = door_alarm_min,
    switch_polarity = switch_polarity,
    config_type = config_type,
    data_resend_min = data_resend_min,
    network = network,
    remaining_data = remaining_data,
    timestamp = System.currentTimeMillis()
)

fun CachedConfig.toConfigResponse() = ConfigResponse(
    imei = imei,
    unit_id = unit_id,
    temp_max = temp_max,
    temp_min = temp_min,
    door_alarm_hour = door_alarm_hour,
    door_alarm_min = door_alarm_min,
    switch_polarity = switch_polarity,
    config_type = config_type,
    data_resend_min = data_resend_min,
    network = network,
    remaining_data = remaining_data,
    req_location = null,
    rev_number = null,
    send_conf = null,
    signal_strength = null,
    sim_iccid = null,
    telegram_no = null,
    trigger_resend_min = null
)

// RangePoint mappings (FIXED - using RangePoint not RangeDataPoint)
fun RangePoint.toCachedRangeData(imei: String) = CachedRangeData(
    imei = imei,
    temp_now = temp_now?.toString() ?: "",
    temp_min = temp_min?.toString() ?: "",
    temp_max = temp_max?.toString() ?: "",
    door_status = door_status,
    door_status_bool = door_status_bool?.toString(),
    supply_status = supply_status,
    bat_status = bat_status,
    data_timestamp = timestamp,
    cached_at = System.currentTimeMillis()
)

fun CachedRangeData.toRangePoint() = RangePoint(
    id = null,
    imei = imei,
    temp_now = temp_now,
    temp_min = temp_min,
    temp_max = temp_max,
    supply_volt = null,
    bat_volt = null,
    supply_status = supply_status,
    bat_status = bat_status,
    door_status = door_status,
    door_status_bool = door_status_bool,
    remaining_data = null,
    signal_strength = null,
    timestamp = data_timestamp,
    active = null
)











