package com.example.todo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.todo.databinding.FragmentAddTodoBinding
import com.example.todo.utils.TodoData
import com.google.android.material.textfield.TextInputEditText

class AddTodoFragment : DialogFragment() {

    private lateinit var binding: FragmentAddTodoBinding
    private lateinit var listener: DialogNextButtonClickListener
    var todoData: TodoData? = null

    fun setListener(listener: DialogNextButtonClickListener) {
        this.listener = listener
    }

    companion object {
        const val TAG = "AddTodoFragment"

        @JvmStatic
        fun newInstance(taskId: String, task: String) = AddTodoFragment().apply {
            arguments = Bundle().apply {
                putString("taskId", taskId)
                putString("task", task)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddTodoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null) {
            todoData = TodoData(
                arguments?.getString("taskId").toString(),
                arguments?.getString("task").toString()
            )
            binding.etTask.setText(todoData?.task)
        }
        registerEvents()
    }


    private fun registerEvents() {
        binding.todoNextButton.setOnClickListener {
            val todoTask = binding.etTask.text.toString()
            if (todoTask.isNotEmpty()) {
                if(todoData == null){
                    listener.onSaveTask(todoTask, binding.etTask)
                }else{
                    todoData?.task = todoTask
                    listener.onUpdateTask(todoData!!,binding.etTask)
                }
            } else {
                Toast.makeText(context, "Please type some task", Toast.LENGTH_SHORT).show()
            }
        }
        binding.closeButton.setOnClickListener {
            dismiss()
        }
    }

    interface DialogNextButtonClickListener {
        fun onSaveTask(todo: String, todoEt: TextInputEditText)
        fun onUpdateTask(todoData: TodoData, todoEt : TextInputEditText)
    }

}