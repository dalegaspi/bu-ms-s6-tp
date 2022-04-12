package edu.bu.cs683.myflickr.fragment

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import edu.bu.cs683.myflickr.R
import edu.bu.cs683.myflickr.databinding.FragmentImageGridBinding
import edu.bu.cs683.myflickr.databinding.FragmentStatsBinding

/**
 * Stats fragment
 *
 * @author dlegaspi@bu.edu
 */
class StatsFragment : Fragment() {
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    fun getDataSet(): List<BarEntry> {
        val data = listOf(BarEntry(1f, 30f),
            BarEntry(2f, 15f),
            BarEntry(3f, 20f),
            BarEntry(4f, 15f),
            BarEntry(5f, 30f),
            BarEntry(6f, 20f)
        )

        return data
    }

    fun getAxis(): List<String> {
        return listOf("Jan", "Feb", "Mar")
    }

    fun drawBarChart() {
        val barChart = binding.barChart
        val barDataSet = BarDataSet(getDataSet(), "Views")


        barChart.data = BarData(barDataSet)
        //hide grid lines
        barChart.axisLeft.setDrawGridLines(false)
        barChart.xAxis.setDrawGridLines(false)
        barChart.xAxis.setDrawAxisLine(false)

        //remove right y-axis
        barChart.axisRight.isEnabled = false

        barChart.description.isEnabled = false
        barDataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
        //add animation
        barChart.animateY(500)

        //draw bar chart
        barChart.invalidate()
    }

    fun drawPieChart() {
        val pieChart = binding.pieChart

        pieChart.setUsePercentValues(true)
        pieChart.description.text = ""
        //hollow pie chart
        pieChart.isDrawHoleEnabled = false
        pieChart.setTouchEnabled(false)
        pieChart.setDrawEntryLabels(false)
        //adding padding
        pieChart.setExtraOffsets(20f, 0f, 20f, 20f)
        pieChart.setUsePercentValues(true)
        pieChart.isRotationEnabled = false
        pieChart.setDrawEntryLabels(false)
        pieChart.legend.orientation = Legend.LegendOrientation.VERTICAL
        pieChart.legend.isWordWrapEnabled = true
        val dataEntries = ArrayList<PieEntry>()
        dataEntries.add(PieEntry(72f, "Nikon"))
        dataEntries.add(PieEntry(26f, "Leica"))
        dataEntries.add(PieEntry(2f, "Other"))

        val colors: ArrayList<Int> = ArrayList()
        colors.add(Color.parseColor("#4DD0E1"))
        colors.add(Color.parseColor("#FFF176"))
        colors.add(Color.parseColor("#FF8A65"))
        val dataSet = PieDataSet(dataEntries, "")
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        dataSet.sliceSpace = 3f
        dataSet.colors = colors
        pieChart.data = data
        data.setValueTextSize(15f)
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)
        //pieChart.animateY(1400, Easing.)

        //create hole in center
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)


        //add text in center
        pieChart.setDrawCenterText(true);
        pieChart.centerText = "Camera Brand Breakdown"

        pieChart.invalidate()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
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