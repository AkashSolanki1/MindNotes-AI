package com.aktech.aipowerednotesapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.aktech.aipowerednotesapp.databinding.ActivityAddNoteBinding

class AddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var db: NotesDatabaseHelper
    private val aiProcessor = AIProcessor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = NotesDatabaseHelper(this)


        binding.btnAi.setOnClickListener {
            val content = binding.contentEditText.text.toString().trim()

            if (content.isEmpty()) {
                Toast.makeText(this, "Write something first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Improving note with AI...", Toast.LENGTH_SHORT).show()

            aiProcessor.improveNoteText(content,
                object : AIProcessor.AIResponseCallback {

                    override fun onSuccess(improvedText: String) {
                        runOnUiThread {
                            binding.contentEditText.setText(improvedText)
                        }
                    }

                    override fun onFailure(error: String) {
                        runOnUiThread {
                            Toast.makeText(this@AddNoteActivity, error, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }


        binding.saveButton.setOnClickListener {
            val title = binding.titleEditText.text.toString()
            val content = binding.contentEditText.text.toString()

            val note = Note(0, title, content)
            db.insertNote(note)
            finish()
            Toast.makeText(this, "Note Saved", Toast.LENGTH_SHORT).show()
        }
    }
}
