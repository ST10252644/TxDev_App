package com.iie.st10089153.txdevsystems_app.ui.common

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

/**
 * Reads the IMEI passed through navigation.
 * Order:
 *  1) Fragment arguments: "IMEI" / "imei" / "device_imei" / "selectedImei"
 *  2) Nav savedStateHandle on current/previous back stack entry
 *  3) Activity intent extra "IMEI"
 * Throws if not found (so wiring bugs are obvious during dev).
 */
fun Fragment.requireImei(): String {
    val arg =
        arguments?.getString("IMEI")
            ?: arguments?.getString("imei")
            ?: arguments?.getString("device_imei")
            ?: arguments?.getString("selectedImei")
    if (!arg.isNullOrBlank()) return arg.trim()

    val nav = findNavController()
    val s1 = nav.currentBackStackEntry?.savedStateHandle?.get<String>("IMEI")
    val s2 = nav.previousBackStackEntry?.savedStateHandle?.get<String>("IMEI")
    if (!s1.isNullOrBlank()) return s1.trim()
    if (!s2.isNullOrBlank()) return s2.trim()

    val fromIntent = activity?.intent?.getStringExtra("IMEI")
    if (!fromIntent.isNullOrBlank()) return fromIntent.trim()

    error("IMEI was not provided to ${this::class.simpleName}")
}
