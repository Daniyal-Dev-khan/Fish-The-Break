package com.cp.fishthebreak.screens.fragments.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.profile.ResourcesSubAdapter
import com.cp.fishthebreak.databinding.FragmentArticleListBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.utils.NavigationDirections
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.hideKeyboardFrom
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.cp.fishthebreak.utils.showKeyboard
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.profile.resources.ResourcesListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArticleListFragment : Fragment() {
    private lateinit var binding: FragmentArticleListBinding
    val viewModel: ResourcesListViewModel by viewModels()
    private val navArgs: ArticleListFragmentArgs by navArgs()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getResourceById(navArgs.articleId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentArticleListBinding.inflate(layoutInflater, container, false)
        initDataBinding()
        initViewModelResponse()
        initListeners()
        return binding.root
    }

    private fun initDataBinding(){
        binding.lifecycleOwner = this@ArticleListFragment
        binding.viewModel = viewModel
        binding.resourcesAdapter = ResourcesSubAdapter(listOf(),viewModel)
    }

    private fun initListeners() {
        binding.backButton.setOnClickListener {
            if (requireActivity() is NavGraphActivity) {
                (requireActivity() as NavGraphActivity).onBack()
            }
        }
        binding.ivSearch.setOnSingleClickListener {
            binding.ivSearch.viewGone()
            binding.layoutSearch.viewVisible()
            binding.tvSearch.requestFocus()
            binding.tvSearch.showKeyboard()
        }
        binding.ivClearSearch.setOnSingleClickListener {
            binding.tvSearch.setText("")
            binding.ivSearch.viewVisible()
            binding.layoutSearch.viewGone()
            requireContext().hideKeyboardFrom(binding.ivClearSearch)
        }
    }

    private fun showHideLoader(visibility: Boolean) {
        if (visibility) {
            binding.loaderLayout.viewVisible()
        } else {
            binding.loaderLayout.viewGone()
        }
    }

    private fun initViewModelResponse() {
        lifecycleScope.launch {
            viewModel.resourcesResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true) {

                            } else {
                                requireActivity().showToast(
                                    response.data?.message ?: resources.getString(
                                        R.string.something_went_wrong
                                    ), false
                                )
                                viewModel.resetResponse()
                            }
                        }

                        is NetworkResult.Error -> {
                            showHideLoader(false)
                            requireActivity().showToast(
                                response.message.getOnErrorMessage(),
                                false
                            )
                            viewModel.resetResponse()
                        }

                        is NetworkResult.Loading -> {
                            showHideLoader(true)
                        }

                        is NetworkResult.NoInternet -> {
                            showHideLoader(false)
                            requireActivity().showToast(
                                resources.getString(R.string.no_internet),
                                false
                            )
                            viewModel.resetResponse()
                        }

                        is NetworkResult.NoCallYet -> {
                        }
                    }
                }
        }
        lifecycleScope.launch {
            viewModel.navigationResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when(response){
                        is NavigationDirections.ResourceList -> {
                            viewModel.resetNavigationResponse()
                        }
                        is NavigationDirections.ResourceScreen -> {
                            findNavController().navigate(ArticleListFragmentDirections.actionArticleListFragmentToSingleResourceFragment(response.data.base_url_webpage+response.data.slug))
                            viewModel.resetNavigationResponse()
                        }
                        else -> {}
                    }
                }
        }

    }
}