package com.example.assignment




import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.lang.Math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {


    private var magnitudePreviousStep = 0.0
    private var sensorManager: SensorManager? = null
    private var running = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f
    private val ACTIVITY_RECOGNITION_REQUEST_CODE: Int = 100
    var num = 0
    var num2 = 0
    var startPoint = 0
    var endPoint = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (isPermissionGranted()) {
            requestPermission()
        }
        loadData()
        resetSteps()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager


        val age1 = findViewById<Button>(R.id.age1)
        val age2 = findViewById<Button>(R.id.age2)
        var textView = findViewById<TextView>(R.id.age)
        age1.setOnClickListener {
            if (num <= 0) {
                num == 0
                textView.text = num.toString()
            } else {
                num--
                textView.text = num.toString()
            }
        }
        age2.setOnClickListener {
            num++
            textView.text = num.toString()
        }
        val weight1 = findViewById<Button>(R.id.weight1)
        val weight2 = findViewById<Button>(R.id.weight2)
        var textView2 = findViewById<TextView>(R.id.weight)
        weight1.setOnClickListener {
            if (num2 <= 0) {
                num2 == 0
                textView2.text = num2.toString()
            } else {
                num2--
                textView2.text = num2.toString()
            }
        }
        weight2.setOnClickListener {
            num2++
            textView2.text = num2.toString()
        }

        val volumeSeekBar = findViewById<SeekBar>(R.id.seek_bar)
        val volume = findViewById<TextView>(R.id.height_value)
        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                volume.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null) {
                    seekBar.setMax(251)
                    startPoint = seekBar.progress
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null) {

                    endPoint = seekBar.progress
                }
                Toast.makeText(this@MainActivity, "changed height", Toast.LENGTH_LONG).show()
            }
        })
    }



    override fun onResume() {
        super.onResume()
         val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        when {
            countSensor != null -> {
                sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI)
            }
            detectorSensor != null -> {
                sensorManager.registerListener(this, detectorSensor, SensorManager.SENSOR_DELAY_UI)
            }
            accelerometer != null -> {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            }
            else -> {
                Toast.makeText(this, "Your device is not compatible", Toast.LENGTH_LONG).show()
            }
        }

    }

    override fun onSensorChanged(event: SensorEvent?) {
        val steptaken = findViewById<TextView>(R.id.tv_stepsTaken)
        val cirbar = findViewById<com.mikhaellopez.circularprogressbar.CircularProgressBar>(R.id.progress_circular)
        if(event!!.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val xaccel: Float = event.values[0]
            val yaccel: Float = event.values[1]
            val zaccel: Float = event.values[2]
            val magnitude: Double = sqrt((xaccel * xaccel + yaccel * yaccel + zaccel * zaccel).toDouble())

            val magnitudeDelta: Double = magnitude - magnitudePreviousStep
            magnitudePreviousStep = magnitude

            if(magnitudeDelta > 6) {
                totalSteps++
            }
            val step: Int = totalSteps.toInt()
            steptaken.text = step.toString()
            cirbar.apply {
                setProgressWithAnimation(step.toFloat())
            }

        } else {
            if(running) {
                totalSteps = event!!.values[0]
                val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
                steptaken.text = currentSteps.toString()
                cirbar.apply {
                    setProgressWithAnimation(currentSteps.toFloat())
                }
            }
        }

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //
    }

    private fun resetSteps() {
        val tv_stepsTaken = findViewById<TextView>(R.id.tv_stepsTaken)
        tv_stepsTaken.setOnClickListener {
            Toast.makeText(this, "Long tap to reset steps", Toast.LENGTH_SHORT).show()
        }
        tv_stepsTaken.setOnLongClickListener{
            previousTotalSteps = totalSteps
            tv_stepsTaken.text = 0.toString()
            saveData()
            true
        }

    }

    private fun saveData() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("key1", previousTotalSteps)
        editor.apply()

    }
    private fun loadData() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getFloat("key1", 0f)
        Log.d("MainActivity", "$savedNumber")
        previousTotalSteps = savedNumber
    }


    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION), ACTIVITY_RECOGNITION_REQUEST_CODE)
        }
    }

    private fun isPermissionGranted(): Boolean {
    return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            ACTIVITY_RECOGNITION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
            }
        }
    }
}