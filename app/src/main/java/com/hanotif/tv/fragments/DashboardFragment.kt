package com.hanotif.tv.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.hanotif.tv.R
import com.hanotif.tv.databinding.FragmentDashboardBinding
import com.hanotif.tv.model.CameraStream
import com.hanotif.tv.model.FrigateEvent
import com.hanotif.tv.network.FrigateClient
import com.hanotif.tv.notification.NotificationHelper
import com.hanotif.tv.util.Constants
import com.hanotif.tv.util.PrefsManager
import com.hanotif.tv.view.CameraAdapter
import com.hanotif.tv.view.EventAdapter
import java.util.concurrent.Executors

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: PrefsManager
    private lateinit var notificationHelper: NotificationHelper
    private val executor = Executors.newSingleThreadExecutor()

    private lateinit var cameraAdapter: CameraAdapter
    private lateinit var eventAdapter: EventAdapter

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val title = intent.getStringExtra(Constants.EXTRA_TITLE) ?: return
            val message = intent.getStringExtra(Constants.EXTRA_MESSAGE) ?: ""
            val snapshotUrl = intent.getStringExtra(Constants.EXTRA_SNAPSHOT_URL)
            val streamUrl = intent.getStringExtra(Constants.EXTRA_STREAM_URL)

            notificationHelper.showOverlay(
                title = title,
                message = message,
                snapshotUrl = snapshotUrl,
                streamUrl = streamUrl,
                onOpenStream = {
                    if (!streamUrl.isNullOrBlank()) {
                        openStream(streamUrl, title)
                    }
                }
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PrefsManager(requireContext())
        notificationHelper = NotificationHelper(requireContext())

        setupCameraGrid()
        setupEventList()
        setupButtons()
        loadData()
    }

    override fun onStart() {
        super.onStart()
        ContextCompat.registerReceiver(
            requireContext(),
            notificationReceiver,
            IntentFilter(Constants.ACTION_SHOW_NOTIFICATION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(notificationReceiver)
    }

    private fun setupCameraGrid() {
        cameraAdapter = CameraAdapter { camera ->
            openStream(camera.rtspUrl, camera.name)
        }
        binding.rvCameras.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = cameraAdapter
        }
    }

    private fun setupEventList() {
        eventAdapter = EventAdapter { event ->
            val frigateUrl = prefs.frigateUrl
            if (frigateUrl.isNotBlank()) {
                val snapshotUrl = "$frigateUrl/api/events/${event.id}/snapshot.jpg"
                openSnapshot(event, snapshotUrl)
            }
        }
        binding.rvEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventAdapter
        }
    }

    private fun setupButtons() {
        binding.btnSettings.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, SettingsFragment(), Constants.TAG_SETTINGS)
                .addToBackStack(Constants.TAG_SETTINGS)
                .commit()
        }

        binding.btnRefresh.setOnClickListener {
            loadData()
        }
    }

    private fun loadData() {
        // Load cameras from prefs
        val cameras = prefs.cameras
        updateCameraGrid(cameras)

        // Load Frigate events in background
        val frigateUrl = prefs.frigateUrl
        if (frigateUrl.isNotBlank()) {
            binding.progressEvents.visibility = View.VISIBLE
            executor.execute {
                val frigateClient = FrigateClient(frigateUrl)
                val events = frigateClient.getEvents(limit = 20)
                binding.rvEvents.post {
                    binding.progressEvents.visibility = View.GONE
                    updateEventList(events)
                }
            }
        }

        // Show empty state if not configured
        if (!prefs.isConfigured()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.tvEmptyState.text = getString(R.string.configure_app_message)
        } else {
            binding.tvEmptyState.visibility = View.GONE
        }
    }

    private fun updateCameraGrid(cameras: List<CameraStream>) {
        cameraAdapter.submitList(cameras)
        binding.tvNoCameras.visibility = if (cameras.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateEventList(events: List<FrigateEvent>) {
        eventAdapter.submitList(events)
        binding.tvNoEvents.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openStream(rtspUrl: String, cameraName: String) {
        val fragment = StreamFragment.newInstance(rtspUrl, cameraName)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment, Constants.TAG_STREAM)
            .addToBackStack(Constants.TAG_STREAM)
            .commit()
    }

    private fun openSnapshot(event: FrigateEvent, snapshotUrl: String) {
        val frigateUrl = prefs.frigateUrl
        val streamUrl = prefs.cameras.find { it.name == event.camera }?.rtspUrl

        notificationHelper.showOverlay(
            title = "${event.label.replaceFirstChar { it.uppercase() }} - ${event.camera}",
            message = getString(R.string.score_format, (event.topScore * 100).toInt()),
            snapshotUrl = snapshotUrl,
            streamUrl = streamUrl,
            onOpenStream = {
                streamUrl?.let { openStream(it, event.camera) }
                    ?: Toast.makeText(
                        requireContext(),
                        R.string.no_stream_configured,
                        Toast.LENGTH_SHORT
                    ).show()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        notificationHelper.dismissOverlay()
        _binding = null
    }
}
