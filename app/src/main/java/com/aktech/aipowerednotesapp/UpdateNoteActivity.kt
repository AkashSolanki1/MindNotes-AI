package com.aktech.aipowerednotesapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.aktech.aipowerednotesapp.databinding.ActivityUpdateBinding

class UpdateNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateBinding
    private lateinit var db: NotesDatabaseHelper
    private val aiProcessor = AIProcessor()
    private var noteId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = NotesDatabaseHelper(this)

        noteId = intent.getIntExtra("note_id", -1)
        if (noteId == -1) {
            finish()
            return
        }

        val note = db.getNoteByID(noteId)
        binding.updateTitleEditText.setText(note.title)
        binding.updateContentEditText.setText(note.content)


        binding.btnAi.setOnClickListener {
            val content = binding.updateContentEditText.text.toString().trim()

            if (content.isEmpty()) {
                Toast.makeText(this, "Nothing to improve", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Improving note with AI...", Toast.LENGTH_SHORT).show()

            aiProcessor.improveNoteText(content,
                object : AIProcessor.AIResponseCallback {

                    override fun onSuccess(improvedText: String) {
                        runOnUiThread {
                            binding.updateContentEditText.setText(improvedText)
                        }
                    }

                    override fun onFailure(error: String) {
                        runOnUiThread {
                            Toast.makeText(this@UpdateNoteActivity, error, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }


        binding.updateSaveButton.setOnClickListener {
            val newTitle = binding.updateTitleEditText.text.toString()
            val newContent = binding.updateContentEditText.text.toString()

            val updatedNote = Note(noteId, newTitle, newContent)
            db.updateNote(updatedNote)
            finish()
            Toast.makeText(this, "Changes Saved", Toast.LENGTH_SHORT).show()
        }
    }
}
