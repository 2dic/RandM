package com.example.randm.ui.characters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.randm.R
import com.example.randm.databinding.FragmentCharactersBinding
import com.example.randm.di.CharactersViewModelFactory
import com.example.randm.ui.MainActivity
import com.example.randm.ui.detail.CharacterDetailFragment
import com.example.randm.ui.filter.FilterFragment

class CharactersFragment : Fragment() {

    private var _binding: FragmentCharactersBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CharactersViewModel
    private lateinit var adapter: CharacterAdapter

    // Сохраняем текущие фильтры
    private var currentFilters = FilterState()
    private var currentQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharactersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        setupSearchView()
        setupSwipeRefresh()
        setupFilterButton()
        setupRetryButton()
        setupFragmentResultListener()
        observeViewModel() // Теперь этот метод существует

        loadCharacters()
    }

    private fun setupFragmentResultListener() {
        // Слушаем результаты из FilterFragment
        setFragmentResultListener("filters_request") { requestKey, bundle ->
            val status = bundle.getString("status")
            val species = bundle.getString("species")
            val gender = bundle.getString("gender")

            currentFilters = FilterState(status, species, gender)
            updateFilterIndicator()
            applyFilters()
        }
    }

    private fun setupViewModel() {
        val factory = CharactersViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[CharactersViewModel::class.java]
    }

    private fun observeViewModel() {
        viewModel.charactersLiveData.observe(viewLifecycleOwner) { result ->
            when (result) {
                is com.example.randm.data.models.Result.Loading -> {
                    showLoading(true)
                    hideEmptyState()
                    hideError()
                }
                is com.example.randm.data.models.Result.Success -> {
                    showLoading(false)
                    binding.swipeRefreshLayout.isRefreshing = false

                    if (result.data.isEmpty()) {
                        showEmptyState(getEmptyStateMessage())
                    } else {
                        hideEmptyState()
                        adapter.submitList(result.data)
                    }
                }
                is com.example.randm.data.models.Result.Error -> {
                    showLoading(false)
                    binding.swipeRefreshLayout.isRefreshing = false

                    if (adapter.itemCount == 0) {
                        showError(result.message ?: "Unknown error")
                    } else {
                        // Показываем Snackbar для некритических ошибок
                        showSnackbar(result.message ?: "Error loading data")
                    }
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        if (_binding == null) return
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showEmptyState(message: String) {
        if (_binding == null) return
        binding.emptyState.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.tvEmptyState.text = message
    }

    private fun hideEmptyState() {
        if (_binding == null) return
        binding.emptyState.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        if (_binding == null) return
        binding.errorState.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.tvError.text = message
    }

    private fun hideError() {
        if (_binding == null) return
        binding.errorState.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }

    private fun showSnackbar(message: String) {
        // Можно добавить Snackbar для некритических ошибок
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun getEmptyStateMessage(): String {
        return when {
            currentQuery.isNotEmpty() -> "No characters found for \"$currentQuery\""
            currentFilters.status != null || currentFilters.species != null || currentFilters.gender != null -> "No characters found with current filters"
            else -> "No characters available"
        }
    }

    private fun setupRecyclerView() {
        adapter = CharacterAdapter { character ->
            navigateToCharacterDetail(character.id)
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@CharactersFragment.adapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText ?: ""
                loadCharacters()
                return true
            }
        })
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshCharacters()
        }
    }

    private fun setupFilterButton() {
        binding.fabFilter.setOnClickListener {
            navigateToFilter()
        }
    }

    private fun setupRetryButton() {
        binding.btnRetry.setOnClickListener {
            loadCharacters()
        }
    }

    private fun navigateToFilter() {
        val filterFragment = FilterFragment.newInstance(
            status = currentFilters.status,
            species = currentFilters.species,
            gender = currentFilters.gender
        )

        // Используйте стандартный подход
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.container, filterFragment)
            .addToBackStack("filter")
            .commit()
    }

    private fun applyFilters() {
        // Безопасно получаем search query
        val searchQuery = try {
            binding.searchView.query?.toString()?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }

        viewModel.loadCharacters(
            name = searchQuery,
            status = currentFilters.status,
            species = currentFilters.species,
            gender = currentFilters.gender
        )
    }

    private fun updateFilterIndicator() {
        if (_binding == null) return

        val hasActiveFilters = currentFilters.hasActiveFilters()

        if (hasActiveFilters) {
            binding.fabFilter.setImageResource(android.R.drawable.ic_menu_edit)
            showActiveFiltersText()
        } else {
            binding.fabFilter.setImageResource(android.R.drawable.ic_menu_sort_by_size)
            hideActiveFiltersText()
        }
    }

    private fun showActiveFiltersText() {
        if (_binding == null) return

        val filters = mutableListOf<String>()
        currentFilters.status?.let { filters.add("Status: $it") }
        currentFilters.species?.let { filters.add("Species: $it") }
        currentFilters.gender?.let { filters.add("Gender: $it") }

        if (filters.isNotEmpty()) {
            binding.tvActiveFilters.visibility = View.VISIBLE
            binding.tvActiveFilters.text = "Active filters: ${filters.joinToString(", ")}"

            binding.tvActiveFilters.setOnClickListener {
                clearAllFilters()
            }
        }
    }

    private fun hideActiveFiltersText() {
        if (_binding == null) return
        binding.tvActiveFilters.visibility = View.GONE
    }

    private fun clearAllFilters() {
        currentFilters = FilterState()
        updateFilterIndicator()
        applyFilters()
    }

    private fun loadCharacters() {
        val searchQuery = try {
            binding.searchView.query?.toString()?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }

        viewModel.loadCharacters(
            name = searchQuery,
            status = currentFilters.status,
            species = currentFilters.species,
            gender = currentFilters.gender
        )
    }

    private fun refreshCharacters() {
        val searchQuery = try {
            binding.searchView.query?.toString()?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }

        viewModel.refreshCharacters(
            name = searchQuery,
            status = currentFilters.status,
            species = currentFilters.species,
            gender = currentFilters.gender
        )
    }

    private fun navigateToCharacterDetail(characterId: Int) {
        val detailFragment = CharacterDetailFragment.newInstance(characterId)

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.container, detailFragment)
            .addToBackStack("character_detail")
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Data class для хранения состояния фильтров
    data class FilterState(
        val status: String? = null,
        val species: String? = null,
        val gender: String? = null
    ) {
        fun hasActiveFilters(): Boolean {
            return status != null || species != null || gender != null
        }
    }
}