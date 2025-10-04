package com.example.labelhighlighter

import com.google.gson.annotations.SerializedName

data class OcrResult(
    @SerializedName("lines") val lines: List<OcrLine>
)

data class OcrLine(
    @SerializedName("text") val text: String,
    @SerializedName("box") val box: List<Int>
)
