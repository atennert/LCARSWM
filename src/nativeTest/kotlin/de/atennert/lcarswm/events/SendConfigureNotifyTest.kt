package de.atennert.lcarswm.events

import de.atennert.lcarswm.system.LoggingSystemFacadeMock
import kotlinx.cinterop.*
import xlib.ConfigureNotify
import xlib.Window
import xlib.XEvent
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 *
 */
class SendConfigureNotifyTest {
    @Test
    fun `send configure notify with measurements`() {
        val eventMock = XEventMock()

        val windowId: Window = 1.convert()
        val measurements = listOf(1, 2, 3, 4)

        sendConfigureNotify(eventMock, windowId, measurements)

        assertEquals(windowId, eventMock.eventWindowId, "Invalid window ID in event call")
        assertEquals(ConfigureNotify, eventMock.eventType, "The event type was not ConfigureNotify")
        assertEquals(measurements, eventMock.measurements, "The window measurements were wrong")
    }

    private class XEventMock : LoggingSystemFacadeMock() {
        var eventWindowId: Window? = null
        var eventType: Int? = null
        var measurements: List<Int>? = null

        override fun sendEvent(
            window: Window,
            propagate: Boolean,
            eventMask: Long,
            event: CPointer<XEvent>
        ): Int {
            val eventValue = event.pointed
            val configure = eventValue.xconfigure

            this.eventWindowId = window
            this.eventType = eventValue.type
            this.measurements = listOf(configure.x, configure.y, configure.width, configure.height)

            return 0
        }
    }
}