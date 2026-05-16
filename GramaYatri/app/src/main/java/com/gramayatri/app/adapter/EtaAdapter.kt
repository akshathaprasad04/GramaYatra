package com.gramayatri.app.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gramayatri.app.databinding.ItemStopEtaBinding
import com.gramayatri.app.model.StopEta

class EtaAdapter(private var stops: List<StopEta>) :
    RecyclerView.Adapter<EtaAdapter.EtaVH>() {

    inner class EtaVH(val binding: ItemStopEtaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        EtaVH(ItemStopEtaBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: EtaVH, position: Int) {
        val stop = stops[position]
        with(holder.binding) {
            tvStopName.text = stop.stopName
            tvEta.text      = stop.etaFormatted()

            if (stop.isCurrentStop) {
                tvCurrentMarker.visibility = android.view.View.VISIBLE
                root.setCardBackgroundColor(Color.parseColor("#E8F5E9"))
                tvEta.text = "🚌 Bus is here"
            } else {
                tvCurrentMarker.visibility = android.view.View.GONE
                root.setCardBackgroundColor(Color.WHITE)
            }
        }
    }

    override fun getItemCount() = stops.size

    fun updateList(newList: List<StopEta>) {
        stops = newList
        notifyDataSetChanged()
    }
}
