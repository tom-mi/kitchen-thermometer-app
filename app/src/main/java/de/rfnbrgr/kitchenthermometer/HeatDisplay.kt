package de.rfnbrgr.kitchenthermometer

import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_heat_display.*

class HeatDisplay : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == HEAT_FRAME_ACTION) {
                val frame = intent.getParcelableExtra<EnrichedHeatFrame>(HEAT_FRAME_PAYLOAD)
                heatmap.setFrame(frame)
            }
            if (intent?.action == CLIENT_STATE_ACTION) {
                val state = intent.getSerializableExtra(CLIENT_STATE_PAYLOAD) as DeviceClient.ClientState
                val hostname = intent.getStringExtra(CLIENT_STATE_HOSTNAME) ?: ""
                when (state) {
                    DeviceClient.ClientState.CONNECTING -> textClientStatus.text = getString(R.string.state_connecting, hostname)
                    DeviceClient.ClientState.CONNECTED -> textClientStatus.text = getString(R.string.state_connected, hostname)
                    DeviceClient.ClientState.NOT_CONNECTED -> textClientStatus.text = getString(R.string.state_not_connected)
                    DeviceClient.ClientState.FAILED -> textClientStatus.text = getString(R.string.state_failed, hostname)
                    else -> textClientStatus.text = getString(R.string.state_unknown)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        setContentView(R.layout.activity_heat_display)
        initializeToolbar()
        configureHeatmap()
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPref.registerOnSharedPreferenceChangeListener(this)
    }

    private fun initializeToolbar() {
        setSupportActionBar(toolbar)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }

        R.id.action_reconnect -> {
            startService(Intent(this, DeviceClientService::class.java))
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Log.d(javaClass.simpleName, "Preference for key $key changed")
        if (
                key == getString(R.string.pref_smooth_heatmap) ||
                key == getString(R.string.pref_temperature_scale) ||
                key == getString(R.string.pref_temperature_range_min) ||
                key == getString(R.string.pref_temperature_range_max)
        ) {
            configureHeatmap()
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter()
        filter.addAction(HEAT_FRAME_ACTION)
        filter.addAction(CLIENT_STATE_ACTION)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)

        startService(Intent(this, DeviceClientService::class.java))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        stopService(Intent(this, DeviceClientService::class.java))
    }

    override fun onResume() {
        super.onResume()
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val hostname = sharedPref.getString(getString(R.string.pref_hostname), "")
        if (hostname == "") {
            showSetHostnameDialog()
        }
    }

    private fun showSetHostnameDialog() {
        val builder = AlertDialog.Builder(this)
        builder
                .setTitle(R.string.dialog_hostname_not_set_title)
                .setMessage(R.string.dialog_hostname_not_set_message)
                .setNeutralButton(R.string.ok, { _, _ ->
                    startActivity(Intent(this, SettingsActivity::class.java))
                })
                .setIcon(R.drawable.ic_warning_black_24dp)
                .show()
    }

    private fun configureHeatmap() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        heatmap.colorStops = mapOf(0f to Color.BLUE, 0.25f to Color.GREEN, 0.5f to Color.YELLOW, 1f to Color.RED)
        heatmap.smooth = sharedPref.getBoolean(getString(R.string.pref_smooth_heatmap), false)
        heatmap.temperatureScale = when (sharedPref.getString(getString(R.string.pref_temperature_scale),
                getString(R.string.pref_temperature_scale_value_auto))) {
            getString(R.string.pref_temperature_scale_value_auto) -> TemperatureScale.AUTO
            getString(R.string.pref_temperature_scale_value_full) -> TemperatureScale.FULL
            getString(R.string.pref_temperature_scale_value_fixed) -> TemperatureScale.FIXED
            else -> TemperatureScale.AUTO
        }
        val fixedTemperatureRangeMin = sharedPref.getString(getString(R.string.pref_temperature_range_min), "0").toFloat()
        val fixedTemperatureRangeMax = sharedPref.getString(getString(R.string.pref_temperature_range_max), "100").toFloat()
        heatmap.fixedTemperatureRange = Pair(fixedTemperatureRangeMin, fixedTemperatureRangeMax)
    }
}
