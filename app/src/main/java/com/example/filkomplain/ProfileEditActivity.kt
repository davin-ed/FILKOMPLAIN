package com.example.filkomplain

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
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

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var phFotoProfil: ImageView
    private lateinit var btnEditPfp: Button

    private lateinit var editNamaProfil: EditText
    private lateinit var editNIMProfil: EditText
    private lateinit var editProdiProfil: EditText
    private lateinit var editEmailProfil: EditText
    private lateinit var editPhoneProfil: EditText

    private lateinit var iconName: ImageView
    private lateinit var iconNIM: ImageView
    private lateinit var iconProdi: ImageView
    private lateinit var iconEmail: ImageView
    private lateinit var iconPhone: ImageView

    private lateinit var btnSelesaiEditProfil: Button

    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>

    private var uploadedImageUrl: String? = null
    private var selectedImageUri: Uri? = null

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private var isFromGoogleLogin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        phFotoProfil = findViewById(R.id.phFotoProfil)
        btnEditPfp = findViewById(R.id.btnEditPfp)
        btnSelesaiEditProfil = findViewById(R.id.btnSelesaiEditProfil)

        editNamaProfil = findViewById(R.id.editNamaProfil)
        editNIMProfil = findViewById(R.id.editNIMProfil)
        editProdiProfil = findViewById(R.id.editProdiProfil)
        editEmailProfil = findViewById(R.id.editEmailProfil)
        editPhoneProfil = findViewById(R.id.editPhoneProfil)

        iconName = findViewById(R.id.iconName)
        iconNIM = findViewById(R.id.iconNIM)
        iconProdi = findViewById(R.id.iconProdi)
        iconEmail = findViewById(R.id.iconEmail)
        iconPhone = findViewById(R.id.iconPhone)

        isFromGoogleLogin = intent.getBooleanExtra("fromGoogleLogin", false)

        loadUserProfile()

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                uploadImageToServer(it)
            }
        }

        btnEditPfp.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        btnSelesaiEditProfil.isEnabled = false

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                btnSelesaiEditProfil.isEnabled = areAllFieldsFilled()
            }
        }

        editNamaProfil.addTextChangedListener(textWatcher)
        editNIMProfil.addTextChangedListener(textWatcher)
        editPhoneProfil.addTextChangedListener(textWatcher)

        editNIMProfil.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val nim = s.toString()
                if (nim.length >= 8) {
                    val kodeProdi = nim.substring(6, 8)

                    val namaProdi = when (kodeProdi) {
                        "20" -> "Teknik Informatika"
                        "30" -> "Teknik Komputer"
                        "40" -> "Sistem Informasi"
                        "60" -> "Pendidikan Teknologi Informasi"
                        "70" -> "Teknologi Informasi"
                        else -> "Program Studi Tidak Dikenal"
                    }

                    editProdiProfil.setText(namaProdi)
                } else {
                    editProdiProfil.setText("")
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        editNamaProfil.addTextChangedListener {
            if (!it.isNullOrBlank()) {
                editNamaProfil.setBackgroundResource(R.drawable.bg_form_input_filled)
                editNamaProfil.setTextColor(ContextCompat.getColor(this, R.color.blue))
                iconName.setColorFilter(ContextCompat.getColor(this, R.color.blue))
            } else {
                editNamaProfil.setBackgroundResource(R.drawable.bg_form_input)
                iconName.colorFilter = null
            }
        }

        editNIMProfil.addTextChangedListener {
            if (!it.isNullOrBlank()) {
                editNIMProfil.setBackgroundResource(R.drawable.bg_form_input_filled)
                editNIMProfil.setTextColor(ContextCompat.getColor(this, R.color.blue))
                iconNIM.setColorFilter(ContextCompat.getColor(this, R.color.blue))
            } else {
                editNIMProfil.setBackgroundResource(R.drawable.bg_form_input)
                iconNIM.colorFilter = null
            }
        }

        editProdiProfil.addTextChangedListener {
            if (!it.isNullOrBlank()) {
                editProdiProfil.setBackgroundResource(R.drawable.bg_form_input_filled)
                editProdiProfil.setTextColor(ContextCompat.getColor(this, R.color.blue))
                iconProdi.setColorFilter(ContextCompat.getColor(this, R.color.blue))
            } else {
                editProdiProfil.setBackgroundResource(R.drawable.bg_form_input)
                iconProdi.colorFilter = null
            }
        }

        editEmailProfil.addTextChangedListener {
            if (!it.isNullOrBlank()) {
                editEmailProfil.setBackgroundResource(R.drawable.bg_form_input_filled)
                editEmailProfil.setTextColor(ContextCompat.getColor(this, R.color.blue))
                iconEmail.setColorFilter(ContextCompat.getColor(this, R.color.blue))
            } else {
                editEmailProfil.setBackgroundResource(R.drawable.bg_form_input)
                iconEmail.colorFilter = null
            }
        }

        editPhoneProfil.addTextChangedListener {
            if (!it.isNullOrBlank()) {
                editPhoneProfil.setBackgroundResource(R.drawable.bg_form_input_filled)
                editPhoneProfil.setTextColor(ContextCompat.getColor(this, R.color.blue))
                iconPhone.setColorFilter(ContextCompat.getColor(this, R.color.blue))
            } else {
                editPhoneProfil.setBackgroundResource(R.drawable.bg_form_input)
                iconPhone.colorFilter = null
            }
        }

        btnSelesaiEditProfil.setOnClickListener {
            updateProfile {
                if (isFromGoogleLogin) {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                    finish()
                } else {
                    val intent = Intent(this, ProfileShowActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                    finish()
                }
            }
        }
    }

    private fun areAllFieldsFilled(): Boolean {
        return editNamaProfil.text.toString().trim().isNotEmpty() &&
                editNIMProfil.text.toString().trim().isNotEmpty() &&
                editPhoneProfil.text.toString().trim().isNotEmpty()
    }

    private var oldNama: String? = null
    private var oldNIM: String? = null
    private var oldEmail: String? = null
    private var oldTelepon: String? = null
    private var oldImageUrl: String? = null

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        oldNama = document.getString("nama")
                        oldNIM = document.getString("nim")
                        oldEmail = document.getString("email")
                        oldTelepon = document.getString("telepon")
                        oldImageUrl = document.getString("imageUrl")

                        val imageUrl = document.getString("imageUrl")

                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(this@ProfileEditActivity)
                                .load(imageUrl)
                                .circleCrop()
                                .placeholder(R.drawable.profile_photo)
                                .into(phFotoProfil)
                        }

                        editNamaProfil.setText(oldNama)
                        editPhoneProfil.setText(oldTelepon)

                        val nim = document.getString("nim")
                        val prodi = document.getString("prodi")

                        editNIMProfil.setText(nim)
                        editEmailProfil.setText(currentUser.email)

                        if (prodi.isNullOrBlank() && !nim.isNullOrBlank()) {
                            val kodeProdi = nim.substring(6, 8)

                            val namaProdi = when (kodeProdi) {
                                "20" -> "Teknik Informatika"
                                "30" -> "Teknik Komputer"
                                "40" -> "Sistem Informasi"
                                "60" -> "Pendidikan Teknologi Informasi"
                                "70" -> "Teknologi Informasi"
                                else -> "Program Studi Tidak Dikenal"
                            }
                            editProdiProfil.setText(namaProdi)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal mengambil data profil", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateProfile(onComplete: () -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User tidak ditemukan, silakan login kembali", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val nama = editNamaProfil.text.toString().trim()
        val nim = editNIMProfil.text.toString().trim()
        val email = currentUser.email ?: ""
        val telepon = editPhoneProfil.text.toString().trim()
        val imageUrl = uploadedImageUrl ?: oldImageUrl.orEmpty()

        saveUserData(userId, nama, nim, email, telepon, imageUrl, onComplete)
    }

    private fun saveUserData(
        userId: String,
        newNama: String,
        newNIM: String,
        newEmail: String,
        newTelepon: String,
        newImageUrl: String,
        onComplete: () -> Unit
    ) {
        val updates = mutableMapOf<String, Any>()

        if (newNama != oldNama) updates["nama"] = newNama
        if (newNIM != oldNIM) updates["nim"] = newNIM
        if (newEmail != oldEmail) updates["email"] = newEmail
        if (newTelepon != oldTelepon) updates["telepon"] = newTelepon
       if (newImageUrl != null && newImageUrl != oldImageUrl) updates["imageUrl"] = newImageUrl

        val updateFirestore = {
            firestore.collection("users").document(userId)
                .set(updates, SetOptions.merge()) // set() + merge
                .addOnSuccessListener {
                    Toast.makeText(this, "Profilmu berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    oldNama = newNama
                    oldNIM = newNIM
                    oldEmail = newEmail
                    oldTelepon = newTelepon
                    oldImageUrl = newImageUrl
                    onComplete()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
                }
        }

        if (updates.isNotEmpty()) {
            updateFirestore()
        } else {
            Toast.makeText(this, "Profilmu tetap sama seperti sebelumnya.", Toast.LENGTH_SHORT).show()
            onComplete()
        }
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
                    Toast.makeText(this@ProfileEditActivity, "Upload berhasil", Toast.LENGTH_SHORT).show()

                    val prefix = "File berhasil di-upload: "
                    if (raw != null && raw.startsWith(prefix)) {
                        val fileName = raw.substringAfter(prefix).trim()
                        val serverUrl = RetrofitClient.getUploadsUrl()
                        uploadedImageUrl = "$serverUrl$fileName"

                        Glide.with(this@ProfileEditActivity)
                            .load(uploadedImageUrl)
                            .circleCrop()
                            .placeholder(R.drawable.profile_photo)
                            .into(phFotoProfil)
                    } else {
                        Toast.makeText(this@ProfileEditActivity, "Format respon tidak dikenali", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    val error = response.errorBody()?.string()
                    Toast.makeText(this@ProfileEditActivity, "Upload gagal: $error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@ProfileEditActivity, "Gagal upload: ${t.message}", Toast.LENGTH_SHORT).show()
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
        val intent = Intent(this, ProfileShowActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }
}