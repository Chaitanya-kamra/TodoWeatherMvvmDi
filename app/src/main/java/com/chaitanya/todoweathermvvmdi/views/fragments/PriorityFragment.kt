package com.chaitanya.todomvvmdi.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.chaitanya.todoweathermvvmdi.data.database.entity.NoteEntity
import com.chaitanya.todoweathermvvmdi.utils.DetailState
import com.chaitanya.todoweathermvvmdi.viewModel.NoteViewModel
import com.chaitanya.todomvvmdi.views.adapter.NoteAdapter
import com.chaitanya.todoweathermvvmdi.R
import com.chaitanya.todoweathermvvmdi.databinding.FragmentPriorityBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PriorityFragment : Fragment() {
    private lateinit var binding: FragmentPriorityBinding
    private lateinit var adapter: NoteAdapter
    val viewModel: NoteViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentBinding = FragmentPriorityBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = NoteAdapter(
            emptyList(),
            { note ->
            viewModel.setDetailState(DetailState.Update)
            viewModel.setDetail(note)
            findNavController().navigate(R.id.action_priorityFragment_to_detailsFragment)
        },
            { deletenote ->

            viewModel.updateNote(
                NoteEntity(
                    id = deletenote.id,
                    title = deletenote.title,
                    description = deletenote.description,
                    isPriority = false,
                    isCompleted = deletenote.isCompleted
                )
            )
            Toast.makeText(requireContext(), "Removed from priority", Toast.LENGTH_SHORT).show()
        },
            { showNote ->
            viewModel.setDetailState(DetailState.Show)
            viewModel.setDetail(showNote)
            findNavController().navigate(R.id.action_priorityFragment_to_detailsFragment)
        },
            { note, isChecked ->
            viewModel.updateNote(
                NoteEntity(
                    id = note.id,
                    title = note.title,
                    description = note.description,
                    isCompleted = isChecked,
                    isPriority = note.isPriority
                )
            )
        },
            { card, note ->

            }
        )
        binding.priorityRv.adapter = adapter

        viewModel.priorityNotes.observe(viewLifecycleOwner) {
            if (it != null) {
                adapter.updateItems(it)
            }
        }
    }

}