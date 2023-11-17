package com.chaitanya.todomvvmdi.views.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.chaitanya.todoweathermvvmdi.data.database.entity.NoteEntity
import com.chaitanya.todoweathermvvmdi.databinding.ItemNoteBinding
import com.google.android.material.card.MaterialCardView

class NoteAdapter(
    private val items: List<NoteEntity>,
    private val updateListener: (note: NoteEntity) -> Unit,
    private val deleteListener: (note: NoteEntity) -> Unit,
    private val showListener: (note: NoteEntity) -> Unit,
    private val completeListener: (note: NoteEntity, isChecked: Boolean) -> Unit,
    private val showMenu: (card: MaterialCardView, note: NoteEntity) -> Unit

) : RecyclerView.Adapter<NoteAdapter.ViewHolder>() {

    var noteList = items

    class ViewHolder(binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.tvTitle
        val ivEdit = binding.ivEdit
        val ivDelete = binding.ivDelete
        val checkBox = binding.cbComplete
        var card = binding.llMain
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemNoteBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val note = noteList[position]

        if (note.isCompleted) {
            holder.title.paintFlags = holder.title.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.title.text = note.title
            holder.ivEdit.visibility = View.INVISIBLE
        } else {
            holder.title.paintFlags = 0
            holder.title.text = note.title
            holder.ivEdit.visibility = View.VISIBLE
        }

        holder.checkBox.isChecked = note.isCompleted
        holder.checkBox.setOnClickListener {
            val value = (it as CheckBox).isChecked
            completeListener(note, value)
        }

        // Handle long click
        holder.itemView.setOnLongClickListener {
            showMenu(holder.card, note)
            true
        }

        // Handle click to show,edit and delete
        holder.ivEdit.setOnClickListener {
            updateListener(note)
        }
        holder.itemView.setOnClickListener {
            showListener(note)
        }
        holder.ivDelete.setOnClickListener {
            deleteListener(note)
        }
    }

    fun updateItems(newItems: List<NoteEntity>) {
        if (newItems != noteList) {
            noteList = newItems
            notifyDataSetChanged()
        }
    }
}