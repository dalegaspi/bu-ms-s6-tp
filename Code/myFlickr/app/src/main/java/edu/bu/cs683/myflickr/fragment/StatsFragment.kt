package edu.bu.cs683.myflickr.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.flickr4java.flickr.stats.Totals
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import edu.bu.cs683.myflickr.MyFlickrApplication
import edu.bu.cs683.myflickr.data.FlickrRepository
import edu.bu.cs683.myflickr.data.Photo
import edu.bu.cs683.myflickr.data.PhotoRepository
import edu.bu.cs683.myflickr.databinding.FragmentStatsBinding
import kotlinx.coroutines.*

/**
 * Stats fragment
 *
 * @author dlegaspi@bu.edu
 */
class StatsFragment : Fragment() {
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private lateinit var flickrRepository: FlickrRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        flickrRepository = (activity?.application as MyFlickrApplication).flickrRepository
    }

    suspend fun getDataSet(): List<BarEntry> {
        return CoroutineScope(Dispatchers.IO).async {
            val totals = flickrRepository.getViewStats()
            val data = listOf(
                BarEntry(1f, totals!!.photos.toFloat()),
                BarEntry(2f, totals!!.collections.toFloat()),
                BarEntry(3f, totals!!.photostream.toFloat()),
                BarEntry(4f, totals!!.sets.toFloat()),
                BarEntry(5f, totals!!.total.toFloat()),
            )

            return@async data
        }.await()
    }

    fun getAxis(): List<String> {
        return listOf("Photos", "Collections", "Photostream", "Sets", "Total")
    }

    fun drawBarChart() {
        CoroutineScope(Dispatchers.Main).launch {
            val barChart = binding.barChart
            val barDataSet = BarDataSet(getDataSet(), "Views")

            barChart.data = BarData(barDataSet)
            barChart.xAxis.setPosition(XAxis.XAxisPosition.BOTTOM)
            barChart.axisLeft.setDrawGridLines(false)
            barChart.axisLeft.granularity = 1f
            barChart.xAxis.labelCount = 5
            barChart.xAxis.setValueFormatter { value, a ->
                  getAxis()[value.toInt() - 1]
            }
            barChart.xAxis.setDrawGridLines(false)
            barChart.xAxis.setDrawAxisLine(false)

            // remove right y-axis
            barChart.axisRight.isEnabled = false
            barChart.description.isEnabled = false
            barDataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
            // add animation
            barChart.animateY(500)

            // draw bar chart
            barChart.invalidate()
        }
    }

    suspend fun getPieChartData(): List<PieEntry> {
        return CoroutineScope(Dispatchers.IO).async {
            val data = PhotoRepository.get().getCameraBreakdown().map {
                PieEntry(it.counts.toFloat(), it.camera)
            }

            return@async data
        }.await()
    }
    private fun drawPieChart() {
        val pieChart = binding.pieChart

        pieChart.setUsePercentValues(true)
        pieChart.description.text = ""
        // hollow pie chart
        pieChart.isDrawHoleEnabled = false
        pieChart.setTouchEnabled(false)
        pieChart.setDrawEntryLabels(false)
        // adding padding
        pieChart.setExtraOffsets(20f, 0f, 20f, 20f)
        pieChart.setUsePercentValues(true)
        pieChart.isRotationEnabled = false
        pieChart.setDrawEntryLabels(false)
        pieChart.legend.orientation = Legend.LegendOrientation.VERTICAL
        pieChart.legend.isWordWrapEnabled = true

        CoroutineScope(Dispatchers.Main).launch {
            val dataEntries = getPieChartData()

            val dataSet = PieDataSet(dataEntries, "Cameras")
            val data = PieData(dataSet)
            data.setValueFormatter(PercentFormatter())
            dataSet.sliceSpace = 3f
            dataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
            pieChart.data = data
            data.setValueTextSize(15f)
            pieChart.setExtraOffsets(5f, 10f, 5f, 5f)

            // create hole in center
            pieChart.holeRadius = 58f
            pieChart.transparentCircleRadius = 61f
            pieChart.isDrawHoleEnabled = true
            pieChart.setHoleColor(Color.WHITE)

            // add text in center
            pieChart.setDrawCenterText(true)
            pieChart.centerText = "Camera Brand Breakdown"

            pieChart.invalidate()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_stats, container, false)

        _binding = FragmentStatsBinding.inflate(inflater, container, false)

        drawBarChart()
        drawPieChart()

        return binding.root
    }

    companion object {
    }
}
