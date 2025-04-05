    package com.example.wavey.repository

    import com.example.wavey.model.Note
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.database.DatabaseReference
    import com.google.firebase.firestore.FirebaseFirestore
    import com.google.firebase.firestore.Query
    import kotlinx.coroutines.tasks.await

    class NoteRepository {
        private val firestore = FirebaseFirestore.getInstance()
        private val auth = FirebaseAuth.getInstance()
        private val notesCollection = firestore.collection("notes")

        // Get current user ID
        private val currentUserId: String
            get() = auth.currentUser?.uid ?: ""

        // Add a new task to Firestore
        suspend fun addNote(note: Note): Result<String> {
            return try {
                val databaseName = firestore.app.options.projectId
                println("Firestore Database Name: $databaseName")

                // Create a new task with the current user ID
                val noteWithUser = note.copy(userId = currentUserId)
                println("Data being stored: $noteWithUser")

                // Add to Firestore and get the document reference
                val documentRef = notesCollection.add(noteWithUser).await()
                Result.success(documentRef.id)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        // Get all tasks for the current user
        suspend fun getNotes(): Result<List<Note>> {
            return try {
                val snapshot = notesCollection
                    .whereEqualTo("userId", currentUserId)
                    .get()
                    .await()

                val notes = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Note::class.java)?.copy(id = doc.id)
                }
                Result.success(notes)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        // Update an existing task
        suspend fun updateNote(note: Note, noteId: String): Result<Unit> {
            return try {
                val updateData = mapOf(
                    "title" to note.title,
                    "details" to note.details,
                )
                notesCollection.document(noteId).update(updateData).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        // Delete a task
        suspend fun deleteNote(noteId: String): Result<Unit> {
            return try {
                notesCollection.document(noteId).delete().await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }