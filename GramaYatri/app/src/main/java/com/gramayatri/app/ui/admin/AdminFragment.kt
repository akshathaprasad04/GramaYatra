package com.gramayatri.app.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ValueEventListener
import com.gramayatri.app.adapter.AdminRouteAdapter
import com.gramayatri.app.adapter.AdminPingAdapter
import com.gramayatri.app.databinding.FragmentAdminBinding
import com.gramayatri.app.model.BusRoute
import com.gramayatri.app.repository.FirebaseRepository

class AdminFragment : Fragment() {

    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!
    private var routeListener: ValueEventListener? = null
    private val pingListeners = mutableMapOf<String, ValueEventListener>()

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentAdminBinding.inflate(inflater, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val routeAdapter = AdminRouteAdapter(emptyList()) { route ->
            confirmDelete("route", route.routeName) {
                FirebaseRepository.deleteRoute(route.routeId) { ok ->
                    if (ok) Toast.makeText(context, "Route deleted", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.rvRoutes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRoutes.adapter = routeAdapter

        routeListener = FirebaseRepository.listenRoutes { routes ->
            routeAdapter.updateRoutes(routes)
            binding.tvRouteCount.text = "Routes: ${routes.size}"
        }

        binding.btnAddRoute.setOnClickListener { showAddRouteDialog() }

        setupPingMonitor()
    }

    private fun setupPingMonitor() {
        val pingAdapter = AdminPingAdapter(emptyList()) { ping ->
            confirmDelete("ping", "by ${ping.userName}") {
                FirebaseRepository.deletePing(ping.routeId, ping.pingId) { ok ->
                    if (ok) Toast.makeText(context, "Ping removed", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.rvPings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPings.adapter = pingAdapter

        FirebaseRepository.listenRoutes { routes ->
            val allPings = mutableListOf<com.gramayatri.app.model.BusPing>()
            routes.forEach { route ->
                if (pingListeners.containsKey(route.routeId)) return@forEach
                val l = FirebaseRepository.listenPings(route.routeId) { pings ->
                    allPings.removeAll { it.routeId == route.routeId }
                    allPings.addAll(pings)
                    allPings.sortByDescending { it.timestamp }
                    pingAdapter.updatePings(allPings)
                    binding.tvPingCount.text = "Active Pings: ${allPings.size}"
                }
                pingListeners[route.routeId] = l
            }
        }
    }

    private fun showAddRouteDialog() {
        val dialogView = layoutInflater.inflate(com.gramayatri.app.R.layout.dialog_add_route, null)
        val etName       = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.gramayatri.app.R.id.etRouteName)
        val etFrom       = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.gramayatri.app.R.id.etFromVillage)
        val etTo         = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.gramayatri.app.R.id.etToVillage)
        val etStops      = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.gramayatri.app.R.id.etStops)
        val etDeparture  = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.gramayatri.app.R.id.etDeparture)

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Route")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name      = etName.text.toString().trim()
                val from      = etFrom.text.toString().trim()
                val to        = etTo.text.toString().trim()
                val stopsRaw  = etStops.text.toString().trim()
                val departure = etDeparture.text.toString().trim()

                if (name.isEmpty() || from.isEmpty() || to.isEmpty() || stopsRaw.isEmpty()) {
                    Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show(); return@setPositiveButton
                }
                val stops = stopsRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val route = BusRoute(routeName = name, fromVillage = from, toVillage = to, stops = stops, departureTime = departure)
                FirebaseRepository.addRoute(route) { ok, _ ->
                    Toast.makeText(context,
                        if (ok) "Route added successfully" else "Failed to add route",
                        Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDelete(type: String, name: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete $type?")
            .setMessage("Are you sure you want to delete $type: \"$name\"?")
            .setPositiveButton("Delete") { _, _ -> onConfirm() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        routeListener?.let { FirebaseRepository.removeRoutesListener(it) }
        pingListeners.forEach { (rid, l) -> FirebaseRepository.removePingsListener(rid, l) }
        _binding = null
    }
}
