package com.ajhavery.weatherapp

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.ajhavery.weatherapp.databinding.ActivityMainBinding
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.*
import org.json.JSONObject
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private var API_KEY: String = "2794825846f665fa1dc2f66f61780a85"
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val lat = intent.getStringExtra("lat")
        val long = intent.getStringExtra("long")

        // set status bar color same as our window color
        window.statusBarColor = Color.parseColor("#FF1383C3")
        getJsonData(lat, long, API_KEY)
    }

    private fun getJsonData(lat: String?, long: String?, apiKey: String) {
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${long}&appid=${apiKey}"
        val cache = DiskBasedCache(cacheDir, 1024 * 1024) // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        val network = BasicNetwork(HurlStack())
        // Instantiate the RequestQueue with the cache and network. Start the queue.
        val requestQueue = RequestQueue(cache, network).apply {
            start()
        }

        // Formulate the request and handle the response.
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            {
                response -> setValues(response)
            },
            {
                error -> Toast.makeText(this, "Error: "+error, Toast.LENGTH_LONG).show()
            }
        )
        // Add the request to the RequestQueue.
        requestQueue.add(jsonObjectRequest)
    }

    private fun setValues(response: JSONObject) {
        binding.city.text = response.getString("name")
        var lat = "Lat: " + response.getJSONObject("coord").getString("lat")
        var long = "Long: " + response.getJSONObject("coord").getString("lon")
        binding.coordinates.text = "$lat, $long"
        binding.weather.text = response.getJSONArray("weather").getJSONObject(0).getString("main")
        var temperature = response.getJSONObject("main").getString("temp")
        var minTemp = response.getJSONObject("main").getString("temp_min")
        var maxTemp = response.getJSONObject("main").getString("temp_max")
        binding.temp.text = tempInC(temperature, "Temp")
        binding.minTemp.text = tempInC(minTemp, "Min")
        binding.maxTemp.text = tempInC(maxTemp, "Max")
        var pressure = response.getJSONObject("main").getString("pressure")
        var pressureInAtm = ((((pressure.toFloat())*1000 / 1013.25).roundToInt().toDouble())/1000).toString() + " atm"
        binding.pressure.text = pressureInAtm
        binding.humidity.text = response.getJSONObject("main").getString("humidity") + "%"
        binding.wind.text = response.getJSONObject("wind").getString("speed")
        binding.degree.text = "Degree: "+ response.getJSONObject("wind").getString("deg") + "°"

    }

    public fun tempInC(temp:String, head: String): String {
        return head + ": " + ((temp.toFloat() - 273.15).toInt()).toString() + "°C"
    }
}