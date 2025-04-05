package com.example.wavey.repository

import com.example.wavey.model.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date

class TaskRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val tasksCollection = firestore.collection("tasks")

    // Get current user ID
    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    // Add a new task to Firestore
    suspend fun addTask(task: Task): Result<String> {
        return try {
            val databaseName = firestore.app.options.projectId
            println("Firestore Database Name: $databaseName")

            // Create a new task with the current user ID
            val taskWithUser = task.copy(userId = currentUserId)
            println("Data being stored: $taskWithUser")

            // Add to Firestore and get the document reference
            val documentRef = tasksCollection.add(taskWithUser).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get all tasks for the current user
    suspend fun getTasks(): Result<List<Task>> {
        return try {
            val snapshot = tasksCollection
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val tasks = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Task::class.java)?.copy(id = doc.id)
            }
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUpComingTasks(): Result<List<Task>> {
        return try {
            val now = Timestamp.now()

            val threeDaysFromNow = Timestamp(
                now.seconds + (3 * 24 * 60 * 60), // 3 days in seconds
                0
            )
            println("Debug - now: $now")
            println("Debug - threeDaysFromNow: $threeDaysFromNow")


            val snapshot = tasksCollection
                .whereEqualTo("userId", currentUserId)
                .whereGreaterThanOrEqualTo("dueDate", now) // Due date is after or equal to now
                .whereLessThanOrEqualTo("dueDate", threeDaysFromNow) // Due date is before or equal to 3 days from now
                .whereEqualTo("completed", false)
                .get()
                .await()

            val tasks = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Task::class.java)?.copy(id = doc.id)
            }
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update an existing task
    suspend fun updateTask(task: Task, taskId: String): Result<Unit> {
        return try {
            val updateData = mapOf(
                "title" to task.title,
                "details" to task.details,
            )

            tasksCollection.document(taskId).update(updateData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete a task
    suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            tasksCollection.document(taskId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Toggle task completion status
    suspend fun toggleTaskCompletion(taskId: String, isCompleted: Boolean): Result<Unit> {
        return try {
            tasksCollection.document(taskId)
                .update("completed", isCompleted)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}