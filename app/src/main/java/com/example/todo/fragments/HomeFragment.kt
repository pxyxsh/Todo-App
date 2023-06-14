package com.example.todo.fragments

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.R
import com.example.todo.databinding.FragmentHomeBinding
import com.example.todo.utils.TodoAdapter
import com.example.todo.utils.TodoData
import com.example.todo.utils.TodoState
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import java.util.*


class HomeFragment : Fragment(), AddTodoFragment.DialogNextButtonClickListener,
    TodoAdapter.TodoAdapterClickInterface, LogOutPopupFragment.LogoutInterface {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentHomeBinding
    private lateinit var navControl: NavController
    private lateinit var databaseRef: DatabaseReference
    private var popUpFragment: AddTodoFragment? = null
    private lateinit var adapter: TodoAdapter
    private lateinit var mList: MutableList<TodoData>
    private lateinit var logoutPopup: LogOutPopupFragment


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
        rearrangeRecyclerView()
    }

    private fun rearrangeRecyclerView() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                source: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val sourcePosition = source.absoluteAdapterPosition
                val targetPosition = target.absoluteAdapterPosition
                Collections.swap(mList, sourcePosition, targetPosition)
                adapter.notifyItemMoved(sourcePosition, targetPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onDeleteTaskButtonClicked(mList[viewHolder.absoluteAdapterPosition])
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                    .addBackgroundColor(ContextCompat.getColor(context!!, R.color.red))
                    .addActionIcon(R.drawable.baseline_delete_24)
                    .addSwipeLeftCornerRadius(16,16F)
                    .addCornerRadius(1,16)
                    .create()
                    .decorate()
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun getDataFromFirebase() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("Home14", "onDataChange: start")
                mList.clear()
                for (taskSnapshot in snapshot.children) {
                    if (taskSnapshot.child("task").value.toString().isNotEmpty()) {
                        val todoTask = taskSnapshot.key?.let {
                            TodoData(
                                it,
                                taskSnapshot.child("task").value.toString(),
                                taskSnapshot.child("checked").value.toString().toBoolean()
                            )
                        }
                        if (todoTask != null) {
                            mList.add(todoTask)
                        }
                    }
                    Log.d("Home14", "onDataChange: success")
                }
                Log.d("Home14", "onDataChange: end")

                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                adapter.notifyDataSetChanged()
                if(mList.isEmpty()){
                    binding.recyclerView.visibility = View.GONE
                    binding.tvNoTask.visibility = View.VISIBLE
                }else{
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.tvNoTask.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun registerEvents() {
        binding.addHomeButton.setOnClickListener {
            if (popUpFragment != null)
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

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            val addButton = binding.addHomeButton
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(!binding.recyclerView.canScrollVertically(-1)){
                    addButton.show()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if(dy > 10 && addButton.isShown){
                    addButton.hide()
                }
                else if(dy < -10 && !addButton.isShown){
                    addButton.show()
                }
            }
        })
    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        navControl = Navigation.findNavController(view)
        databaseRef = FirebaseDatabase.getInstance().reference.child("Tasks")
            .child(auth.currentUser?.uid.toString())
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        mList = mutableListOf()
        adapter = TodoAdapter(mList)
        adapter.setListener(this)
        binding.recyclerView.adapter = adapter
    }

    override fun onSaveTask(todo: String, todoEt: TextInputEditText) {
        val task = TodoState(todo, false)
        databaseRef.push().setValue(task).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("Home14", "onSaveTask: success")
                //Toast.makeText(context, "Task saved successfully", Toast.LENGTH_SHORT).show()
                todoEt.text?.clear()
            } else {
                Log.d("Home14", "onSaveTask: fail")
                Toast.makeText(context, it.exception?.message.toString(), Toast.LENGTH_SHORT).show()
            }
            popUpFragment!!.dismiss()
        }
        Log.d("Home14", "onSaveTask: complete")
    }

    override fun onUpdateTask(todoData: TodoData, todoEt: TextInputEditText) {
        val map = HashMap<String, Any>()
        val todoState = TodoState(todoData.task, false)
        map[todoData.taskId] = todoState
        databaseRef.updateChildren(map).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("Home14", "onUpdateTask: success")
                //Toast.makeText(context, "Task Updated", Toast.LENGTH_SHORT).show()

            } else {
                Log.d("Home14", "onUpdateTask: fail")
                Toast.makeText(context, it.exception?.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        todoEt.text?.clear()
        popUpFragment?.dismiss()
        Log.d("Home14", "onUpdateTask: complete")
    }

    override fun onDeleteTaskButtonClicked(todoData: TodoData) {
        databaseRef.child(todoData.taskId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("Home14", "onDeleteTaskButtonClicked: success")
                //Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("Home14", "onDeleteTaskButtonClicked: fail")
                Toast.makeText(context, it.exception?.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        Log.d("Home14", "onDeleteTaskButtonClicked: complete")
    }

    override fun onEditTaskButtonClicked(todoData: TodoData) {
        if (popUpFragment != null)
            childFragmentManager.beginTransaction().remove(popUpFragment!!).commit()

        popUpFragment = AddTodoFragment.newInstance(todoData.taskId, todoData.task)
        popUpFragment!!.setListener(this)
        popUpFragment!!.show(childFragmentManager, AddTodoFragment.TAG)
    }

    override fun onCheckChange(todoData: TodoData, isChecked: Boolean) {
        databaseRef.child(todoData.taskId).child("checked").setValue(isChecked)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("Home14", "onCheckChange: success")
                } else {
                    Log.d("Home14", "onCheckChange: fail")
                    Toast.makeText(context, it.exception?.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        Log.d("Home14", "onCheckChange: complete")
    }

    override fun logout() {
        logoutPopup.dismiss()
        auth.signOut()
        navControl.navigate(R.id.action_homeFragment_to_signInFragment)
    }
}
