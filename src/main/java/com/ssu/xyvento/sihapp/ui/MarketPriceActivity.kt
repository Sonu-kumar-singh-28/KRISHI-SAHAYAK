package com.ssu.xyvento.sihapp.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssu.xyvento.sihapp.adapter.MarketPriceAdapter
import com.ssu.xyvento.sihapp.databinding.ActivityMarketPriceBinding
import com.ssu.xyvento.sihapp.dataclass.MarketPrice
import com.ssu.xyvento.sihapp.dataclass.MarketPriceResponse
import com.ssu.xyvento.sihapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MarketPriceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMarketPriceBinding
    private val API_KEY = "579b464db66ec23bdd000001193bab90f7bb4a58457c7e8803a6a665"

    private lateinit var adapter: MarketPriceAdapter
    private val marketData = mutableListOf<MarketPrice>()

    private val crops = listOf(
        "Wheat", "Rice", "Maize", "Barley", "Gram", "Tur", "Moong", "Urad",
        "Soybean", "Potato", "Onion", "Tomato", "Cotton", "Sugarcane"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarketPriceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecycler()
        setupCropSpinner()
    }

    private fun setupRecycler() {
        adapter = MarketPriceAdapter(marketData)
        binding.rvMarketPrice.layoutManager = LinearLayoutManager(this)
        binding.rvMarketPrice.adapter = adapter
    }

    private fun setupCropSpinner() {
        val spinnerAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, crops)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCrop.adapter = spinnerAdapter

        binding.spinnerCrop.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    fetchMarketData(crops[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        // default
        binding.spinnerCrop.setSelection(0)
    }

    private fun fetchMarketData(crop: String) {
        binding.progressBar.visibility = View.VISIBLE
        marketData.clear()
        adapter.updateList(marketData)

        RetrofitClient.marketApi.getMarketPrices(
            apiKey = API_KEY,
            commodity = crop,
            state = null,
            limit = 200
        ).enqueue(object : Callback<MarketPriceResponse> {

            override fun onResponse(
                call: Call<MarketPriceResponse>,
                response: Response<MarketPriceResponse>
            ) {
                binding.progressBar.visibility = View.GONE

                val data = response.body()?.records ?: emptyList()
                if (data.isEmpty()) {
                    Toast.makeText(
                        this@MarketPriceActivity,
                        "No data found for $crop",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    marketData.addAll(data)
                    adapter.updateList(marketData)
                }
            }

            override fun onFailure(call: Call<MarketPriceResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this@MarketPriceActivity,
                    "API Error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}
