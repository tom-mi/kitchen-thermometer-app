package de.rfnbrgr.kitchenheatseeker

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_discovery.*

class Discovery : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discovery)

    }

    fun connect(view: View) {
        val intent = Intent(this, HeatDisplay::class.java)
        intent.putExtra(HeatDisplay.HOSTNAME, editHostname.text.toString())
        startActivity(intent)
    }
}
