package com.example.todo.fragments

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo.R
import com.example.todo.databinding.FragmentHomeBinding
import com.example.todo.utils.TodoAdapter
import com.example.todo.utils.TodoData
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class HomeFragment : Fragment(), AddTodoFragment.DialogNextButtonClickListener,
    TodoAdapter.TodoAdapterClickInterface, LogOutPopupFragment.LogoutInterface {

    private lateinit var auth : FirebaseAuth
    private lateinit var binding : FragmentHomeBinding
    private lateinit var navControl : NavController
    private lateinit var databaseRef : DatabaseReference
    private var popUpFragment : AddTodoFragment? = null
    private lateinit var adapter : TodoAdapter
    private lateinit var mList : MutableList<TodoData>
    private lateinit var logoutPopup : LogOutPopupFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        getDataFromFirebase()
        registerEvents()
    }

    private fun getDataFromFirebase() {
        databaseRef.addValueEventListener(object : ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                for(taskSnapshot in snapshot.children) {
                    val todoTask = taskSnapshot.key?.let {
                        TodoData(it, taskSnapshot.value.toString())
                    }
                    if(todoTask != null){
                        mList.add(todoTask)
                    }
                }
                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun registerEvents() {
        binding.addHomeButton.setOnClickListener{
            if(popUpFragment != null)
                childFragmentManager.beginTransaction().remove(popUpFragment!!).commit()
            popUpFragment = AddTodoFragment()
            popUpFragment!!.setListener(this)
            popUpFragment!!.show(
                childFragmentManager,
                AddTodoFragment.TAG
            )
        }
        binding.backButton.setOnClickListener {
            logoutPopup = LogOutPopupFragment()
            logoutPopup.setLogoutListener(this)
            logoutPopup.show(
                childFragmentManager,
                "LogoutPopup"
            )
        }
    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        navControl = Navigation.findNavController(view)
        databaseRef = FirebaseDatabase.getInstance().reference.child("Tasks").child(auth.currentUser?.uid.toString())
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        mList = mutableListOf()
        adapter = TodoAdapter(mList)
        adapter.setListener(this)
        binding.recyclerView.adapter = adapter
    }

    override fun onSaveTask(todo: String, todoEt: TextInputEditText) {
        databaseRef.push().setValue(todo).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Task saved successfully", Toast.LENGTH_SHORT).show()
                todoEt.text?.clear()
            } else {
                Toast.makeText(context, it.exception?.message.toString(), Toast.LENGTH_SHORT).show()
            }
            popUpFragment!!.dismiss()
        }
    }

    override fun onUpdateTask(todoData: TodoData, todoEt: TextInputEditText) {
        val map = HashMap<String, Any>()
        map[todoData.taskId] = todoData.task
        databaseRef.updateChildren(map).addOnCompleteListener {
            if(it.isSuccessful) {
                Toast.makeText(context, "Task Updated", Toast.LENGTH_SHORT).show()

            }else{
                Toast.makeText(context, it.exception?.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        todoEt.text?.clear()
        popUpFragment?.dismiss()
    }

    override fun onDeleteTaskButtonClicked(todoData: TodoData) {
        databaseRef.child(todoData.taskId).removeValue().addOnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context, it.exception?.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onEditTaskButtonClicked(todoData: TodoData) {
        if(popUpFragment != null)
            childFragmentManager.beginTransaction().remove(popUpFragment!!).commit()

        popUpFragment = AddTodoFragment.newInstance(todoData.taskId, todoData.task)
        popUpFragment!!.setListener(this)
        popUpFragment!!.show(childFragmentManager,AddTodoFragment.TAG)
    }

    override fun onCheckChange(isChecked: Boolean, taskTextView: TextView) {
        if(isChecked){
            taskTextView.paintFlags = taskTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }else{
            taskTextView.paintFlags = taskTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    override fun logout() {
        logoutPopup.dismiss()
        auth.signOut()
        navControl.navigate(R.id.action_homeFragment_to_signInFragment)
    }
}
