package com.example

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
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
    assertTrue(appName.isNotEmpty())
  }

  @Test
  fun testLoadNovaDrawable() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val drawable = ContextCompat.getDrawable(context, R.drawable.nova)
    assertNotNull("Drawable could not be loaded!", drawable)
  }
}
