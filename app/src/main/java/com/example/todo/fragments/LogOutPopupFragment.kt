package com.example.todo.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.todo.R
import com.example.todo.databinding.FragmentLogOutPopupBinding


class LogOutPopupFragment : DialogFragment() {

    private lateinit var binding : FragmentLogOutPopupBinding
    private lateinit var listener : LogoutInterface

    fun setLogoutListener(listener : LogoutInterface){
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLogOutPopupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerEvents()
    }

    private fun registerEvents() {
        binding.LogoutButton.setOnClickListener {
            listener.logout()
        }

        binding.CancelButton.setOnClickListener {
            dismiss()
        }
    }

    interface LogoutInterface{
        fun logout()
    }
}