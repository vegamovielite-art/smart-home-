package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.preferences.SmartHomePreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Smart Home", appName)
  }

  @Test
  fun `verify default preferences`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = SmartHomePreferences(context)
    assertEquals("192.168.1.100", prefs.esp32Ip)
    assertEquals(80, prefs.esp32Port)
    assertNotNull(prefs.getBaseUrl())
  }
}
