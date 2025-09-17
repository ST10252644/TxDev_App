package com.iie.st10089153.txdevsystems_app.ui.chart

import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup

/**
 * Keeps a toolbar's title in sync with the checked pill's text (e.g., Wayne, Hoender2, Vriesks).
 * Works for single- and multi-select toggle groups (uses the first checked in multi).
 */
fun Fragment.bindToolbarTitleToUnitToggle(
    @IdRes toolbarId: Int,
    @IdRes toggleGroupId: Int,
    defaultTitle: String? = null
) {
    val root = view ?: return
    val toolbar = root.findViewById<MaterialToolbar>(toolbarId) ?: return
    val group = root.findViewById<MaterialButtonToggleGroup>(toggleGroupId) ?: return

    fun selectedButton(): MaterialButton? {
        val singleId = if (group.isSingleSelection) group.checkedButtonId else View.NO_ID
        if (singleId != View.NO_ID) return root.findViewById(singleId)
        val first = group.checkedButtonIds.firstOrNull() ?: return null
        return root.findViewById(first)
    }

    fun applyTitle() {
        val text = selectedButton()?.text?.toString()?.takeIf { it.isNotBlank() } ?: defaultTitle
        if (!text.isNullOrBlank()) toolbar.title = text
    }

    // Apply immediately and on future changes
    applyTitle()
    group.addOnButtonCheckedListener { _, _, _ -> applyTitle() }
}
