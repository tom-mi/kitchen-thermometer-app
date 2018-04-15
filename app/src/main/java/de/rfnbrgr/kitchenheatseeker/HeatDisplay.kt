package de.rfnbrgr.kitchenheatseeker

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_heat_display.*

class HeatDisplay : AppCompatActivity() {

    companion object {
        const val HOSTNAME = "hostname"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heat_display)
        val hostname = intent.getStringExtra(HOSTNAME)
        textHostname.text = hostname
    }

}
