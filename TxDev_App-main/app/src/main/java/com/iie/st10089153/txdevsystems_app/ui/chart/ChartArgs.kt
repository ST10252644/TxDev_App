package com.iie.st10089153.txdevsystems_app.ui.chart

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

/** Try every reasonable place an IMEI might be stored. */
fun Fragment.resolveImeiFlexible(): String? {
    // 1) Direct args (several common keys)
    val keys = listOf("IMEI", "imei", "device_imei", "selectedImei")
    for (k in keys) {
        val v = arguments?.getString(k)
        if (!v.isNullOrBlank()) return v.trim()
    }
    // 2) SavedStateHandle from current/previous entries
    val currSaved = findNavController().currentBackStackEntry
        ?.savedStateHandle?.get<String>("IMEI")
    if (!currSaved.isNullOrBlank()) return currSaved.trim()

    val prevSaved = findNavController().previousBackStackEntry
        ?.savedStateHandle?.get<String>("IMEI")
    if (!prevSaved.isNullOrBlank()) return prevSaved.trim()

    // 3) Previous back stack arguments (if any)
    val prevArg = findNavController().previousBackStackEntry
        ?.arguments?.getString("IMEI")
    if (!prevArg.isNullOrBlank()) return prevArg.trim()

    return null
}
