package de.atennert.lcarswm.windowactions

import kotlinx.cinterop.convert
import xlib.Window
import kotlin.test.Test
import kotlin.test.assertNull

class WindowFocusHandlerTest {
    @Test
    fun `check that initially there is no focused window`() {
        val windowFocusHandler = WindowFocusHandler()

        assertNull(windowFocusHandler.getFocusedWindow(), "There is no focused window")

        var activeWindow: Window? = 42.convert()
        windowFocusHandler.registerObserver {activeWindow = it}
        assertNull(activeWindow, "The observer should get null window")
    }
}
