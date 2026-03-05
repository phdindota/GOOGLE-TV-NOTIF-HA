package com.hanotif.tv.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.hanotif.tv.R
import com.hanotif.tv.databinding.FragmentSettingsBinding
import com.hanotif.tv.model.CameraStream
import com.hanotif.tv.util.PrefsManager

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: PrefsManager
    private val cameraList = mutableListOf<CameraStream>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PrefsManager(requireContext())

        loadCurrentSettings()
        setupButtons()
    }

    private fun loadCurrentSettings() {
        binding.etHaUrl.setText(prefs.haUrl)
        binding.etHaToken.setText(prefs.haToken)
        binding.etFrigateUrl.setText(prefs.frigateUrl)

        cameraList.clear()
        cameraList.addAll(prefs.cameras)
        updateCameraListDisplay()
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            saveSettings()
        }

        binding.btnAddCamera.setOnClickListener {
            addCamera()
        }

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun saveSettings() {
        val haUrl = binding.etHaUrl.text?.toString()?.trim() ?: ""
        val haToken = binding.etHaToken.text?.toString()?.trim() ?: ""
        val frigateUrl = binding.etFrigateUrl.text?.toString()?.trim() ?: ""

        prefs.haUrl = haUrl
        prefs.haToken = haToken
        prefs.frigateUrl = frigateUrl
        prefs.cameras = cameraList.toList()

        Toast.makeText(requireContext(), R.string.settings_saved, Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }

    private fun addCamera() {
        val name = binding.etCameraName.text?.toString()?.trim() ?: ""
        val rtspUrl = binding.etCameraRtspUrl.text?.toString()?.trim() ?: ""

        if (name.isBlank() || rtspUrl.isBlank()) {
            Toast.makeText(requireContext(), R.string.camera_fields_required, Toast.LENGTH_SHORT).show()
            return
        }

        cameraList.add(CameraStream(name = name, rtspUrl = rtspUrl))
        binding.etCameraName.text?.clear()
        binding.etCameraRtspUrl.text?.clear()
        updateCameraListDisplay()
    }

    private fun updateCameraListDisplay() {
        binding.tvCameraList.text = if (cameraList.isEmpty()) {
            getString(R.string.no_cameras_added)
        } else {
            cameraList.joinToString("\n") { "• ${it.name}: ${it.rtspUrl}" }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
