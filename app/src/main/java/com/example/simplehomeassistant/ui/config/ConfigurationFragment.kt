package com.example.simplehomeassistant.ui.config

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simplehomeassistant.databinding.FragmentConfigurationBinding
import com.google.android.material.snackbar.Snackbar

class ConfigurationFragment : Fragment() {

    private var _binding: FragmentConfigurationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ConfigurationViewModel by viewModels()
    private lateinit var adapter: ConfigurationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfigurationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ConfigurationAdapter(
            onActivate = { configuration ->
                viewModel.setActiveConfiguration(configuration)
                Snackbar.make(
                    binding.root,
                    "${configuration.name} activated",
                    Snackbar.LENGTH_SHORT
                ).show()
            },
            onDelete = { configuration ->
                viewModel.deleteConfiguration(configuration)
                Snackbar.make(binding.root, "${configuration.name} deleted", Snackbar.LENGTH_SHORT)
                    .show()
            }
        )

        binding.configurationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ConfigurationFragment.adapter
        }
    }

    private fun setupListeners() {
        binding.saveButton.setOnClickListener {
            val name = binding.nameEditText.text?.toString() ?: ""
            val internalUrl = binding.internalUrlEditText.text?.toString() ?: ""
            val externalUrl = binding.externalUrlEditText.text?.toString() ?: ""
            val apiToken = binding.apiTokenEditText.text?.toString() ?: ""

            if (name.isEmpty() || internalUrl.isEmpty() || externalUrl.isEmpty() || apiToken.isEmpty()) {
                Snackbar.make(binding.root, "All fields are required", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.saveConfiguration(name, internalUrl, externalUrl, apiToken)
        }
    }

    private fun observeViewModel() {
        viewModel.configurations.observe(viewLifecycleOwner) { configurations ->
            val activeId = viewModel.activeConfiguration.value?.id
            adapter.submitList(configurations, activeId)
        }

        viewModel.activeConfiguration.observe(viewLifecycleOwner) { activeConfig ->
            val configs = viewModel.configurations.value ?: emptyList()
            adapter.submitList(configs, activeConfig?.id)
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = {
                    Snackbar.make(binding.root, "Configuration saved", Snackbar.LENGTH_SHORT).show()
                    clearForm()
                },
                onFailure = { error ->
                    Snackbar.make(binding.root, "Error: ${error.message}", Snackbar.LENGTH_LONG)
                        .show()
                }
            )
        }
    }

    private fun clearForm() {
        binding.nameEditText.text?.clear()
        binding.internalUrlEditText.text?.clear()
        binding.externalUrlEditText.text?.clear()
        binding.apiTokenEditText.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
