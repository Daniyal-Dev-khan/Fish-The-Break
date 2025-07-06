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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.layers.MapLayersAdapter
import com.cp.fishthebreak.adapters.profile.ResourcesAdapter
import com.cp.fishthebreak.databinding.FragmentArticlesBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.utils.NavigationDirections
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.profile.resources.ResourcesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArticlesFragment : Fragment() {
    private lateinit var binding: FragmentArticlesBinding
    val viewModel: ResourcesViewModel by viewModels()
    private val mAdapter: ResourcesAdapter by lazy {
        ResourcesAdapter(listOf(), viewModel, viewModel)
    }
    private var mLayoutManager: LinearLayoutManager? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentArticlesBinding.inflate(layoutInflater, container, false)
        binding.stickyHeader.viewGone()
        initDataBinding()
        initViewModelResponse()
        initListeners()
        return binding.root
    }

    private fun initListeners() {
        binding.backButton.setOnClickListener {
            if (requireActivity() is NavGraphActivity) {
                (requireActivity() as NavGraphActivity).onBack()
            }
        }
        binding.readMoreButton.setOnClickListener {
            viewModel.resourcesResponse.value.data?.data?.featured_resource?.let { data ->
                findNavController().navigate(ArticlesFragmentDirections.actionArticlesFragmentToSingleResourceFragment(
                    data.base_url_webpage + data.slug
                ))
            }
        }
        binding.viewAllButton1.setOnClickListener {
            val firstVisibleItem = mLayoutManager?.findFirstVisibleItemPosition()?:0
            if(!viewModel.resourcesResponse.value.data?.data?.resources.isNullOrEmpty() && firstVisibleItem >= 0){
                findNavController().navigate(
                    ArticlesFragmentDirections.actionArticlesFragmentToArticleListFragment(
                        viewModel.resourcesResponse.value.data?.data?.resources?.get(firstVisibleItem)?.id?:-1
                    )
                )
            }
        }
    }

    private fun initDataBinding() {
        binding.lifecycleOwner = this@ArticlesFragment
        binding.viewModel = viewModel
        mLayoutManager = LinearLayoutManager(requireContext())
        binding.rvArticle.layoutManager = mLayoutManager
        binding.resourcesAdapter = mAdapter
        binding.rvArticle.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
                val firstVisibleItem = mLayoutManager?.findFirstVisibleItemPosition()?:0
                if(!viewModel.resourcesResponse.value.data?.data?.resources.isNullOrEmpty() && firstVisibleItem >= 0){
                    binding.tvStickyHeader.text = viewModel.resourcesResponse.value.data?.data?.resources?.get(firstVisibleItem)?.type_name?:""
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
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
                                binding.stickyHeader.viewVisible()
                                val firstVisibleItem = mLayoutManager?.findFirstVisibleItemPosition()?:0
                                if(response.data.data.resources.isNotEmpty() && firstVisibleItem >= 0){
                                    binding.tvStickyHeader.text = response.data.data.resources[firstVisibleItem].type_name
                                }else if(response.data.data.resources.isNotEmpty()){
                                    binding.tvStickyHeader.text = response.data.data.resources.first().type_name
                                }
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
                    when (response) {
                        is NavigationDirections.ResourceList -> {
                            findNavController().navigate(
                                ArticlesFragmentDirections.actionArticlesFragmentToArticleListFragment(
                                    response.data.id
                                )
                            )
                            viewModel.resetNavigationResponse()
                        }

                        is NavigationDirections.ResourceScreen -> {
                            findNavController().navigate(
                                ArticlesFragmentDirections.actionArticlesFragmentToSingleResourceFragment(
                                    response.data.base_url_webpage + response.data.slug
                                )
                            )
                            viewModel.resetNavigationResponse()
                        }

                        else -> {}
                    }
                }
        }

    }

}