package com.example.filkomplain.model

data class KomplainModel(
    val judul: String = "",
    val deskripsi: String = "",
    val lokasi: String = "",
    val kontak: String = "",
    val tanggal: String = "",
    val imageUrl: String = "",
    val timestamp: Long = 0L,
    val uid: String = "",
    var id: String = ""
)