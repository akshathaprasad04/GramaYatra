package com.gramayatri.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gramayatri.app.databinding.ItemAdminRouteBinding
import com.gramayatri.app.model.BusRoute

class AdminRouteAdapter(
    private var routes: List<BusRoute>,
    private val onDelete: (BusRoute) -> Unit
) : RecyclerView.Adapter<AdminRouteAdapter.VH>() {

    inner class VH(val binding: ItemAdminRouteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemAdminRouteBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val route = routes[position]
        with(holder.binding) {
            tvRouteName.text  = route.routeName
            tvFromTo.text     = "${route.fromVillage} → ${route.toVillage}"
            tvStopCount.text  = "${route.stops.size} stops"
            tvDeparture.text  = route.departureTime
            btnDelete.setOnClickListener { onDelete(route) }
        }
    }

    override fun getItemCount() = routes.size

    fun updateRoutes(newRoutes: List<BusRoute>) {
        routes = newRoutes
        notifyDataSetChanged()
    }
}
