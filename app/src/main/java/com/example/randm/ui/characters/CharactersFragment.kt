package com.example.randm.ui.characters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rickmorty.databinding.FragmentCharactersBinding
import com.example.randm.di.CharactersViewModelFactory

class CharactersFragment : Fragment() {

    private var _binding: FragmentCharactersBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CharactersViewModel
    private lateinit var adapter: CharacterAdapter

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
        observeViewModel()

        loadCharacters()
    }

    private fun setupViewModel() {
        val factory = CharactersViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[CharactersViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = CharacterAdapter { character ->
            // Navigation will be implemented later
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CharactersFragment.adapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.loadCharacters(name = newText)
                return true
            }
        })
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshCharacters()
        }
    }

    private fun observeViewModel() {
        viewModel.characters.observe(viewLifecycleOwner) { result ->
            when (result) {
                is com.example.rickmorty.data.models.Result.Loading -> {
                    showLoading(true)
                    hideEmptyState()
                    hideError()
                }
                is com.example.rickmorty.data.models.Result.Success -> {
                    showLoading(false)
                    binding.swipeRefreshLayout.isRefreshing = false

                    if (result.data.isEmpty()) {
                        showEmptyState()
                    } else {
                        hideEmptyState()
                        adapter.submitList(result.data)
                    }
                }
                is com.example.rickmorty.data.models.Result.Error -> {
                    showLoading(false)
                    binding.swipeRefreshLayout.isRefreshing = false
                    showError(result.message ?: "Unknown error")
                }
            }
        }
    }

    private fun loadCharacters() {
        viewModel.loadCharacters()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showEmptyState() {
        binding.emptyState.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }

    private fun hideEmptyState() {
        binding.emptyState.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        binding.errorState.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.tvError.text = message
    }

    private fun hideError() {
        binding.errorState.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}