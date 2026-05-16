package com.gramayatri.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gramayatri.app.databinding.ItemPingFeedBinding
import com.gramayatri.app.model.BusPing
import com.gramayatri.app.model.StopEta
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PingFeedAdapter(private val pings: MutableList<BusPing>) :
    RecyclerView.Adapter<PingFeedAdapter.PingVH>() {

    inner class PingVH(val binding: ItemPingFeedBinding) : RecyclerView.ViewHolder(binding.root)

    private val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PingVH(ItemPingFeedBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: PingVH, position: Int) {
        val ping = pings[position]
        with(holder.binding) {
            tvPingType.text    = ping.pingType
            tvLocation.text    = "📍 ${ping.currentStop}"
            tvUser.text        = "👤 ${ping.userName}"
            tvTime.text        = sdf.format(Date(ping.timestamp))

            // Show nearest upcoming stop ETA
            val nextEta = ping.etaMap.entries
                .filter { it.value > System.currentTimeMillis() }
                .minByOrNull { it.value }
            if (nextEta != null) {
                val eta = StopEta(nextEta.key, nextEta.value)
                tvNextStop.text = "Next: ${nextEta.key} in ${eta.etaFormatted()}"
                tvNextStop.visibility = android.view.View.VISIBLE
            } else {
                tvNextStop.visibility = android.view.View.GONE
            }
        }
    }

    override fun getItemCount() = pings.size
}
