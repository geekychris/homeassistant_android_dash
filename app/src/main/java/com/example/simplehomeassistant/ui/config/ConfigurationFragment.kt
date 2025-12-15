package com.example.simplehomeassistant.ui.config

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simplehomeassistant.databinding.FragmentConfigurationBinding
import com.example.simplehomeassistant.ui.auth.WebViewAuthActivity
import com.google.android.material.snackbar.Snackbar

class ConfigurationFragment : Fragment() {

    private var _binding: FragmentConfigurationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ConfigurationViewModel by viewModels()
    private lateinit var adapter: ConfigurationAdapter

    // Store configuration details for WebView auth
    private var pendingConfigName: String = ""
    private var pendingInternalUrl: String = ""
    private var pendingExternalUrl: String = ""
    private var pendingInternalToken: String = ""
    private var pendingConfigId: Long = 0
    private var isAuthenticatingExternal: Boolean = false

    // Activity result launcher for WebView authentication
    private val webViewAuthLauncher: androidx.activity.result.ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val accessToken =
                result.data?.getStringExtra(WebViewAuthActivity.EXTRA_ACCESS_TOKEN) ?: ""
            val refreshToken =
                result.data?.getStringExtra(WebViewAuthActivity.EXTRA_REFRESH_TOKEN) ?: ""

