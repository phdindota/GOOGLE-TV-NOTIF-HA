package com.hanotif.tv.fragments

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.media3.ui.PlayerView
import com.hanotif.tv.databinding.FragmentStreamBinding
import com.hanotif.tv.player.RTSPPlayerManager
import com.hanotif.tv.util.Constants

class StreamFragment : Fragment() {

    private var _binding: FragmentStreamBinding? = null
    private val binding get() = _binding!!

    private lateinit var playerManager: RTSPPlayerManager
    private var streamUrl: String = ""
    private var cameraName: String = ""

    companion object {
        fun newInstance(streamUrl: String, cameraName: String): StreamFragment {
            return StreamFragment().apply {
                arguments = Bundle().apply {
                    putString(Constants.EXTRA_STREAM_URL, streamUrl)
                    putString(Constants.EXTRA_CAMERA_NAME, cameraName)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        streamUrl = arguments?.getString(Constants.EXTRA_STREAM_URL) ?: ""
        cameraName = arguments?.getString(Constants.EXTRA_CAMERA_NAME) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStreamBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playerManager = RTSPPlayerManager(requireContext())

        binding.tvCameraName.text = cameraName

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.root.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                parentFragmentManager.popBackStack()
                true
            } else {
                false
            }
        }

        if (streamUrl.isNotBlank()) {
            playerManager.play(streamUrl, binding.playerView)
        }
    }

    override fun onResume() {
        super.onResume()
        playerManager.resume()
    }

    override fun onPause() {
        super.onPause()
        playerManager.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        playerManager.release()
        _binding = null
    }
}
