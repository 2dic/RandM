package com.example.randm.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.randm.databinding.FragmentCharacterDetailBinding
import com.example.randm.di.CharacterDetailViewModelFactory

class CharacterDetailFragment : Fragment() {

    private var _binding: FragmentCharacterDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CharacterDetailViewModel
    private var characterId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            characterId = it.getInt("character_id")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharacterDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupToolbar()
        setupRetryButton()
        observeViewModel()

        // Загружаем данные, если ViewModel еще не загрузила
        if (viewModel.characterLiveData.value == null) {
            viewModel.loadCharacter()
        }
    }

    private fun setupViewModel() {
        val factory = CharacterDetailViewModelFactory(requireActivity().application, characterId)
        viewModel = ViewModelProvider(this, factory)[CharacterDetailViewModel::class.java]
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupRetryButton() {
        binding.btnRetry.setOnClickListener {
            viewModel.loadCharacter()
        }
    }

    private fun observeViewModel() {
        viewModel.characterLiveData.observe(viewLifecycleOwner) { result ->
            when (result) {
                is com.example.randm.data.models.Result.Loading -> {
                    showLoading(true)
                    hideContent()
                    hideError()
                }
                is com.example.randm.data.models.Result.Success -> {
                    showLoading(false)
                    showContent()
                    hideError()
                    bindCharacter(result.data)
                }
                is com.example.randm.data.models.Result.Error -> {
                    showLoading(false)
                    hideContent()
                    showError(result.message ?: "Unknown error occurred")
                }
            }
        }
    }

    private fun bindCharacter(character: com.example.randm.data.models.Character) {
        // Загрузка изображения
        Glide.with(requireContext())
            .load(character.image)
            .into(binding.ivCharacterImage)

        // Установка данных
        binding.tvCharacterName.text = character.name
        binding.tvStatusValue.text = character.status
        binding.tvSpeciesValue.text = character.species
        binding.tvGenderValue.text = character.gender
        binding.tvTypeValue.text = if (character.type.isNotEmpty()) character.type else "Unknown"
        binding.tvOriginValue.text = character.origin.name
        binding.tvLocationValue.text = character.location.name

        // Форматируем дату создания (если нужно)
        val createdDate = try {
            character.created.substring(0, 10) // Берем только дату
        } catch (e: Exception) {
            character.created
        }
        binding.tvCreatedValue.text = createdDate

        // Устанавливаем цвет статуса
        val statusColor = when (character.status.lowercase()) {
            "alive" -> ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
            "dead" -> ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
            else -> ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
        }
        binding.tvStatusValue.setTextColor(statusColor)

        // Обновляем заголовок тулбара
        binding.toolbar.title = character.name
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showContent() {
        binding.content.visibility = View.VISIBLE
    }

    private fun hideContent() {
        binding.content.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.errorState.visibility = View.VISIBLE
        binding.tvError.text = message
    }

    private fun hideError() {
        binding.errorState.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_CHARACTER_ID = "character_id"

        fun newInstance(characterId: Int): CharacterDetailFragment {
            return CharacterDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_CHARACTER_ID, characterId)
                }
            }
        }
    }
}