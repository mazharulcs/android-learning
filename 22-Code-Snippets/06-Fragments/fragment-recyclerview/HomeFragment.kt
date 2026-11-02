package com.example.recyclerviewandfragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recyclerviewandfragment.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    // view binding
    private var _binding: FragmentHomeBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentHomeBinding.bind(view)

        // create the data list
        val students = listOf<Student>(
            Student("Abir Mahamud", "1302043", "CSE"),
            Student("Price Ahmed", "1302056", "ECE"),
            Student("Price Ahmed", "1302056", "ECE"),
            Student("Price Ahmed", "1302056", "ECE"),
            Student("Price Ahmed", "1302056", "ECE"),
            Student("Price Ahmed", "1302056", "ECE"),
            Student("Price Ahmed", "1302056", "ECE"),
            Student("Price Ahmed", "1302056", "ECE"),
            Student("Price Ahmed", "1302056", "ECE"),
            Student("Abir Mahamud", "1302043", "CSE"),
            Student("Abir Mahamud", "1302043", "CSE"),
            Student("Abir Mahamud", "1302043", "CSE"),
            Student("Abir Mahamud", "1302043", "CSE"),
            Student("Abir Mahamud", "1302043", "CSE"),
            Student("Abir Mahamud", "1302043", "CSE"),
            Student("Abir Mahamud", "1302043", "CSE"),
            Student("Abir Mahamud", "1302043", "CSE"),
            Student("Abir Mahamud", "1302043", "CSE"),
            Student("Price Ahmed", "1302056", "ECE"),
            Student("Price Ahmed", "1302056", "ECE"),
            Student("Price Ahmed", "1302056", "ECE"),
            Student("Price Ahmed", "1302056", "ECE"),
            Student("Price Ahmed", "1302056", "ECE"),
            Student("Price Ahmed", "1302056", "ECE"),
            Student("Price Ahmed", "1302056", "ECE"),
            Student("Price Ahmed", "1302056", "ECE"),
            Student("Price Ahmed", "1302056", "ECE"),
        )

        // set the recyclerView
        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )

        recyclerView.adapter = StudentAdapter(
            students,
            {
                student ->
                val intent = Intent(requireContext(), StudentDetails::class.java)

                val bundle = Bundle()
                bundle.putString("name", student.name)
                bundle.putString("id", student.id)
                bundle.putString("dept", student.dept)

                intent.putExtras(bundle)
                startActivity(intent)
            }
        )

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}