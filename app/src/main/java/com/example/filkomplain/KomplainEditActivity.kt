package com.example.filkomplain

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.addTextChangedListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class KomplainEditActivity : AppCompatActivity() {

    private lateinit var btnEditFotoKomplain: Button
    private lateinit var textUpFoto: TextView
    private lateinit var textDescUpFoto: TextView

    private lateinit var editJudul: EditText
    private lateinit var editDescKomplain: EditText
    private lateinit var editLokasi: EditText
    private lateinit var editKontak: EditText

    private lateinit var iconJudul: ImageView
    private lateinit var iconDescKomplain: ImageView
    private lateinit var iconLokasi: ImageView
    private lateinit var iconKontak: ImageView

    private lateinit var btnSelesaiEditKomplain: Button

    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>

    private var uploadedImageUrl: String? = null
    private var selectedImageUri: Uri? = null

    private lateinit var firestore: FirebaseFirestore
    private var komplainId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_komplain_edit)

        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        btnEditFotoKomplain = findViewById(R.id.btnEditFotoKomplain)
        btnSelesaiEditKomplain = findViewById(R.id.btnEditKomplain)
        textUpFoto = findViewById(R.id.textUpFoto)
        textDescUpFoto = findViewById(R.id.textDescUpFoto)

        editJudul = findViewById(R.id.editJudul)
        editDescKomplain = findViewById(R.id.editDescKomplain)
        editLokasi = findViewById(R.id.editLokasi)
        editKontak = findViewById(R.id.editKontak)

        iconJudul = findViewById(R.id.iconJudul)
        iconDescKomplain = findViewById(R.id.iconDescKomplain)
        iconLokasi = findViewById(R.id.iconLokasi)
        iconKontak = findViewById(R.id.iconKontak)

        firestore = FirebaseFirestore.getInstance()

        komplainId = intent.getStringExtra("komplainId")

        oldJudul = intent.getStringExtra("judul")
        oldDescKomplain = intent.getStringExtra("deskripsi")
        oldLokasi = intent.getStringExtra("lokasi")
        oldKontak = intent.getStringExtra("kontak")
        oldImageUrl = intent.getStringExtra("imageUrl")

        editJudul.setText(oldJudul)
        editDescKomplain.setText(oldDescKomplain)
        editLokasi.setText(oldLokasi)
        editKontak.setText(oldKontak)

        applyFieldStyle(editJudul, iconJudul)
        applyFieldStyle(editDescKomplain, iconDescKomplain)
        applyFieldStyle(editLokasi, iconLokasi)
        applyFieldStyle(editKontak, iconKontak)

        tampilanSebelumUpload()

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                uploadImageToServer(it)
            }
        }

        btnEditFotoKomplain.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        btnSelesaiEditKomplain.isEnabled = areAllFieldsFilled()

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                btnSelesaiEditKomplain.isEnabled = areAllFieldsFilled()
            }
        }

        editJudul.addTextChangedListener(textWatcher)
        editDescKomplain.addTextChangedListener(textWatcher)
        editLokasi.addTextChangedListener(textWatcher)
        editKontak.addTextChangedListener(textWatcher)

        editJudul.addTextChangedListener {
            if (!it.isNullOrBlank()) {
                editJudul.setBackgroundResource(R.drawable.bg_form_input_filled)
                editJudul.setTextColor(ContextCompat.getColor(this, R.color.blue))
                iconJudul.setColorFilter(ContextCompat.getColor(this, R.color.blue))
            } else {
                editJudul.setBackgroundResource(R.drawable.bg_form_input)
                iconJudul.colorFilter = null
            }
        }

        editDescKomplain.addTextChangedListener {
            if (!it.isNullOrBlank()) {
                editDescKomplain.setBackgroundResource(R.drawable.bg_form_input_filled)
                editDescKomplain.setTextColor(ContextCompat.getColor(this, R.color.blue))
                iconDescKomplain.setColorFilter(ContextCompat.getColor(this, R.color.blue))
            } else {
                editDescKomplain.setBackgroundResource(R.drawable.bg_form_input)
                iconDescKomplain.colorFilter = null
            }
        }

        editLokasi.addTextChangedListener {
            if (!it.isNullOrBlank()) {
                editLokasi.setBackgroundResource(R.drawable.bg_form_input_filled)
                editLokasi.setTextColor(ContextCompat.getColor(this, R.color.blue))
                iconLokasi.setColorFilter(ContextCompat.getColor(this, R.color.blue))
            } else {
                editLokasi.setBackgroundResource(R.drawable.bg_form_input)
                iconLokasi.colorFilter = null
            }
        }

        editKontak.addTextChangedListener {
            if (!it.isNullOrBlank()) {
                editKontak.setBackgroundResource(R.drawable.bg_form_input_filled)
                editKontak.setTextColor(ContextCompat.getColor(this, R.color.blue))
                iconKontak.setColorFilter(ContextCompat.getColor(this, R.color.blue))
            } else {
                editKontak.setBackgroundResource(R.drawable.bg_form_input)
                iconKontak.colorFilter = null
            }
        }

        btnSelesaiEditKomplain.setOnClickListener {
            val newJudul = editJudul.text.toString().trim()
            val newDeskripsi = editDescKomplain.text.toString().trim()
            val newLokasi = editLokasi.text.toString().trim()
            val newKontak = editKontak.text.toString().trim()

            updateKomplain(
                newJudul,
                newDeskripsi,
                newLokasi,
                newKontak,
                uploadedImageUrl ?: oldImageUrl.orEmpty()
            ) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom)
                finish()
            }
        }
    }

    private fun areAllFieldsFilled(): Boolean {
        return editJudul.text.toString().trim().isNotEmpty() &&
                editDescKomplain.text.toString().trim().isNotEmpty() &&
                editLokasi.text.toString().trim().isNotEmpty() &&
                editKontak.text.toString().trim().isNotEmpty()
    }

    private fun applyFieldStyle(editText: EditText, icon: ImageView) {
        if (!editText.text.isNullOrBlank()) {
            editText.setBackgroundResource(R.drawable.bg_form_input_filled)
            editText.setTextColor(ContextCompat.getColor(this, R.color.blue))
            icon.setColorFilter(ContextCompat.getColor(this, R.color.blue))
        } else {
            editText.setBackgroundResource(R.drawable.bg_form_input)
            icon.colorFilter = null
        }
    }

    private var oldJudul: String? = null
    private var oldDescKomplain: String? = null
    private var oldLokasi: String? = null
    private var oldKontak: String? = null
    private var oldImageUrl: String? = null

    private fun updateKomplain(
        newJudul: String,
        newDeskripsi: String,
        newLokasi: String,
        newKontak: String,
        newImageUrl: String,
        onComplete: () -> Unit
    ) {
        val updates = mutableMapOf<String, Any>()

        if (newJudul != oldJudul) updates["judul"] = newJudul
        if (newDeskripsi != oldDescKomplain) updates["deskripsi"] = newDeskripsi
        if (newLokasi != oldLokasi) updates["lokasi"] = newLokasi
        if (newKontak != oldKontak) updates["kontak"] = newKontak
        if (newImageUrl != oldImageUrl) updates["imageUrl"] = newImageUrl

        val updateFirestore = {
            firestore.collection("komplain").document(komplainId!!)
                .set(updates, SetOptions.merge()) // set() + merge
                .addOnSuccessListener {
                    Toast.makeText(this, "Laporanmu berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    oldJudul = newJudul
                    oldDescKomplain = newDeskripsi
                    oldLokasi = newLokasi
                    oldKontak = newKontak
                    onComplete()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal memperbarui laporan", Toast.LENGTH_SHORT).show()
                }
        }

        if (updates.isNotEmpty()) {
            updateFirestore()
        } else {
            Toast.makeText(this, "Laporanmu tetap sama seperti sebelumnya.", Toast.LENGTH_SHORT).show()
            onComplete()
        }
    }

    private fun tampilanSebelumUpload() {
        btnEditFotoKomplain.apply {
            background = ContextCompat.getDrawable(this@KomplainEditActivity, R.drawable.bg_btn_edit_photo_ripple)
            isClickable = true
        }
        textUpFoto.text = "Ubah foto"
        textDescUpFoto.text = "Kamu dapat mengubah\nfoto laporanmu di sini"
    }

    private fun tampilanSetelahUploadBerhasil() {
        btnEditFotoKomplain.apply {
            background = ContextCompat.getDrawable(this@KomplainEditActivity, R.drawable.bg_btn_uploaded_photo)
            isClickable = false
        }
        textUpFoto.text = "Foto berhasil diubah"
        textDescUpFoto.text = "Pastikan kamu mengunggah \nfoto yang tepat"
    }

    private fun uploadImageToServer(uri: Uri) {
        val file = uriToFile(uri, this)
        if (file == null) {
            Toast.makeText(this, "Gagal mendapatkan file dari gambar", Toast.LENGTH_SHORT).show()
            return
        }

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
        val description = "Foto Komplain".toRequestBody("text/plain".toMediaTypeOrNull())

        val apiService = RetrofitClient.instance.create(ApiService::class.java)
        apiService.uploadImage(body, description).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val raw = response.body()?.string()
                    Toast.makeText(this@KomplainEditActivity, "Upload berhasil", Toast.LENGTH_SHORT).show()

                    val prefix = "File berhasil di-upload: "
                    if (raw != null && raw.startsWith(prefix)) {
                        val fileName = raw.substringAfter(prefix).trim()
                        val serverUrl = RetrofitClient.getUploadsUrl()
                        uploadedImageUrl = "$serverUrl$fileName"

                        tampilanSetelahUploadBerhasil()
                        btnSelesaiEditKomplain.isEnabled = areAllFieldsFilled() && uploadedImageUrl != null
                    } else {
                        Toast.makeText(this@KomplainEditActivity, "Format respon tidak dikenali", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    val error = response.errorBody()?.string()
                    Toast.makeText(this@KomplainEditActivity, "Upload gagal: $error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@KomplainEditActivity, "Gagal upload: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun uriToFile(uri: Uri, context: Context): File? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("temp_image", ".jpg", context.cacheDir)
        tempFile.outputStream().use { fileOut ->
            inputStream.copyTo(fileOut)
        }
        return tempFile
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom)
        finish()
    }
}