            if (accessToken.isNotEmpty()) {
                // Single token works for both internal and external URLs
                // (they point to the same Home Assistant instance, just different network paths)
                android.util.Log.d("ConfigDebug", "Got token for: $pendingConfigName")
                android.util.Log.d("ConfigDebug", "Internal URL: [$pendingInternalUrl]")
                android.util.Log.d("ConfigDebug", "External URL: [$pendingExternalUrl]")
                android.util.Log.d("ConfigDebug", "Token length: ${accessToken.length}")

                viewModel.saveConfiguration(
                    pendingConfigName,
                    pendingInternalUrl,
                    pendingExternalUrl,
                    accessToken, // Single token for both URLs
                    null,
                    null,
                    null, // No separate external token
                    false,
                    pendingConfigId // Pass the ID to support edit
                )
                isAuthenticatingExternal = false
                clearPendingAuthState()
                clearForm()
                Snackbar.make(
                    binding.root,
                    "Configuration saved successfully",
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                Snackbar.make(binding.root, "Failed to get access token", Snackbar.LENGTH_LONG)
                    .show()
                isAuthenticatingExternal = false
            }
        } else {
            Snackbar.make(binding.root, "Authentication cancelled", Snackbar.LENGTH_SHORT).show()
            isAuthenticatingExternal = false
        }
    }

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
            },
            onEdit = { configuration ->
                // Pre-fill the form with existing configuration
                binding.nameEditText.setText(configuration.name)
                binding.internalUrlEditText.setText(configuration.internalUrl)
                binding.externalUrlEditText.setText(configuration.externalUrl)
                binding.apiTokenEditText.setText(configuration.apiToken)
                binding.externalApiTokenEditText.setText(configuration.externalApiToken ?: "")

                // Set auth method
                if (configuration.useTokenAuth) {
                    binding.tokenRadioButton.isChecked = true
                } else {
                    binding.usernamePasswordRadioButton.isChecked = true
                    binding.usernameEditText.setText(configuration.username ?: "")
                    binding.passwordEditText.setText(configuration.password ?: "")
                }

                // Store the ID so we can update instead of create
                pendingConfigId = configuration.id

                Snackbar.make(binding.root, "Editing ${configuration.name}", Snackbar.LENGTH_SHORT).show()
            },
            onUrlPreferenceChange = { configuration, preferInternal ->
                viewModel.updateUrlPreference(configuration, preferInternal)
                val urlType = if (preferInternal) "internal" else "external"
                Snackbar.make(
                    binding.root,
                    "Switched to $urlType URL",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        )

        binding.configurationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ConfigurationFragment.adapter
        }
    }

    private fun setupListeners() {
        // Toggle between token and username/password authentication
        binding.authMethodRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.tokenRadioButton.id -> {
                    // Show single token field (works for both internal and external URLs)
                    binding.apiTokenInputLayout.visibility = View.VISIBLE
                    binding.externalApiTokenInputLayout.visibility =
                        View.GONE // Single token for both
                    binding.usernameInputLayout.visibility = View.GONE
                    binding.passwordInputLayout.visibility = View.GONE
                    binding.webViewAuthHelp.visibility = View.GONE
                }

                binding.usernamePasswordRadioButton.id -> {
                    // Hide token fields, show WebView help message
                    binding.apiTokenInputLayout.visibility = View.GONE
                    binding.externalApiTokenInputLayout.visibility = View.GONE
                    binding.usernameInputLayout.visibility = View.GONE
                    binding.passwordInputLayout.visibility = View.GONE
                    binding.webViewAuthHelp.visibility = View.VISIBLE
                }
            }
        }

        binding.saveButton.setOnClickListener {
            val name = binding.nameEditText.text?.toString() ?: ""
            val internalUrl = binding.internalUrlEditText.text?.toString() ?: ""
            val externalUrl = binding.externalUrlEditText.text?.toString() ?: ""

            val useTokenAuth = binding.tokenRadioButton.isChecked

            if (name.isEmpty() || internalUrl.isEmpty() || externalUrl.isEmpty()) {
                Snackbar.make(binding.root, "Name and URLs are required", Snackbar.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (useTokenAuth) {
                // Token authentication - single token works for both URLs
                val apiToken = binding.apiTokenEditText.text?.toString() ?: ""

                if (apiToken.isEmpty()) {
                    Snackbar.make(
                        binding.root,
                        "API Token is required",
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
                    return@setOnClickListener
                }
                viewModel.saveConfiguration(
                    name,
                    internalUrl,
                    externalUrl,
                    apiToken,
                    null,
                    null,
                    null, // No separate external token
                    false,
                    pendingConfigId
                )

                // Clear the ID after saving
                pendingConfigId = 0
            } else {
                // WebView authentication - launch browser login
                pendingConfigName = name
                pendingInternalUrl = internalUrl
                pendingExternalUrl = externalUrl

                // Use internal URL if available, otherwise external
                val baseUrl = if (internalUrl.isNotEmpty()) internalUrl else externalUrl

                val intent = Intent(requireContext(), WebViewAuthActivity::class.java).apply {
                    putExtra(WebViewAuthActivity.EXTRA_BASE_URL, baseUrl)
                }
                webViewAuthLauncher.launch(intent)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.configurations.observe(viewLifecycleOwner) { configurations ->
            val activeId = viewModel.activeConfiguration.value?.id
            val previousSize = adapter.itemCount
            adapter.submitList(configurations, activeId)

            // Scroll to newly added item if list size increased
            if (configurations.size > previousSize && previousSize > 0) {
                binding.configurationsRecyclerView.smoothScrollToPosition(configurations.size - 1)
            }
        }

        viewModel.activeConfiguration.observe(viewLifecycleOwner) { activeConfig ->
            val configs = viewModel.configurations.value ?: emptyList()
            adapter.submitList(configs, activeConfig?.id)
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = { savedConfigId ->
                    android.util.Log.d(
                        "ConfigFragment",
                        "Configuration saved with ID: $savedConfigId"
                    )
                    android.util.Log.d(
                        "ConfigFragment",
                        "Active config before: ${viewModel.activeConfiguration.value?.id}"
                    )

                    Snackbar.make(binding.root, "Configuration saved", Snackbar.LENGTH_SHORT).show()
                    clearForm()

                    // Auto-activate if no active configuration exists
                    val hasActiveConfig = viewModel.activeConfiguration.value != null
                    android.util.Log.d("ConfigFragment", "Has active config: $hasActiveConfig")

                    if (!hasActiveConfig && savedConfigId > 0) {
                        android.util.Log.d(
                            "ConfigFragment",
                            "Auto-activating config ID: $savedConfigId"
                        )
                        viewModel.activateConfigurationById(savedConfigId)
                        Snackbar.make(
                            binding.root,
                            "Activated as default configuration",
                            Snackbar.LENGTH_LONG
                        ).show()
                    } else {
                        android.util.Log.d(
                            "ConfigFragment",
                            "NOT auto-activating. HasActive: $hasActiveConfig, SavedId: $savedConfigId"
                        )
                    }
                },
                onFailure = { error ->
                    android.util.Log.e("ConfigFragment", "Save failed: ${error.message}")
                    Snackbar.make(binding.root, "Error: ${error.message}", Snackbar.LENGTH_LONG)
                        .show()
                }
            )
        }

        viewModel.testResult.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = { message ->
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                },
                onFailure = { error ->
                    Snackbar.make(binding.root, error.message ?: "Test failed", Snackbar.LENGTH_LONG)
                        .show()
                }
            )
        }

        // Test Internal URL button
        binding.testInternalButton.setOnClickListener {
            val internalUrl = binding.internalUrlEditText.text?.toString() ?: ""
            val apiToken = binding.apiTokenEditText.text?.toString() ?: ""

            if (internalUrl.isEmpty()) {
                Snackbar.make(binding.root, "Internal URL is required", Snackbar.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (apiToken.isEmpty()) {
                Snackbar.make(binding.root, "API Token is required to test", Snackbar.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            viewModel.testConnection(internalUrl, apiToken, "Internal")
        }

        // Test External URL button
        binding.testExternalButton.setOnClickListener {
            val externalUrl = binding.externalUrlEditText.text?.toString() ?: ""
            val apiToken = binding.apiTokenEditText.text?.toString() ?: ""

            if (externalUrl.isEmpty()) {
                Snackbar.make(binding.root, "External URL is required", Snackbar.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (apiToken.isEmpty()) {
                Snackbar.make(binding.root, "API Token is required to test", Snackbar.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            viewModel.testConnection(externalUrl, apiToken, "External")
        }
    }

    private fun clearForm() {
        binding.nameEditText.text?.clear()
        binding.internalUrlEditText.text?.clear()
        binding.externalUrlEditText.text?.clear()
        binding.apiTokenEditText.text?.clear()
        binding.externalApiTokenEditText.text?.clear()
        binding.usernameEditText.text?.clear()
        binding.passwordEditText.text?.clear()
        binding.tokenRadioButton.isChecked = true
    }

    private fun clearPendingAuthState() {
        pendingConfigName = ""
        pendingInternalUrl = ""
        pendingExternalUrl = ""
        pendingInternalToken = ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
