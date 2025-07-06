package com.cp.fishthebreak.screens.fragments.group

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.group.ReasonListAdapter
import com.cp.fishthebreak.databinding.FragmentRejectionReasonBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.hideKeyboardFrom
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.group.ReasonViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RejectionReasonFragment : Fragment() {
    private lateinit var binding: FragmentRejectionReasonBinding
    val viewModel: ReasonViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentRejectionReasonBinding.inflate(layoutInflater,container,false)
        initDataBinding()
        initViewModelResponse()
        initListeners()
        return binding.root
    }

    private fun initListeners(){
        binding.etOther.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(binding.etOther.text.toString().trim().isNotEmpty()){
                    viewModel.clearSelection()
                    binding.rv.adapter?.notifyDataSetChanged()
                }else{
                    viewModel.restoreSelection()
                    binding.rv.adapter?.notifyDataSetChanged()
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        binding.backButton.setOnClickListener {
            if(requireActivity() is NavGraphActivity){
                (requireActivity() as NavGraphActivity).onBack()
            }
        }

        binding.saveButton.setOnClickListener {
            val selected = viewModel.list.value.filter { item-> item.isSelected }
            if(selected.isNotEmpty() || binding.etOther.text.toString().trim().isNotEmpty()){
                val sIntent = Intent()
                if(selected.isNotEmpty()) {
                    sIntent.putExtra("selectedReason", selected.first().reason)
                }else{
                    sIntent.putExtra("selectedReason", binding.etOther.text.toString().trim())
                }
                requireActivity().setResult(Activity.RESULT_OK, sIntent)
                requireActivity().finish()
            }else{
                requireActivity().showToast(resources.getString(R.string.error_reason),false)
            }
        }
    }

    private fun initDataBinding() {
        binding.lifecycleOwner = this@RejectionReasonFragment
        binding.viewModel = viewModel
        val mLayoutManager = LinearLayoutManager(requireContext())
        binding.rv.layoutManager = mLayoutManager
        binding.chatAdapter = ReasonListAdapter(listOf(), viewModel)
    }

    private fun initViewModelResponse() {
        lifecycleScope.launch {
            viewModel.reasonResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
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
                            requireActivity().showToast(
                                response.message.getOnErrorMessage(),
                                false
                            )
                            viewModel.resetResponse()
                        }

                        is NetworkResult.Loading -> {

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
            viewModel.loadingResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    showHideLoader(response)
                }
        }
        lifecycleScope.launch {
            viewModel.notifyAdapter
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    if(response){
                        binding.rv.adapter?.notifyDataSetChanged()
                        binding.etOther.setText("")
                        requireContext().hideKeyboardFrom(binding.etOther)
                        viewModel.resetNotifyResponse()
                    }
                }
        }
    }

    private fun showHideLoader(visibility: Boolean) {
        if (visibility) {
            if(!binding.pullToRefresh.isRefreshing) {
                binding.loaderLayout.viewVisible()
            }
        } else {
            binding.loaderLayout.viewGone()
            binding.pullToRefresh.isRefreshing = false
        }
    }

}