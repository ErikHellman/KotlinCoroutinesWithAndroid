package se.hellsoft.kotlincoroutines

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
  data class Cat(val id: Int, val uri: String, val photo: Bitmap)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val catAdapter = CatAdapter()
    catAdapter.cats = loadCats()
    listOfCats.adapter = catAdapter
  }

  private fun loadCats(): List<Cat> {
    val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.TITLE)
    val cursor = MediaStore.Images.Media.query(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection)
    return cursor?.let {
      generateSequence { if(it.moveToNext()) it else null }
          .map {
            val photoId = it.getInt(it.getColumnIndex(MediaStore.Images.Media._ID))
            val uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, photoId.toString())
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            Cat(photoId, it.getString(it.getColumnIndex(MediaStore.Images.Media.TITLE)), bitmap)
          }
          .toList()
    } ?: emptyList()
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
