package com.example.randm.ui.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.randm.R
import com.example.randm.databinding.FragmentFilterBinding

class FilterFragment : Fragment() {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    // Сохраняем текущие значения фильтров
    private var currentStatus: String? = null
    private var currentSpecies: String? = null
    private var currentGender: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Восстанавливаем значения из аргументов
        arguments?.let {
            currentStatus = it.getString(ARG_STATUS)
            currentSpecies = it.getString(ARG_SPECIES)
            currentGender = it.getString(ARG_GENDER)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupFilterOptions()
        setupApplyButton()
        setupClearButton()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            applyCurrentFilters()
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupFilterOptions() {
        // Восстанавливаем Status из сохраненных значений
        when (currentStatus) {
            "alive" -> binding.radioGroupStatus.check(R.id.radioAlive)
            "dead" -> binding.radioGroupStatus.check(R.id.radioDead)
            "unknown" -> binding.radioGroupStatus.check(R.id.radioUnknown)
            else -> binding.radioGroupStatus.clearCheck()
        }

        // Восстанавливаем Gender из сохраненных значений
        when (currentGender) {
            "female" -> binding.radioGroupGender.check(R.id.radioFemale)
            "male" -> binding.radioGroupGender.check(R.id.radioMale)
            "genderless" -> binding.radioGroupGender.check(R.id.radioGenderless)
            "unknown" -> binding.radioGroupGender.check(R.id.radioGenderUnknown)
            else -> binding.radioGroupGender.clearCheck()
        }

        // Восстанавливаем Species из сохраненных значений
        if (!currentSpecies.isNullOrEmpty()) {
            binding.etSpecies.setText(currentSpecies)
        } else {
            binding.etSpecies.text.clear()
        }
    }

    private fun setupApplyButton() {
        binding.btnApply.setOnClickListener {
            applyCurrentFilters()
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupClearButton() {
        binding.btnClear.setOnClickListener {
            clearAllFilters()
        }
    }

    private fun applyCurrentFilters() {
        val status = getSelectedStatus()
        val species = getSelectedSpecies()
        val gender = getSelectedGender()

        // Сохраняем текущие значения
        currentStatus = status
        currentSpecies = species
        currentGender = gender

        // Отправляем результаты через Fragment Result API
        val result = Bundle().apply {
            putString("status", status)
            putString("species", species)
            putString("gender", gender)
        }
        parentFragmentManager.setFragmentResult("filters_request", result)
    }

    private fun getSelectedStatus(): String? {
        return when (binding.radioGroupStatus.checkedRadioButtonId) {
            R.id.radioAlive -> "alive"
            R.id.radioDead -> "dead"
            R.id.radioUnknown -> "unknown"
            else -> null
        }
    }

    private fun getSelectedGender(): String? {
        return when (binding.radioGroupGender.checkedRadioButtonId) {
            R.id.radioFemale -> "female"
            R.id.radioMale -> "male"
            R.id.radioGenderless -> "genderless"
            R.id.radioGenderUnknown -> "unknown"
            else -> null
        }
    }

    private fun getSelectedSpecies(): String? {
        return binding.etSpecies.text.toString().takeIf { it.isNotBlank() }
    }

    private fun clearAllFilters() {
        binding.radioGroupStatus.clearCheck()
        binding.radioGroupGender.clearCheck()
        binding.etSpecies.text.clear()

        // Сбрасываем сохраненные значения
        currentStatus = null
        currentSpecies = null
        currentGender = null

        // Немедленно применяем сброс
        val result = Bundle().apply {
            putString("status", null)
            putString("species", null)
            putString("gender", null)
        }
        parentFragmentManager.setFragmentResult("filters_request", result)
        requireActivity().supportFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_STATUS = "current_status"
        private const val ARG_SPECIES = "current_species"
        private const val ARG_GENDER = "current_gender"

        fun newInstance(status: String?, species: String?, gender: String?): FilterFragment {
            return FilterFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_STATUS, status)
                    putString(ARG_SPECIES, species)
                    putString(ARG_GENDER, gender)
                }
            }
        }
    }
}