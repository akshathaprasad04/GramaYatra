package com.gramayatri.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gramayatri.app.databinding.ItemAdminPingBinding
import com.gramayatri.app.model.BusPing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminPingAdapter(
    private var pings: List<BusPing>,
    private val onDelete: (BusPing) -> Unit
) : RecyclerView.Adapter<AdminPingAdapter.VH>() {

    inner class VH(val binding: ItemAdminPingBinding) : RecyclerView.ViewHolder(binding.root)
    private val sdf = SimpleDateFormat("dd MMM hh:mm a", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemAdminPingBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ping = pings[position]
        with(holder.binding) {
            tvUser.text     = "👤 ${ping.userName}"
            tvType.text     = ping.pingType
            tvStop.text     = "📍 ${ping.currentStop}"
            tvTime.text     = sdf.format(Date(ping.timestamp))
            btnRemove.setOnClickListener { onDelete(ping) }
        }
    }

    override fun getItemCount() = pings.size

    fun updatePings(newPings: List<BusPing>) {
        pings = newPings
        notifyDataSetChanged()
    }
}
