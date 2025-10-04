package com.example.labelhighlighter

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "Starting OpenAI Vision analysis...")
        processTestImageWithOpenAI()
    }

    private fun processTestImageWithOpenAI() {
        lifecycleScope.launch {
            try {
                val bitmap = BitmapFactory.decodeResource(resources, R.drawable.test_label)
                if (bitmap == null) {
                    Log.e(TAG, "Bitmap could not be decoded from R.drawable.test_label")
                    Toast.makeText(this@MainActivity, "Error loading test image.", Toast.LENGTH_LONG).show()
                    return@launch
                }

                Log.d(TAG, "Image loaded, sending to OpenAI for analysis...")

                val result = OpenAIHelper.analyzeImage(bitmap)

                Log.i(TAG, "--- OpenAI Analysis Result ---")
                Log.i(TAG, result)
                Log.i(TAG, "-----------------------------")

            } catch (e: Exception) {
                Log.e(TAG, "Error during OpenAI processing", e)
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val TAG = "LabelHighlighterApp"
    }
}
