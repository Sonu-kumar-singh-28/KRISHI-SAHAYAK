package com.ssu.xyvento.sihapp.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssu.xyvento.sihapp.R
import com.ssu.xyvento.sihapp.adapter.SoilFertilizerAdapter
import com.ssu.xyvento.sihapp.databinding.ActivitySoilFertilizerBinding
import com.ssu.xyvento.sihapp.dataclass.SoilAdvisory

class SoilFertilizerActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySoilFertilizerBinding
    private lateinit var adapter: SoilFertilizerAdapter

    private val crops = listOf("Wheat", "Rice", "Maize", "Cotton", "Sugarcane")
    private val soils = listOf("Loamy", "Sandy", "Clay", "Black")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySoilFertilizerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecycler()
        setupSpinners()
    }

    private fun setupRecycler() {
        adapter = SoilFertilizerAdapter(emptyList())
        binding.rvSoilFertilizer.layoutManager = LinearLayoutManager(this)
        binding.rvSoilFertilizer.adapter = adapter
    }

    private fun setupSpinners() {

        val cropAdapter = ArrayAdapter(
            this,
            R.layout.spinner_item_black,
            crops
        )
        cropAdapter.setDropDownViewResource(R.layout.spinner_dropdown_black)
        binding.spinnerCrop.adapter = cropAdapter

        val soilAdapter = ArrayAdapter(
            this,
            R.layout.spinner_item_black,
            soils
        )
        soilAdapter.setDropDownViewResource(R.layout.spinner_dropdown_black)
        binding.spinnerSoil.adapter = soilAdapter

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                loadAdvisory()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerCrop.onItemSelectedListener = listener
        binding.spinnerSoil.onItemSelectedListener = listener
    }

    private fun loadAdvisory() {
        binding.progressBar.visibility = View.VISIBLE

        val crop = binding.spinnerCrop.selectedItem.toString()
        val soil = binding.spinnerSoil.selectedItem.toString()

        val advisory = mutableListOf<SoilAdvisory>()

        advisory.add(
            SoilAdvisory("🌱 $crop grows best in $soil soil with proper drainage.")
        )

        when (soil) {
            "Loamy" -> advisory.add(SoilAdvisory("Use balanced NPK (10:10:10) fertilizer"))
            "Sandy" -> advisory.add(SoilAdvisory("Apply organic manure + nitrogen fertilizer"))
            "Clay" -> advisory.add(SoilAdvisory("Add gypsum and ensure proper irrigation"))
            "Black" -> advisory.add(SoilAdvisory("Use phosphorus rich fertilizer"))
        }

        when (crop) {
            "Wheat" -> advisory.add(SoilAdvisory("Recommended fertilizer: Urea + DAP"))
            "Rice" -> advisory.add(SoilAdvisory("Maintain water level & apply NPK"))
            "Cotton" -> advisory.add(SoilAdvisory("Use potash for better yield"))
        }

        advisory.add(
            SoilAdvisory("🌾 Add organic compost before sowing for best results")
        )

        binding.progressBar.visibility = View.GONE
        adapter.updateData(advisory)
    }
}
