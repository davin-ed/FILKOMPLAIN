package com.example.filkomplain

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Environment
import android.provider.MediaStore
import com.bumptech.glide.request.transition.Transition
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.android.material.imageview.ShapeableImageView

class KomplainAdapter(
    private val listKomplain: MutableList<KomplainModel>,
    private val onDeleteClick: (KomplainModel) -> Unit
) : RecyclerView.Adapter<KomplainAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val phItemKomplain: ShapeableImageView = itemView.findViewById(R.id.phItemKomplain)
        val titleItemKomplain: TextView = itemView.findViewById(R.id.titleItemKomplain)
        val textLokasiKomplain: TextView = itemView.findViewById(R.id.textLokasiKomplain)
        val textTanggalKomplain: TextView = itemView.findViewById(R.id.textTanggalKomplain)
        val bgTwoBtnKomplain: View = itemView.findViewById(R.id.bgTwoBtnKomplain)
        val btnDloadDelKomplain: View = itemView.findViewById(R.id.btnDloadDelKomplain)
        val btnDloadKomplain: View = itemView.findViewById(R.id.btnDloadKomplain)
        val btnDelKomplain: View = itemView.findViewById(R.id.btnDelKomplain)
        val overlaySelected: View = itemView.findViewById(R.id.overlaySelected)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_komplain, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val komplain = listKomplain[position]

        holder.titleItemKomplain.text = komplain.judul
        holder.textLokasiKomplain.text = komplain.lokasi
        holder.textTanggalKomplain.text = komplain.tanggal

        Glide.with(holder.itemView.context)
            .load(komplain.imageUrl)
            .placeholder(R.drawable.ph_img_komplain)
            .into(holder.phItemKomplain)

        holder.overlaySelected.visibility = if (komplain.isSelected) View.VISIBLE else View.GONE
        holder.bgTwoBtnKomplain.visibility = if (komplain.isSelected) View.VISIBLE else View.GONE
        holder.btnDloadDelKomplain.visibility = if (komplain.isSelected) View.VISIBLE else View.GONE

        holder.itemView.setOnLongClickListener {
            komplain.isSelected = true

            holder.overlaySelected.alpha = 0f
            holder.overlaySelected.visibility = View.VISIBLE
            holder.overlaySelected.animate().alpha(1f).setDuration(200).start()

            holder.bgTwoBtnKomplain.alpha = 0f
            holder.bgTwoBtnKomplain.visibility = View.VISIBLE
            holder.bgTwoBtnKomplain.animate().alpha(1f).setDuration(200).start()

            holder.btnDloadDelKomplain.alpha = 0f
            holder.btnDloadDelKomplain.visibility = View.VISIBLE
            holder.btnDloadDelKomplain.animate().alpha(1f).setDuration(200).start()

            true
        }

        holder.itemView.setOnClickListener {
            if (komplain.isSelected) {
                komplain.isSelected = false

                holder.overlaySelected.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        holder.overlaySelected.visibility = View.GONE
                        holder.overlaySelected.alpha = 1f
                    }.start()

                holder.bgTwoBtnKomplain.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        holder.bgTwoBtnKomplain.visibility = View.GONE
                        holder.bgTwoBtnKomplain.alpha = 1f
                    }.start()

                holder.btnDloadDelKomplain.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        holder.btnDloadDelKomplain.visibility = View.GONE
                        holder.btnDloadDelKomplain.alpha = 1f
                    }.start()
            }
        }

        holder.btnDloadKomplain.setOnClickListener {
            val context = holder.itemView.context
            Glide.with(context)
                .asBitmap()
                .load(komplain.imageUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        saveImageToDownloads(context, resource)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {}
                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        Toast.makeText(context, "Gagal mengambil gambar", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        holder.btnDelKomplain.setOnClickListener {
            onDeleteClick(komplain)
        }
    }

    private fun saveImageToDownloads(context: Context, bitmap: Bitmap) {
        val filename = "komplain_${System.currentTimeMillis()}.jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/FILKOMPLAIN")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let { contentUri ->
            val outputStream = resolver.openOutputStream(contentUri)
            if (outputStream != null) {
                outputStream.use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                }
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(contentUri, contentValues, null, null)

                Toast.makeText(context, "Gambar berhasil disimpan ke galerimu!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Gagal membuka stream penyimpanan", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(context, "Gagal menyimpan gambar", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = listKomplain.size
}