package com.example.wavey.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wavey.R
import com.example.wavey.databinding.ItemSectionHeaderBinding
import com.example.wavey.databinding.ItemTaskBinding
import com.example.wavey.model.Task
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SectionedTaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onTaskCompletionToggle: (Task, Boolean) -> Unit,
    private val onSectionToggle: (Boolean, Boolean) -> Unit
) : ListAdapter<SectionedTaskAdapter.Item, RecyclerView.ViewHolder>(TaskDiffCallback()) {

    // Track expanded/collapsed state
    private var isTodoSectionExpanded = true
    private var isCompletedSectionExpanded = true

    // Item types
    sealed class Item {
        data class SectionHeader(val title: String, val isCompleted: Boolean, val count: Int) : Item()
        data class TaskItem(val task: Task) : Item()
    }

    // ViewHolder types
    class SectionHeaderViewHolder(private val binding: ItemSectionHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(header: Item.SectionHeader, isExpanded: Boolean, onSectionToggle: (Boolean, Boolean) -> Unit) {

            binding.tvSectionTitle.text = "${header.title} (${header.count})"

            // Set different colors for "To Do" and "Completed" sections
            val colorRes = if (header.isCompleted) R.color.gray else R.color.main_orange
            val textColorRes = if (header.isCompleted) R.color.black else R.color.white

            binding.sectionHeader.setCardBackgroundColor(
                ContextCompat.getColor(binding.sectionHeader.context, colorRes)
            )
            binding.tvSectionTitle.setTextColor(
                ContextCompat.getColor(binding.tvSectionTitle.context, textColorRes)
            )
            binding.ivToggleArrow.setColorFilter(
                ContextCompat.getColor(binding.ivToggleArrow.context, textColorRes)
            )

            fun dpToPx(dp: Float, context: Context): Float {
                return dp * context.resources.displayMetrics.density
            }

            binding.sectionHeader.radius = dpToPx(16f, binding.sectionHeader.context)


            // Set expanded/collapsed arrow
            binding.ivToggleArrow.rotation = if (isExpanded) 0f else -90f

            // Set click listener for expanding/collapsing
            binding.sectionHeaderContainer.setOnClickListener {
                onSectionToggle(header.isCompleted, !isExpanded)
            }
        }
    }

    class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task, onTaskClick: (Task) -> Unit, onTaskCompletionToggle: (Task, Boolean) -> Unit) {
            binding.tvTaskTitle.text = task.title
            binding.tvTaskDetails.text = task.details
            binding.cbTaskCompleted.isChecked = task.completed

            // Format creation time
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val formatedCreatedDate = sdf.format(Date(task.timestamp.seconds * 1000))

            binding.tvTaskDate.text = binding.tvTaskDate.context.getString(R.string.created_date, formatedCreatedDate)
            binding.tvTaskDate.visibility = View.VISIBLE

            if (task.dueDate != null) {
                val formatedDueDate = sdf.format(Date(task.dueDate.seconds * 1000))
                binding.tvTaskDueDate.text = binding.tvTaskDueDate.context.getString(R.string.due_date, formatedDueDate)
                binding.tvTaskDueDate.visibility = View.VISIBLE
            }

            // Set checkbox listener
            binding.cbTaskCompleted.setOnCheckedChangeListener { _, isChecked ->

                if (isChecked != task.completed) {
                    onTaskCompletionToggle(task, isChecked)

                }
            }

            // Set task click listener
            binding.root.setOnClickListener {
                onTaskClick(task)
            }

            // Clear the chip group and add tags with colors
            binding.chipGroupTags.removeAllViews()

            for ((index, tag) in task.tags.withIndex()) {
                val chip = Chip(binding.chipGroupTags.context)
                chip.text = tag
                chip.isCheckable = false
                chip.isClickable = false

                // Apply color based on tag index (cycling through available colors)
                val colorId = when (chip.text) {
                    "work" -> R.color.very_light_yellow
                    "school" -> R.color.very_light_green
                    "errands" -> R.color.very_light_blue
                    "shopping" -> R.color.very_light_purple
                    "finance" -> R.color.very_light_pink
                    else -> R.color.very_light_red
                }

                chip.chipBackgroundColor = chip.context.getColorStateList(colorId)
                chip.setTextColor(chip.context.getColor(R.color.black))
                binding.chipGroupTags.addView(chip)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Item.SectionHeader -> VIEW_TYPE_HEADER
            is Item.TaskItem -> VIEW_TYPE_TASK
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemSectionHeaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                SectionHeaderViewHolder(binding)
            }
            VIEW_TYPE_TASK -> {
                val binding = ItemTaskBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                TaskViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Item.SectionHeader -> (holder as SectionHeaderViewHolder).bind(
                item,
                if (item.isCompleted) isCompletedSectionExpanded else isTodoSectionExpanded,
                onSectionToggle
            )
            is Item.TaskItem -> (holder as TaskViewHolder).bind(
                item.task,
                onTaskClick,
                onTaskCompletionToggle
            )
        }
    }

    fun updateSections(todoTasks: List<Task>, completedTasks: List<Task>) {
        val items = mutableListOf<Item>()

        // Add Todo section header
        items.add(Item.SectionHeader("To Do", false, todoTasks.size))

        // Add Todo tasks if expanded
        if (isTodoSectionExpanded) {
            todoTasks.forEach { task ->
                items.add(Item.TaskItem(task))
            }
        }

        // Add Completed section header
        items.add(Item.SectionHeader("Completed", true, completedTasks.size))

        // Add Completed tasks if expanded
        if (isCompletedSectionExpanded) {
            completedTasks.forEach { task ->
                items.add(Item.TaskItem(task))
            }
        }

        submitList(items)
    }

    fun toggleSection(isCompletedSection: Boolean, isExpanded: Boolean) {
        if (isCompletedSection) {
            isCompletedSectionExpanded = isExpanded
        } else {
            isTodoSectionExpanded = isExpanded
        }

        notifyDataSetChanged()  // We'll need to recalculate the list
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_TASK = 1
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return when {
                oldItem is Item.SectionHeader && newItem is Item.SectionHeader ->
                    oldItem.title == newItem.title
                oldItem is Item.TaskItem && newItem is Item.TaskItem ->
                    oldItem.task.id == newItem.task.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return when {
                oldItem is Item.SectionHeader && newItem is Item.SectionHeader ->
                    oldItem == newItem
                oldItem is Item.TaskItem && newItem is Item.TaskItem ->
                    oldItem.task == newItem.task
                else -> false
            }
        }
    }
}