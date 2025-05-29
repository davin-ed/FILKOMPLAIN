package com.example.filkomplain

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.widget.*
import android.os.Bundle
import android.content.Intent
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var editNama: EditText
    private lateinit var editNIM: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPhone: EditText
    private lateinit var editPassword: EditText
    private lateinit var btnDaftar: Button
    private lateinit var textMasuk: TextView

    private lateinit var iconName: ImageView
    private lateinit var iconNIM: ImageView
    private lateinit var iconEmail: ImageView
    private lateinit var iconPhone: ImageView
    private lateinit var iconPassword: ImageView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        editNama = findViewById(R.id.editNama)
        editNIM = findViewById(R.id.editNIM)
        editEmail = findViewById(R.id.editEmail)
        editPhone = findViewById(R.id.editPhone)
        editPassword = findViewById(R.id.editPassword)
        btnDaftar = findViewById(R.id.btnDaftar)

        textMasuk = findViewById(R.id.textMasuk)

        val fullText = "Sudah punya akun? Masuk"
        val spannable = SpannableString(fullText)

        val startIndex = fullText.indexOf("Masuk")
        val endIndex = startIndex + "Masuk".length
        val textColor = ContextCompat.getColor(this, R.color.boldblue)

        spannable.setSpan(UnderlineSpan(), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(textColor), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        textMasuk.text = spannable

        iconName = findViewById(R.id.iconName)
        iconNIM = findViewById(R.id.iconNIM)
        iconEmail = findViewById(R.id.iconEmail)
        iconPhone = findViewById(R.id.iconPhone)
        iconPassword = findViewById(R.id.iconPassword)

        btnDaftar.isEnabled = false

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                btnDaftar.isEnabled = areAllFieldsFilled()
            }
        }

        editNama.addTextChangedListener(textWatcher)
        editNIM.addTextChangedListener(textWatcher)
        editEmail.addTextChangedListener(textWatcher)
        editPhone.addTextChangedListener(textWatcher)
        editPassword.addTextChangedListener(textWatcher)

        editNama.addTextChangedListener {
            if (!it.isNullOrEmpty()) {
                editNama.setBackgroundResource(R.drawable.bg_form_input_filled)
                editNama.setTextColor(ContextCompat.getColor(this, R.color.blue))
                iconName.setColorFilter(ContextCompat.getColor(this, R.color.blue))
            } else {
                editNama.setBackgroundResource(R.drawable.bg_form_input)
                iconName.colorFilter = null
            }
        }

        editNIM.addTextChangedListener {
            if (!it.isNullOrEmpty()) {
                editNIM.setBackgroundResource(R.drawable.bg_form_input_filled)
                editNIM.setTextColor(ContextCompat.getColor(this, R.color.blue))
                iconNIM.setColorFilter(ContextCompat.getColor(this, R.color.blue))
            } else {
                editNIM.setBackgroundResource(R.drawable.bg_form_input)
                iconNIM.colorFilter = null
            }
        }

        editEmail.addTextChangedListener {
            if (!it.isNullOrEmpty()) {
                editEmail.setBackgroundResource(R.drawable.bg_form_input_filled)
                editEmail.setTextColor(ContextCompat.getColor(this, R.color.blue))
                iconEmail.setColorFilter(ContextCompat.getColor(this, R.color.blue))
            } else {
                editEmail.setBackgroundResource(R.drawable.bg_form_input)
                iconEmail.colorFilter = null
            }
        }

        editPhone.addTextChangedListener {
            if (!it.isNullOrEmpty()) {
                editPhone.setBackgroundResource(R.drawable.bg_form_input_filled)
                editPhone.setTextColor(ContextCompat.getColor(this, R.color.blue))
                iconPhone.setColorFilter(ContextCompat.getColor(this, R.color.blue))
            } else {
                editPhone.setBackgroundResource(R.drawable.bg_form_input)
                iconPhone.colorFilter = null
            }
        }

        editPassword.addTextChangedListener {
            if (!it.isNullOrEmpty()) {
                editPassword.setBackgroundResource(R.drawable.bg_form_input_filled)
                editPassword.setTextColor(ContextCompat.getColor(this, R.color.blue))
                iconPassword.setColorFilter(ContextCompat.getColor(this, R.color.blue))
            } else {
                editPassword.setBackgroundResource(R.drawable.bg_form_input)
                iconPassword.colorFilter = null
            }
        }

        btnDaftar.setOnClickListener {
            val nama = editNama.text.toString().trim()
            val nim = editNIM.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val phone = editPhone.text.toString().trim()
            val password = editPassword.text.toString().trim()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid
                        val userMap = hashMapOf(
                            "nama" to nama,
                            "nim" to nim,
                            "email" to email,
                            "telepon" to phone
                        )
                        if (uid != null) {
                            firestore.collection("users").document(uid).set(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Akunmu berhasil terdaftar!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Gagal menyimpan data.", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Registrasi gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        textMasuk.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }
    }

    private fun areAllFieldsFilled(): Boolean {
        return editNama.text.toString().trim().isNotEmpty() &&
                editNIM.text.toString().trim().isNotEmpty() &&
                editEmail.text.toString().trim().isNotEmpty() &&
                editPhone.text.toString().trim().isNotEmpty() &&
                editPassword.text.toString().trim().isNotEmpty()
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }
}