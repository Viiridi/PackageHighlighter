package com.example.labelhighlighter

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {

    private val nutritionalValuePattern = Pattern.compile("(\\d*\\.?\\d+)\\s*(g|mg)", Pattern.CASE_INSENSITIVE)
    private val recogniser = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val keywordMap = mapOf(
        "fat" to "Fat",
        "of which saturates" to "Saturates",
        "l0f which saturates" to "Saturates",
        "saturates" to "Saturates",
        "carbohydrate" to "Carbohydrate",
        "of which sugars" to "Sugars",
        "l0f which sugars" to "Sugars",
        "sugars" to "Sugars",
        "protein" to "Protein",
        "salt" to "Salt"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Starting still image processing (ordered list association, verbose line logging, updated keywordMap)...")
        processTestImage()
    }

    private fun processTestImage() {
        try {
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.test_label)
            if (bitmap == null) {
                Log.e(TAG, "Bitmap could not be decoded from R.drawable.test_label")
                Toast.makeText(this, "Error loading test image.", Toast.LENGTH_LONG).show()
                return
            }
            val image = InputImage.fromBitmap(bitmap, 0)

            Log.d(TAG, "Processing InputImage with TextRecognizer...")
            recogniser.process(image)
                .addOnSuccessListener { visionText ->
                    Log.d(TAG, "--- Still Image Frame Analysed ---")
                    if (visionText.textBlocks.isEmpty()) {
                        Log.d(TAG, "No text found in the still image.")
                    }

                    val identifiedKeywords = mutableListOf<Pair<String, String>>()
                    val identifiedValues = mutableListOf<Pair<String, String>>()

                    for (block in visionText.textBlocks) {
                        for (lineInBlock in block.lines) {
                            val originalLineText = lineInBlock.text
                            val correctedLineText = originalLineText.replace("o", "0", ignoreCase = true)
                            var cleanedLineText = correctedLineText.trimStart('|')

                            Log.v(TAG, "  Processed Line: '$cleanedLineText' (Original: '$originalLineText')")

                            var lineProcessedForKeyword = false
                            for ((prefix, canonicalKeyword) in keywordMap) {
                                if (cleanedLineText.toLowerCase().startsWith(prefix)) {
                                    identifiedKeywords.add(Pair(canonicalKeyword, originalLineText))
                                    Log.d(TAG, "    FOUND KEYWORD (prefix: '$prefix' -> '$canonicalKeyword') on line: '$cleanedLineText'")
                                    lineProcessedForKeyword = true
                                    break
                                }
                            }

                            val generalMatcher = nutritionalValuePattern.matcher(cleanedLineText) 
                            if (generalMatcher.find()) {
                                val value = generalMatcher.group(1)
                                val unit = generalMatcher.group(2)
                                val valueWithUnit = "$value$unit"
                                
                                if (!(cleanedLineText.contains("Typical Values per", ignoreCase = true) && valueWithUnit == "100g")) {
                                    identifiedValues.add(Pair(valueWithUnit, originalLineText))
                                    Log.d(TAG, "    FOUND VALUE: '$valueWithUnit' on line: '$cleanedLineText'")
                                } else {
                                    Log.d(TAG, "    Skipped header value: '$valueWithUnit' on line: '$cleanedLineText'")
                                }
                            }
                        }
                    }

                    Log.d(TAG, "--- Association Phase ---")
                    Log.d(TAG, "Identified Keywords (${identifiedKeywords.size}):")
                    identifiedKeywords.forEachIndexed { index, pair -> Log.d(TAG, "  KW[$index]: ${pair.first} (from '${pair.second}')") }
                    Log.d(TAG, "Identified Values (${identifiedValues.size}):")
                    identifiedValues.forEachIndexed { index, pair -> Log.d(TAG, "  VAL[$index]: ${pair.first} (from '${pair.second}')") }

                    // Keep targeting Sugars for consistency in testing this fix
                    val targetKeywordToAssociate = "Sugars" 
                    val keywordIndex = identifiedKeywords.indexOfFirst { it.first.equals(targetKeywordToAssociate, ignoreCase = true) }

                    if (keywordIndex != -1) {
                        Log.i(TAG, "Keyword '$targetKeywordToAssociate' found at index $keywordIndex in keyword list.")
                        if (identifiedValues.size > keywordIndex) {
                            val associatedValue = identifiedValues[keywordIndex].first
                            val originalLineForValue = identifiedValues[keywordIndex].second
                            Log.i(TAG, "  >> Associated '$targetKeywordToAssociate' with value: '$associatedValue' (from original line: '$originalLineForValue')")
                        } else {
                            Log.w(TAG, "  >> '$targetKeywordToAssociate' found, but not enough values in list (${identifiedValues.size}) to associate by index $keywordIndex.")
                        }
                    } else {
                        Log.w(TAG, "Keyword '$targetKeywordToAssociate' not found in the identified keyword list.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Text recognition failed for still image", e)
                }
                .addOnCompleteListener {
                    Log.d(TAG, "Still image processing complete.")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up still image processing", e)
            Toast.makeText(this, "Error during still image processing setup.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        private const val TAG = "LabelHighlighterApp"
    }
}
