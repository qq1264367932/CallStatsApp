package com.callstats.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.callstats.app.databinding.ItemDailyStatsBinding

class DailyStatsAdapter : RecyclerView.Adapter<DailyStatsAdapter.ViewHolder>() {

    private var statsList = listOf<DailyCallStat>()

    fun updateData(newStats: List<DailyCallStat>) {
        statsList = newStats
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDailyStatsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(statsList[position])
    }

    override fun getItemCount(): Int = statsList.size

    class ViewHolder(
        private val binding: ItemDailyStatsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(stat: DailyCallStat) {
            binding.tvDate.text = stat.date
            binding.tvCallCount.text = "通话次数: ${stat.callCount}"
            binding.tvDuration.text = "总时长: ${stat.getFormattedDuration()}"
            binding.tvOutgoing.text = "呼出: ${stat.outgoingCount}"
            binding.tvIncoming.text = "呼入: ${stat.incomingCount}"
        }
    }
}
