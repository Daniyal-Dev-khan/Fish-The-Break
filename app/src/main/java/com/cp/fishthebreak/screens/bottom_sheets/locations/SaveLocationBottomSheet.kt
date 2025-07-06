package com.cp.fishthebreak.screens.bottom_sheets.locations

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.FragmentSaveLocationBottomSheetBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.screens.activities.BaseActivityResult
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.utils.MapUiData
import com.cp.fishthebreak.utils.SelectImageListener
import com.cp.fishthebreak.utils.getNauticalLatitude
import com.cp.fishthebreak.utils.getNauticalLongitude
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.loadImage
import com.cp.fishthebreak.utils.selectImage
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.transformIntoDatePicker
import com.cp.fishthebreak.utils.transformIntoLatPicker
import com.cp.fishthebreak.utils.transformIntoTimePicker
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.profile.locations.LocationViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.Date

@AndroidEntryPoint
class SaveLocationBottomSheet(
    private val lat: Double?,
    private val long: Double?,
    private val isFishLogOnly: Boolean = false,
    private val data: MapUiData? = null,
    private val listener: OnClickListener? = null,
    private val disableEditing: Boolean = false
) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentSaveLocationBottomSheetBinding
    val viewModel: LocationViewModel by viewModels()
    private val activityLauncher: BaseActivityResult<Intent, ActivityResult> =
        BaseActivityResult.registerActivityForResult(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        try {
            (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
            (dialog as? BottomSheetDialog)?.behavior?.isDraggable = false
        } catch (ex: Exception) {
        }
        binding = FragmentSaveLocationBottomSheetBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.model = viewModel
        initViewModelResponse()
        initListeners()
        initViews()
        return binding.root
    }

    private fun initViews() {
        if(requireActivity() is NavGraphActivity){
            binding.ivBottom.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.bg_bottom_sheet_bottom))
        }
        when (data) {
            is MapUiData.FishLogData -> {
                viewModel.onRadioButtonChangeEvent(binding.rbFishlog.id)
                binding.tfWeight.viewVisible()
                binding.tfDate.viewVisible()
                binding.tfLengthInches.viewVisible()
                binding.tfLengthFeet.viewVisible()
                binding.tfTime.viewVisible()
                binding.tvSave.viewGone()
                binding.radioGroup.viewGone()
                binding.buttonSave.text = resources.getString(R.string.save_fish_log)
                viewModel.onIdChangeEvent(data.data.id)
                viewModel.onNameChangeEvent(data.data.fish_name ?: "")
                viewModel.onDescriptionChangeEvent(data.data.description ?: "")
                viewModel.onDateChangeEvent(data.data.date?:"")
                viewModel.onTimeChangeEvent(data.data.time?:"")
                viewModel.onWeightChangeEvent(data.data.weight ?: "")
                if (!data.data.length.isNullOrEmpty()) {
                    val lengthData = data.data.length?.split("'")
                    if (!lengthData.isNullOrEmpty()){
                        if(lengthData.first().contains("\"")){
                            viewModel.onLengthInchesChangeEvent(lengthData.first().replace("'","").trim())
                        }
                        else{
                            viewModel.onLengthChangeEvent(lengthData.first().replace("'","").trim())
                        }
                    }
                    if (!lengthData.isNullOrEmpty() && lengthData.size > 1){
                        viewModel.onLengthInchesChangeEvent(lengthData[1].replace("\"","").trim())
                    }
                }
                binding.iv.loadImage(
                    data.data.base_url + data.data.image,
                    R.drawable.place_holder_square,
                    R.drawable.place_holder_square
                )
            }

            is MapUiData.LocationData -> {
                viewModel.onRadioButtonChangeEvent(binding.rbLocation.id)
                binding.tfWeight.viewGone()
                binding.tfDate.viewGone()
                binding.tfLengthFeet.viewGone()
                binding.tfLengthInches.viewGone()
                binding.tfTime.viewGone()
                binding.radioGroup.viewGone()
                binding.tvSave.viewGone()
                binding.buttonSave.text = resources.getString(R.string.save_location)
                viewModel.onIdChangeEvent(data.data.id)
                viewModel.onNameChangeEvent(data.data.point_name ?: "")
                viewModel.onDescriptionChangeEvent(data.data.description ?: "")
                binding.iv.loadImage(
                    data.data.base_url + data.data.image,
                    R.drawable.place_holder_square,
                    R.drawable.place_holder_square
                )
            }

            null -> {
                if (isFishLogOnly) {
                    viewModel.onRadioButtonChangeEvent(binding.rbFishlog.id)
                    binding.tfWeight.viewVisible()
                    binding.tfDate.viewVisible()
                    binding.tfLengthFeet.viewVisible()
                    binding.tfLengthInches.viewVisible()
                    binding.tfTime.viewVisible()
                    binding.tvSave.viewGone()
                    binding.radioGroup.viewGone()
                    binding.buttonSave.text = resources.getString(R.string.save_fish_log)
                } else {
                    viewModel.onRadioButtonChangeEvent(binding.rbLocation.id)
                    binding.tfWeight.viewGone()
                    binding.tfDate.viewGone()
                    binding.tfLengthFeet.viewGone()
                    binding.tfLengthInches.viewGone()
                    binding.tfTime.viewGone()
                    binding.buttonSave.text = resources.getString(R.string.save_location)
                }
            }

            else -> {}
        }
        if (lat != null) {
            viewModel.onLatChangeEvent(lat.toString())
            viewModel.onLatFormatChangeEvent(getNauticalLatitude(lat))
        }
        if (long != null) {
            viewModel.onLangChangeEvent(long.toString())
            viewModel.onLangFormatChangeEvent(getNauticalLongitude(long))
        }
        if(disableEditing){
            binding.etName.isEnabled = false
            binding.etLat.isEnabled = false
            binding.etLang.isEnabled = false
            binding.etDate.isEnabled = false
            binding.etTime.isEnabled = false
            binding.etWeight.isEnabled = false
            binding.etLengthFeet.isEnabled = false
            binding.etLengthInches.isEnabled = false
            binding.etDescription.isEnabled = false
            binding.buttonSave.viewGone()
            binding.ivAddImage.viewGone()
        }
    }

    private fun initViewModelResponse() {
        lifecycleScope.launch {
            viewModel.locationUIStates
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    showHideError(
                        resources.getString(R.string.error_location_name),
                        binding.tfName,
                        !response.nameError
                    )
                    showHideError(
                        resources.getString(R.string.error_location_lat),
                        binding.tfLat,
                        !response.latError
                    )
                    showHideError(
                        resources.getString(R.string.error_location_lang),
                        binding.tfLang,
                        !response.langError
                    )
                    showHideError(
                        resources.getString(R.string.error_fish_date),
                        binding.tfDate,
                        !response.dateError
                    )
                    showHideError(
                        resources.getString(R.string.error_fish_time),
                        binding.tfTime,
                        !response.timeError
                    )
                    showHideError(
                        resources.getString(R.string.error_fish_weight),
                        binding.tfWeight,
                        !response.weightError
                    )
                    showHideError(
                        resources.getString(R.string.error_fish_length),
                        binding.tfLengthFeet,
                        !response.lengthError
                    )
                    showHideError(
                        resources.getString(R.string.error_fish_length),
                        binding.tfLengthInches,
                        !response.lengthError
                    )
                    showHideError(
                        resources.getString(R.string.error_location_description),
                        binding.tfDescription,
                        !response.descriptionError
                    )
                }
        }
        lifecycleScope.launch {
            viewModel.savePointResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true) {
                                requireActivity().showToast(
                                    response.data.message, true
                                )
                                if (data != null && data is MapUiData.LocationData) {
                                    data.data.point_name = response.data.data?.point_name
                                    data.data.description = response.data.data?.description
                                    data.data.date = response.data.data?.date ?: ""
                                    data.data.time = response.data.data?.time ?: ""
                                    data.data.image = response.data.data?.image
                                    data.data.latitude = response.data.data?.latitude
                                    data.data.longitude = response.data.data?.longitude
                                    data.data.type = response.data.data?.type
                                }
                                listener?.onSave()
                                dismiss()
                            } else {
                                dialog?.showToast(
                                    requireContext(),
                                    response.data?.message ?: resources.getString(
                                        R.string.something_went_wrong
                                    ), false
                                )
                                viewModel.resetResponse()
                            }
                        }

                        is NetworkResult.Error -> {
                            showHideLoader(false)
                            dialog?.showToast(
                                requireContext(),
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
                            dialog?.showToast(
                                requireContext(),
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
            viewModel.saveFishLogResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true) {
                                requireActivity().showToast(
                                    response.data.message, true
                                )
                                if (data != null && data is MapUiData.FishLogData) {
                                    data.data.fish_name = response.data.data?.fish_name
                                    data.data.description = response.data.data?.description
                                    data.data.date = response.data.data?.date ?: ""
                                    data.data.time = response.data.data?.time ?: ""
                                    data.data.image = response.data.data?.image
                                    data.data.latitude = response.data.data?.latitude
                                    data.data.longitude = response.data.data?.longitude
                                    data.data.type = response.data.data?.type
                                    data.data.length = response.data.data?.length
                                    data.data.weight = response.data.data?.weight
                                }
                                listener?.onSave()
                                dismiss()
                            } else {
                                dialog?.showToast(
                                    requireContext(),
                                    response.data?.message ?: resources.getString(
                                        R.string.something_went_wrong
                                    ), false
                                )
                                viewModel.resetResponse()
                            }
                        }

                        is NetworkResult.Error -> {
                            showHideLoader(false)
                            dialog?.showToast(
                                requireContext(),
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
                            dialog?.showToast(
                                requireContext(),
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
    }

    private fun showHideLoader(visibility: Boolean) {
        if (visibility) {
            binding.loaderLayout.viewVisible()
        } else {
            binding.loaderLayout.viewGone()
        }
    }

    private fun showHideError(error: String, tf: TextInputLayout, showError: Boolean) {
        if (showError) {
            tf.error = error
        } else {
            tf.error = null
        }
    }

    private fun initListeners() {
        binding.etLat.transformIntoLatPicker(childFragmentManager, isLongitude = false)
        binding.etLang.transformIntoLatPicker(childFragmentManager, isLongitude = true)
        binding.etDate.transformIntoDatePicker(requireContext(), "yyyy-MM-dd", childFragmentManager)
        binding.etTime.transformIntoTimePicker(requireContext(), childFragmentManager, Date())
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            // Responds to child RadioButton checked/unchecked
            when (checkedId) {
                binding.rbLocation.id -> {
                    viewModel.onRadioButtonChangeEvent(binding.rbLocation.id)
                    binding.buttonSave.text = resources.getString(R.string.save_location)
                    binding.tfName.hint = resources.getString(R.string.location_name)
                    binding.tfWeight.viewGone()
                    binding.tfDate.viewGone()
                    binding.tfLengthFeet.viewGone()
                    binding.tfLengthInches.viewGone()
                    binding.tfTime.viewGone()
                }

                binding.rbFishlog.id -> {
                    binding.buttonSave.text = resources.getString(R.string.save_fish_log)
                    binding.tfName.hint = resources.getString(R.string.fish_name)
                    viewModel.onRadioButtonChangeEvent(binding.rbFishlog.id)
                    binding.tfWeight.viewVisible()
                    binding.tfDate.viewVisible()
                    binding.tfLengthFeet.viewVisible()
                    binding.tfLengthInches.viewVisible()
                    binding.tfTime.viewVisible()
                }
            }
        }
        binding.ivAddImage.setOnSingleClickListener {
            imagePickerAvatar()
        }
        binding.ivCrossLocation.setOnClickListener {
            dismiss()
        }
    }

    private fun imagePickerAvatar() {
        requireActivity().selectImage(
            childFragmentManager,
            activityLauncher,
            object : SelectImageListener {
                override fun onImageSelect(path: String?) {
                    if (path.isNullOrEmpty()) {
                        return
                    }
                    binding.iv.loadImage(
                        path,
                        R.drawable.place_holder_square,
                        R.drawable.place_holder_square
                    )
                    viewModel.onImageChangeEvent(path)
                }

                override fun onImageCancel() {
                }

            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    interface OnClickListener {
        fun onSave()
    }

}