package com.cameraplanner.eventscanner

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TextRecognitionActivity : AppCompatActivity() {

    private lateinit var selectedImage: ImageView
    private lateinit var textView: TextView
    private lateinit var selectImageButton: Button

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            selectedImage.setImageBitmap(bitmap)
            recognizeText(InputImage.fromBitmap(bitmap, 0))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.text_recognition)

        setupBackButton()

        selectedImage = findViewById(R.id.selectedImage)
        textView = findViewById(R.id.textView)
        selectImageButton = findViewById(R.id.button_image)

        selectImageButton.setOnClickListener {
            getContent.launch("image/*")
        }
    }

    private fun recognizeText(image: InputImage) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val text = visionText.text
                textView.text = text

                val dates = extractDates(text).map { formatDateString(it) }

                if (dates.isNotEmpty()) {
                    goToCalendarView(dates)
                }
            }
            .addOnFailureListener { e ->
                Log.e("TextRecognition", "Text recognition failed: ${e.message}")
            }
    }



    private fun setupBackButton() {
        val buttonCalendar: Button = findViewById(R.id.calendar_button)
        buttonCalendar.setOnClickListener {
            gotoCalendar()
        }
    }

    private fun gotoCalendar() {
        val intent = Intent(this, CalendarView::class.java)
        startActivity(intent)
        finish()
    }


    private fun extractDates(text: String): List<String> {
        val datePatterns = listOf(
            "\\b\\d{1,2}[-/\\.]\\d{1,2}[-/\\.]\\d{4}\\b", // Formats like 11-01-2023, 11/01/2023, 11.01.2023
            "\\b\\d{4}[-/\\.]\\d{1,2}[-/\\.]\\d{1,2}\\b",  // Formats like 2023-11-01, 2023/11/01, 2023.11.01
            "\\b(?:January|February|March|April|May|June|July|August|September|October|November|December)\\s\\d{1,2},\\s\\d{4}\\b" // Formats like October 12, 2024
        )

        val dates = mutableListOf<String>()
        for (pattern in datePatterns) {
            val regex = Regex(pattern)
            dates.addAll(regex.findAll(text).map { it.value })
        }
        return dates.distinct() // Return unique dates
    }


    private fun formatDateString(dateString: String): String {
        val dateFormats = listOf(
            SimpleDateFormat("MMMM d, yyyy", Locale.US), // for "October 24, 2023"
            SimpleDateFormat("MM/dd/yyyy", Locale.US), // for "10/24/2023"
            SimpleDateFormat("yyyy/MM/dd", Locale.US) // for "2023/10/24"
        )

        var parsedDate: Date? = null
        for (format in dateFormats) {
            try {
                parsedDate = format.parse(dateString)
                if (parsedDate != null) {
                    break
                }
            } catch (e: ParseException) {
                // Continue to try the next format if the current one fails
            }
        }

        val desiredFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        return if (parsedDate != null) {
            desiredFormat.format(parsedDate)
        } else {
            "Invalid Date" // or throw an exception if preferred
        }
    }


    private fun goToCalendarView(dates: List<String>) {
        val intent = Intent(this, CalendarView::class.java)
        intent.putStringArrayListExtra("EXTRA_DATES", ArrayList(dates))
        startActivity(intent)
    }
}




