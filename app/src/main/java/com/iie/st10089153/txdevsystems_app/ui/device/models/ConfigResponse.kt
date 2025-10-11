
package com.iie.st10089153.txdevsystems_app.ui.device.models

import com.google.gson.annotations.SerializedName

/**
 * Configuration response model matching the new API structure
 * Includes all fields from the API response
 */
data class ConfigResponse(
    @SerializedName("imei")
    val imei: String,

    @SerializedName("unit_id")
    val unit_id: String?,

    @SerializedName("temp_max")
    val temp_max: String?,

    @SerializedName("temp_min")
    val temp_min: String?,

    @SerializedName("door_alarm_hour")
    val door_alarm_hour: String?,

    @SerializedName("door_alarm_min")
    val door_alarm_min: String?,

    @SerializedName("switch_polarity")
    val switch_polarity: String?,

    @SerializedName("config_type")
    val config_type: String?,

    @SerializedName("data_resend_min")
    val data_resend_min: String?,

    @SerializedName("network")
    val network: String?,

    @SerializedName("remaining_data")
    val remaining_data: String?,

    @SerializedName("req_location")
    val req_location: String?,

    @SerializedName("rev_number")
    val rev_number: String?,

    @SerializedName("send_conf")
    val send_conf: String?,

    @SerializedName("signal_strength")
    val signal_strength: String?,

    @SerializedName("sim_iccid")
    val sim_iccid: String?,

    @SerializedName("telegram_no")
    val telegram_no: String?,

    @SerializedName("trigger_resend_min")
    val trigger_resend_min: String?
)