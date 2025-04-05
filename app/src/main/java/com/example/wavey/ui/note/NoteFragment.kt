package com.example.wavey.ui.note

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
import com.example.wavey.adapter.NoteAdapter
import com.example.wavey.databinding.FragmentNoteBinding
import com.example.wavey.model.Note
import com.example.wavey.ui.NoteItemActivity
import com.example.wavey.ui.tasks.NoteViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NoteFragment : Fragment() {

    private var _binding: FragmentNoteBinding? = null
    private val binding get() = _binding!!

    // Share ViewModel with MainActivity
    private val viewModel: NoteViewModel by activityViewModels()

    private lateinit var noteAdapter: NoteAdapter

    // Activity result launcher for editing notes
    private val editNoteLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Refresh the note list
            viewModel.loadNotes()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()

        // Load notes when fragment is created
        viewModel.loadNotes()

        // Setup pull-to-refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadNotes()
        }
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter(
            onNoteClick = { note ->
                val intent = Intent(requireContext(), NoteItemActivity::class.java).apply {
                    putExtra("NOTE_ID", note.id)
                    putExtra("NOTE_TITLE", note.title)
                    putExtra("NOTE_DETAILS", note.details)
                    putExtra("NOTE_TIMESTAMP", note.creationDate.seconds * 1000)
                    putExtra("USER_ID", note.userId)
                }
                editNoteLauncher.launch(intent)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = noteAdapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe notes
                launch {
                    viewModel.notes.collectLatest { notes ->
                        updateNoteList(notes)
                        // Update empty state visibility
                        binding.emptyStateLayout.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
                    }
                }

                // Observe loading state
                launch {
                    viewModel.isLoading.collectLatest { isLoading ->
                        binding.swipeRefreshLayout.isRefreshing = isLoading
                        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
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

    private fun updateNoteList(notes: List<Note>) {
        noteAdapter.updateNotes(notes)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}