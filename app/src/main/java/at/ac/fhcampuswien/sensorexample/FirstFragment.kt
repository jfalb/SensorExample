package at.ac.fhcampuswien.sensorexample

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import at.ac.fhcampuswien.sensorexample.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var mService: LocalService
    private var mBound = false

    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as LocalService.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val intent = Intent(requireActivity(), LocalService::class.java)
        requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.buttonService1.setOnClickListener {
            val intent = Intent(requireActivity(), HelloService::class.java)
            requireActivity().startService(intent)
        }

        binding.buttonService2.setOnClickListener {
            if (mBound) {
                Toast.makeText(
                    requireContext(),
                    "Number: ${mService.randomNumber}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
        deviceSensors.forEach { sensor -> Log.i("SensorExample", "${sensor.id}, ${sensor.name}, ${sensor.vendor}, ${sensor}") }

        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!
    }

    override fun onDestroyView() {
        super.onDestroyView()

        requireActivity().unbindService(connection)

        _binding = null
    }

    override fun onResume() {
        // Register a listener for the sensor.
        super.onResume()
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun onPause() {
        // Be sure to unregister the sensor when the activity pauses.
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            binding.root.setBackgroundColor(
                Color.HSVToColor(floatArrayOf(event.values[0] / sensor.maximumRange * 360.0f, 1.0f, 1.0f))
            )
            Log.i("SensorExample", "Value: ${event.values[0]}")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}