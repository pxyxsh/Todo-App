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
import com.example.todo.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth


class SignUpFragment : Fragment() {

    private lateinit var auth : FirebaseAuth
    private lateinit var navControl : NavController
    private lateinit var binding : FragmentSignUpBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        registerEvents()
    }

    private fun registerEvents() {
        binding.SignUpButton.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val rePassword = binding.etRePassword.text.toString().trim()

            if(email.isNotEmpty() && password.isNotEmpty() && rePassword.isNotEmpty()){
                if(password == rePassword) {
                    binding.SignUpButton.visibility = View.INVISIBLE
                    binding.progressBarSignUp.visibility = View.VISIBLE
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                        if(it.isSuccessful){
                            Toast.makeText(context, "Registered Successfully", Toast.LENGTH_SHORT).show()
                            navControl.navigate(R.id.action_signUpFragment_to_signInFragment)
                        }else{
                            Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
                            binding.SignUpButton.visibility = View.VISIBLE
                            binding.progressBarSignUp.visibility = View.GONE
                        }

                    }
                }else{
                    Toast.makeText(context, "Passwords do not match. Re-Enter the same password", Toast.LENGTH_SHORT).show()
                    binding.etPassword.text?.clear()
                    binding.etRePassword.text?.clear()
                }
            }else {
                Toast.makeText(context, "Please enter all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.SignInIntentText.setOnClickListener {
            navControl.navigate(R.id.action_signUpFragment_to_signInFragment)
        }
    }

    private fun init(view: View) {
        navControl = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
    }

}