package se.hellsoft.kotlincoroutines

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.actor_activity.*
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.Ref
import org.jetbrains.anko.coroutines.experimental.asReference
import org.jetbrains.anko.coroutines.experimental.bg
import java.lang.Thread.sleep

class BackgroundJobs : AppCompatActivity() {
  private var job: Deferred<Unit>? = null
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_background_jobs)
    val ref: Ref<BackgroundJobs> = this.asReference()
    jobButton.setOnClickListener {
      async(UI) {
        val data: Deferred<String> = bg {
          loadData()
        }

        ref().jobText.append(data.await())
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    job?.cancel()
  }

  private fun loadData(): String {
    sleep(2500)
    return "Hello!"
  }
}
