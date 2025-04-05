package com.example.wavey.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wavey.databinding.FragmentHomeBinding
import com.example.wavey.model.Category
import com.example.wavey.model.Note
import com.example.wavey.model.Task
import com.example.wavey.ui.tasks.NoteViewModel
import com.example.wavey.ui.tasks.TasksViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Share ViewModel with MainActivity and TasksFragment
    private val taskViewModel: TasksViewModel by activityViewModels()
    private val noteViewModel: NoteViewModel by activityViewModels()

    // Adapter for categories
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoriesRecyclerView()
        setupObservers()

        // Load tasks and notes when fragments are created
        taskViewModel.loadTasks()
        noteViewModel.loadNotes()
        taskViewModel.loadUpcomingTasks()
    }

    private fun setupCategoriesRecyclerView() {
        categoryAdapter = CategoryAdapter { category ->
            // Handle category click if needed
        }

        binding.categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe tasks
                launch {

                    taskViewModel.tasks.collectLatest { tasks ->
                        print("updateDashboardStats is called")

                        // Update dashboard stats
                        updateDashboardStats(tasks)

                        // Update categories with task counts
                        updateCategories(tasks)
                    }
                }

                //Observe upcoming tasks
                launch {
                    taskViewModel.upcomingTasks.collectLatest { upcomingTasks ->
                        print("updateIncomingTasks is called")

                        updateIncomingTasks(upcomingTasks)
                    }
                }

                // Observe notes
                launch {
                    noteViewModel.notes.collectLatest { notes ->
                        // Update dashboard stats
                        updateDashboardNoteCount(notes)
                    }
                }


            }
        }
    }

    private fun updateIncomingTasks(upcomingTasks: List<Task>) {
        val tasks = upcomingTasks.size
        println("upcomingTasks $tasks")
        binding.tvUpcomingTasks.text = tasks.toString()
    }

    private fun updateDashboardStats(tasks: List<Task>) {
        val totalTasks = tasks.size
        val completedTasks = tasks.count { it.completed }


        val completionPercentage = if (totalTasks > 0) {
            (completedTasks.toFloat() / totalTasks) * 100
        } else {
            0f
        } // Avoid dividing 0 tasks

        // Update UI
        binding.tvCompletionPercentage.text = "%.0f%%".format(completionPercentage)
        // Only update upcoming tasks here if the specific observer hasn't fired yet

        binding.notCompleteCountTextView.text = (totalTasks - completedTasks).toString()
    }

    private fun updateDashboardNoteCount(notes: List<Note>) {
        val totalNote = notes.size
        binding.totalNotesCountTextView.text = totalNote.toString()
    }

    private fun updateCategories(tasks: List<Task>) {
        // Define your 6 categories - this is an example, adjust as needed
        val categories = listOf(
            Category("Work", "work", getTaskCountByTag(tasks, "work")),
            Category("School", "school", getTaskCountByTag(tasks, "school")),
            Category("Errands", "errands", getTaskCountByTag(tasks, "errands")),
            Category("Shopping", "shopping", getTaskCountByTag(tasks, "shopping")),
            Category("Finance", "finance", getTaskCountByTag(tasks, "finance")),
            Category("Fitness", "fitness", getTaskCountByTag(tasks, "fitness"))
        )

        categoryAdapter.submitList(categories)
    }

    private fun getTaskCountByTag(tasks: List<Task>, tag: String): Int {
        return tasks.count { task -> task.tags.contains(tag) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}