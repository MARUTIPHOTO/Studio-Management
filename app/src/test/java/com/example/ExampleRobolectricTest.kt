package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
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
    assertEquals("Studio Management", appName)
  }

  @Test
  fun `test phone normalization`() {
    assertEquals("9825012345", com.example.data.repository.StudioRepository.normalizePhone("9825012345"))
    assertEquals("9825012345", com.example.data.repository.StudioRepository.normalizePhone("+91 98250 12345"))
    assertEquals("9825012345", com.example.data.repository.StudioRepository.normalizePhone("09825012345"))
  }
}
