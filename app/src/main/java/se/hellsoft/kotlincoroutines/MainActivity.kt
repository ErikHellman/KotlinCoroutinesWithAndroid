package se.hellsoft.kotlincoroutines

import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : AppCompatActivity() {
  data class Cat(val id: Int, val uri: String, val photo: Bitmap)

  fun loge(e: Throwable, message: () -> String) {
    Log.e("MainActivity", message(), e)
  }

  fun logd(message: () -> String) {
    Log.d("MainActivity", message())
  }

  private var channel: ReceiveChannel<Cat>? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val linearLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    linearLayoutManager.stackFromEnd = true
    listOfCats.layoutManager = linearLayoutManager
    val catAdapter = CatAdapter()
    catAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
      override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        super.onItemRangeInserted(positionStart, itemCount)
        listOfCats.scrollToPosition(positionStart)
      }
    })
    listOfCats.adapter = catAdapter
    logd { "Load photos!" }
    val number = AtomicInteger(0)
    channel = produce {
      val queryDatabase = queryDatabase()
      val cursor = object : Cursor by queryDatabase {
        override fun close() {
          logd { "Closing!" }
          queryDatabase.close()
        }
      }

      cursor.use {
        while (it.moveToNext()) {
          send(cursorToData(it))
          logd { "Cursor is at last: ${it.isLast}" }
        }
      }
    }

    floatingActionButton.setOnClickListener {
      async(UI) {
        val cat = channel?.receiveOrNull()
        cat?.let {
          logd { "Adding next cat on ${Thread.currentThread().name}: $cat" }
          catAdapter.cats += it
          catAdapter.notifyItemInserted(catAdapter.cats.lastIndex)
        }
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    channel?.let {
      logd { "Cancelling channel!" }
      it.cancel()
    }
  }

  private fun queryDatabase(): Cursor {
    val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.TITLE)
    return MediaStore.Images.Media.query(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection)
  }

  private fun cursorToData(it: Cursor): Cat {
    logd { "Building a cat on ${Thread.currentThread().name}" }
    val photoId = it.getInt(it.getColumnIndex(MediaStore.Images.Media._ID))
    val uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, photoId.toString())
    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
    val scaled = Bitmap.createScaledBitmap(bitmap, bitmap.width / 3, bitmap.height / 3, true)
    return Cat(photoId, it.getString(it.getColumnIndex(MediaStore.Images.Media.TITLE)), scaled)
  }

  class CatAdapter : RecyclerView.Adapter<CatViewHolder>() {
    var cats = emptyList<Cat>()

    override fun onBindViewHolder(holder: CatViewHolder?, position: Int) {
      holder?.photoView?.setImageBitmap(cats[position].photo)
    }

    override fun getItemCount(): Int = cats.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CatViewHolder {
      val view = LayoutInflater.from(parent?.context).inflate(R.layout.cat_card, parent, false)
      return CatViewHolder(view)
    }


  }

  class CatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val photoView = itemView.findViewById<ImageView>(R.id.catPhoto)
  }
}
