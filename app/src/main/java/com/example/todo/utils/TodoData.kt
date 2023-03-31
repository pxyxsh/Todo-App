package com.example.todo.utils

data class TodoData(val taskId : String, var task : String, val checked : Boolean)
data class TodoState(var task: String, var isChecked: Boolean)
