package se.hellsoft.kotlincoroutines

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.actor_activity.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.delay

class ActorActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.actor_activity)

    val jobActor = actor<Unit>(UI) { for (event in channel) doJob() }
    jobButton.setOnClickListener {
      jobActor.offer(Unit)
    }
  }

  suspend fun doJob() {
    jobText.append("Job started...\n")
    delay(1000)
    jobText.append("still working...\n")
    delay(1000)
    jobText.append("just a bit more...\n")
    delay(1000)
    jobText.append("Job done!\n")
  }
}
