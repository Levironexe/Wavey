package com.example.wavey.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wavey.R
import com.example.wavey.databinding.ItemCategoryBinding
import com.example.wavey.model.Category

class CategoryAdapter(private val onCategoryClick: (Category) -> Unit) :
    ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCategoryClick(getItem(position))
                }
            }
        }

        fun bind(category: Category) {
            binding.categoryName.text = category.name
            binding.categoryIcon.text = category.taskCount.toString()
            binding.taskCount.text = "Tasks"



            // Set background color based on category
            val bgColorResId = when (category.tag) {
                "work" -> R.color.very_light_yellow
                "school" -> R.color.very_light_green
                "errands" -> R.color.very_light_blue
                "shopping" -> R.color.very_light_purple
                "finance" -> R.color.very_light_pink
                else -> R.color.very_light_red
            }
            val categoryImageRes = when (category.tag) {
                "work" -> R.drawable.ic_briefcase
                "school" -> R.drawable.ic_fire_smaller
                "errands" -> R.drawable.ic_errands_image
                "shopping" -> R.drawable.ic_credit_card
                "finance" -> R.drawable.ic_piggy_bank
                else -> R.drawable.ic_whistle
            }

            binding.categoryIcon.backgroundTintList = binding.root.context.getColorStateList(bgColorResId)
            binding.categoryImage.setImageResource(categoryImageRes)
        }
    }

    private class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.tag == newItem.tag
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }
}