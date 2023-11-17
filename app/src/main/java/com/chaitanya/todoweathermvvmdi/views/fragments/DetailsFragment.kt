package com.chaitanya.todomvvmdi.views.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.chaitanya.todoweathermvvmdi.data.database.entity.NoteEntity
import com.chaitanya.todoweathermvvmdi.databinding.FragmentDetailsBinding
import com.chaitanya.todoweathermvvmdi.utils.DetailState
import com.chaitanya.todoweathermvvmdi.viewModel.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailsFragment : Fragment() {
    private lateinit var binding: FragmentDetailsBinding

    val viewModel : NoteViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentBinding = FragmentDetailsBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        (activity as AppCompatActivity).setSupportActionBar(binding.detailToolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.title = ""
        binding.detailToolbar.setNavigationOnClickListener { findNavController().popBackStack()}
        return fragmentBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Button to Edit
        binding.ivEditButton.setOnClickListener {
            viewModel.setDetailState(DetailState.Update)
        }

        // Initialize the view and UI based on the detail state
        viewModel.detailState.observe(viewLifecycleOwner) {
            Log.e("fadsf", it.name)
            if (it != null){
                when(it){
                    DetailState.Update ->{
                        viewModel.detailNote.observe(viewLifecycleOwner){note->
                            binding.apply {
                                checkbox.isEnabled = true
                                etTitle.isEnabled = true
                                etDescription.isEnabled = true
                                etTitle.setText(note.title)
                                etDescription.setText(note.description)
                                AddButton.visibility = View.VISIBLE
                                AddButton.text = "UPDATE"
                                checkbox.isChecked = note.isPriority
                                ivEditButton.visibility = View.INVISIBLE
                            }
                            binding.AddButton.setOnClickListener {
                                if (binding.etTitle.text.isNullOrEmpty()){
                                    Toast.makeText(requireContext(),"Enter Title",Toast.LENGTH_SHORT).show()

                                }else{
                                    viewModel.updateNote(NoteEntity(id = note.id,title = binding.etTitle.text.toString(), description = binding.etDescription.text.toString(), isPriority = binding.checkbox.isChecked, isCompleted = note.isCompleted))
                                    findNavController().popBackStack()
                                }
                            }
                        }
                    }
                    DetailState.Insert->{
                        binding.apply {
                            checkbox.isEnabled = true
                            etTitle.isEnabled = true
                            etDescription.isEnabled = true
                            etTitle.setText("")
                            etDescription.setText("")
                            AddButton.text = "ADD NOTE"
                        }

                        binding.AddButton.setOnClickListener {
                            if (binding.etTitle.text.isNullOrEmpty()){
                                Toast.makeText(requireContext(),"Enter Title",Toast.LENGTH_SHORT).show()
                            }else{
                                viewModel.insertNote(NoteEntity(title = binding.etTitle.text.toString(), description = binding.etDescription.text.toString(), isPriority = binding.checkbox.isChecked))
                                findNavController().popBackStack()
                            }
                        }
                    }
                    DetailState.Show -> {
                        viewModel.detailNote.observe(viewLifecycleOwner){note->
                            binding.apply {
                                etTitle.setText(note.title)
                                etDescription.setText(note.description)
                                AddButton.visibility = View.GONE
                                etTitle.isEnabled = false
                                etDescription.isEnabled = false
                                checkbox.isChecked = note.isPriority
                                checkbox.isEnabled = false
                                etTitle.setTextColor(Color.BLACK)
                                etDescription.setTextColor(Color.BLACK)
                                ivEditButton.visibility = View.VISIBLE
                            }

                        }
                    }

                }
            }

        }
    }

}