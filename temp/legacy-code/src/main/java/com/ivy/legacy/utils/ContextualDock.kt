package com.ivy.legacy.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Contextual dock "+" - pushed screens (Categories, Tags) collect [addRequests]
 * and open their add flow (e.g. Add category / Add tag) when the user taps the
 * dock's center + button.
 *
 * The app shell (IvyNavGraph) renders the floating dock on top of those screens
 * and emits here on tapping the + button instead of blooming the radial add menu.
 */
object ContextualDock {
    private val _addRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val addRequests: SharedFlow<Unit> = _addRequests.asSharedFlow()

    fun requestAdd() {
        _addRequests.tryEmit(Unit)
    }
}
