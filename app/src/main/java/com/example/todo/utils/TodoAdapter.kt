package com.example.todo.utils

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.databinding.ListItemBinding

class TodoAdapter(private val list : MutableList<TodoData>) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    private var listener : TodoAdapterClickInterface? = null
    fun setListener(listener : TodoAdapterClickInterface){
        this.listener = listener
    }

    class TodoViewHolder(binding : ListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val deleteButton : ImageView = binding.deleteImage
        val taskText : TextView = binding.TaskView
        val taskCheckBox : CheckBox = binding.checkBox
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodoViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        with(holder){
            with(list[position]){
                taskText.text = this.task
                listener?.onCheckChange(this.checked, taskText)
                deleteButton.setOnClickListener {
                    listener?.onDeleteTaskButtonClicked(this)
                }
                taskText.setOnClickListener {
                    listener?.onEditTaskButtonClicked(this)
                }
                taskCheckBox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
                    listener?.onCheckChange(isChecked, taskText)
                })
            }
        }
    }

    interface TodoAdapterClickInterface {
        fun onDeleteTaskButtonClicked(todoData: TodoData)
        fun onEditTaskButtonClicked(todoData: TodoData)
        fun onCheckChange(isChecked : Boolean, taskTextView: TextView)
    }
}