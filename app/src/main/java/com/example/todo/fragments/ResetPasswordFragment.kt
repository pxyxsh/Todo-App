package com.example.todo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.todo.R
import com.example.todo.databinding.FragmentResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth


class ResetPasswordFragment : Fragment() {

    private lateinit var binding : FragmentResetPasswordBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var navControl: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        registerEvents()
    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        navControl = Navigation.findNavController(view)
    }

    private fun registerEvents() {
        binding.SubmitButton.setOnClickListener {
            val email = binding.etEmail.text.toString()
            if(email.isNotEmpty()){
                resetPassword(email)
            }
        }
        binding.BackButton.setOnClickListener {
            navControl.navigate(R.id.action_resetPasswordFragment_to_signInFragment)
        }
    }

    private fun resetPassword(email: String) {
        binding.SubmitButton.visibility = View.INVISIBLE
        binding.progressBarForgotPassword.visibility = View.VISIBLE
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(context, "Reset password link has been sent to your registered Email", Toast.LENGTH_LONG).show()
                navControl.navigate(R.id.action_resetPasswordFragment_to_signInFragment)
            }else{
                Toast.makeText(context, it.exception?.message.toString(), Toast.LENGTH_SHORT).show()
            }
            binding.progressBarForgotPassword.visibility = View.GONE
            binding.SubmitButton.visibility = View.VISIBLE
        }
    }
}