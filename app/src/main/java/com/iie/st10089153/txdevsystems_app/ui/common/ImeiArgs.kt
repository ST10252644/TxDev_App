package com.iie.st10089153.txdevsystems_app.ui.common

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

/**
 * Reads the IMEI that was passed through navigation.
 * Order:
 * 1) Fragment arguments: "IMEI"/"imei"/"device_imei"/"selectedImei"
 * 2) Navigation savedStateHandle from current/previous back stack entry
 * 3) Activity intent extra "IMEI"
 * If none exist, it throws so you see the wiring bug quickly.
 */
fun Fragment.requireImei(): String {
    val fromArgs =
        arguments?.getString("IMEI")
            ?: arguments?.getString("imei")
            ?: arguments?.getString("device_imei")
            ?: arguments?.getString("selectedImei")
    if (!fromArgs.isNullOrBlank()) return fromArgs.trim()

    val nav = findNavController()
    val fromSavedState =
        nav.currentBackStackEntry?.savedStateHandle?.get<String>("IMEI")
            ?: nav.previousBackStackEntry?.savedStateHandle?.get<String>("IMEI")
    if (!fromSavedState.isNullOrBlank()) return fromSavedState.trim()

    val fromIntent = activity?.intent?.getStringExtra("IMEI")
    if (!fromIntent.isNullOrBlank()) return fromIntent.trim()

    error("IMEI was not provided to ${this::class.simpleName}")
}
