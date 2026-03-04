package com.hanotif.tv

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.hanotif.tv.databinding.ActivityMainBinding
import com.hanotif.tv.fragments.DashboardFragment
import com.hanotif.tv.fragments.StreamFragment
import com.hanotif.tv.util.Constants

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.fragmentContainer, DashboardFragment(), Constants.TAG_DASHBOARD)
            }
        }

        requestOverlayPermissionIfNeeded()
        handleIncomingIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        intent ?: return
        val streamUrl = intent.getStringExtra(Constants.EXTRA_STREAM_URL)
        val cameraName = intent.getStringExtra(Constants.EXTRA_CAMERA_NAME) ?: ""

        if (!streamUrl.isNullOrBlank()) {
            val fragment = StreamFragment.newInstance(streamUrl, cameraName)
            supportFragmentManager.commit {
                replace(R.id.fragmentContainer, fragment, Constants.TAG_STREAM)
                addToBackStack(Constants.TAG_STREAM)
            }
        }
    }

    private fun requestOverlayPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !Settings.canDrawOverlays(this)
        ) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}
