package de.rfnbrgr.kitchenthermometer

import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
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
                setBatteryStatus(frame)
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

    private var clientService: DeviceClientService? = null

    private var serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            clientService = (service as DeviceClientService.DeviceClientServiceBinder).getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            clientService = null
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        PreferenceManager.setDefaultValues(this, R.xml.alarm_preferences, false)
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
            clientService?.restartDeviceClientThread()
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

        bindService(Intent(this, DeviceClientService::class.java), serviceConnection,
                Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        unbindService(serviceConnection)
    }

    override fun onResume() {
        super.onResume()
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val hostname = sharedPref.getString(getString(R.string.pref_hostname), "")
        if (hostname == "") {
            showSetHostnameDialog()
        } else {
            clientService?.restartDeviceClientThread()
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
        val fixedTemperatureRangeMin = getFloatPreference(this, getString(R.string.pref_temperature_range_min))
        val fixedTemperatureRangeMax = getFloatPreference(this, getString(R.string.pref_temperature_range_max))
        heatmap.fixedTemperatureRange = Pair(fixedTemperatureRangeMin, fixedTemperatureRangeMax)
    }

    private fun setBatteryStatus(frame: EnrichedHeatFrame) {
        textBatteryStatus.text = String.format(getString(R.string.battery_template), frame.battery * 100f, frame.batteryVoltage)
    }

}
