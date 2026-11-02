package com.example.recyclerviewandfragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.recyclerviewandfragment.databinding.ItemStudentBinding

class StudentAdapter(
    private val students: List<Student>,
    private val clickAction: (Student) -> Unit
): RecyclerView.Adapter<StudentViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StudentViewHolder {

        val binding = ItemStudentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: StudentViewHolder,
        position: Int
    ) {
        val student = students[position]
        holder.binding.tvName.text = student.name
        holder.binding.tvId.text = student.id
        holder.binding.tvDept.text = student.dept

        holder.binding.root.setOnClickListener {
            clickAction(student)
        }
    }

    override fun getItemCount(): Int {
        return students.size
    }

}