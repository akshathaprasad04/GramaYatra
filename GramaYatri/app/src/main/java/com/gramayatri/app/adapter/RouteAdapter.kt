package com.gramayatri.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gramayatri.app.databinding.ItemRouteBinding
import com.gramayatri.app.model.BusRoute

class RouteAdapter(private var routes: List<BusRoute>) :
    RecyclerView.Adapter<RouteAdapter.RouteVH>() {

    inner class RouteVH(val binding: ItemRouteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RouteVH(ItemRouteBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RouteVH, position: Int) {
        val route = routes[position]
        with(holder.binding) {
            tvRouteName.text = route.routeName
            tvFromTo.text    = "🏠 ${route.fromVillage}  →  ${route.toVillage}"
            tvDeparture.text = "🕐 Departure: ${route.departureTime}"
            tvStops.text     = "📍 ${route.stops.joinToString(" → ")}"
            tvStopCount.text = "${route.stops.size} stops"
            val status = if (route.isActive) "Active" else "Inactive"
            tvStatus.text = status
            tvStatus.setBackgroundResource(
                if (route.isActive) com.gramayatri.app.R.drawable.badge_active
                else com.gramayatri.app.R.drawable.badge_inactive
            )
        }
    }

    override fun getItemCount() = routes.size

    fun updateRoutes(newRoutes: List<BusRoute>) {
        routes = newRoutes
        notifyDataSetChanged()
    }
}
