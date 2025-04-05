package com.example.wavey.ui.task

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wavey.ui.TaskItemActivity
import com.example.wavey.adapter.SectionedTaskAdapter
import com.example.wavey.databinding.FragmentTasksBinding
import com.example.wavey.model.Task
import com.example.wavey.ui.tasks.TasksViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    // Share ViewModel with MainActivity
    private val viewModel: TasksViewModel by activityViewModels()

    private lateinit var sectionedTaskAdapter: SectionedTaskAdapter

    // In TasksFragment class
    private val editTaskLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Refresh the task list
            viewModel.loadTasks()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()

        // Load tasks when fragment is created
        viewModel.loadTasks()

        // Setup pull-to-refresh if available
        binding.swipeRefreshLayout?.setOnRefreshListener {
            viewModel.loadTasks()
        }
    }

    private fun setupRecyclerView() {
        sectionedTaskAdapter = SectionedTaskAdapter(
            onTaskClick = { task ->
                val intent = Intent(requireContext(), TaskItemActivity::class.java).apply {
                    putExtra("TASK_ID", task.id)
                    putExtra("TASK_TITLE", task.title)
                    putExtra("TASK_DETAILS", task.details)
                    putExtra("TASK_IS_COMPLETED", task.completed)
                    putExtra("TASK_TIMESTAMP", task.timestamp.seconds * 1000)
                    putExtra("DUE_DATE", (task.dueDate?.seconds?: 0) * 1000)
                    putExtra("USER_ID", task.userId)
                    val tagsArrayList = ArrayList<String>(task.tags)
                    putStringArrayListExtra("TASK_TAGS", tagsArrayList)
                }
                editTaskLauncher.launch(intent)
                          },
            onTaskCompletionToggle = { task, isCompleted ->
                viewModel.toggleTaskCompletion(task.id, isCompleted)
            },
            onSectionToggle = { isCompletedSection, isExpanded ->
                sectionedTaskAdapter.toggleSection(isCompletedSection, isExpanded)
                updateTaskList(viewModel.tasks.value)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sectionedTaskAdapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe tasks
                launch {
                    viewModel.tasks.collectLatest { tasks ->
                        updateTaskList(tasks)
                        // Update empty state visibility
                        binding.emptyStateLayout?.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
                    }
                }

                // Observe loading state
                launch {
                    viewModel.isLoading.collectLatest { isLoading ->
                        binding.swipeRefreshLayout?.isRefreshing = isLoading
                    }
                }

                // Observe errors
                launch {
                    viewModel.error.collectLatest { errorMsg ->
                        errorMsg?.let {
                            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                            viewModel.clearError()
                        }
                    }
                }
            }
        }
    }

    private fun updateTaskList(tasks: List<Task>) {
        println("Total tasks: ${tasks.size}")
        // Split tasks into todo and completed tasks
        val todoTasks = tasks.filter { !it.completed }
        val completedTasks = tasks.filter { it.completed }


        print("Todo tasks: ${todoTasks.size}")
        print("Completed tasks: ${completedTasks.size}\"")

        // Update the adapter with the sectioned data
        sectionedTaskAdapter.updateSections(todoTasks, completedTasks)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}