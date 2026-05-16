package com.gramayatri.app.ui.ping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ValueEventListener
import com.gramayatri.app.adapter.EtaAdapter
import com.gramayatri.app.databinding.FragmentPingBinding
import com.gramayatri.app.model.BusPing
import com.gramayatri.app.model.BusRoute
import com.gramayatri.app.model.StopEta
import com.gramayatri.app.repository.FirebaseRepository

class PingFragment : Fragment() {

    private var _binding: FragmentPingBinding? = null
    private val binding get() = _binding!!

    private val routes = mutableListOf<BusRoute>()
    private var selectedRoute: BusRoute? = null
    private var selectedStop: String = ""
    private var routeListener: ValueEventListener? = null
    private var pingListener: ValueEventListener? = null
    private var currentRouteIdListened: String? = null

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentPingBinding.inflate(inflater, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etaAdapter = EtaAdapter(emptyList())
        binding.rvEta.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEta.adapter = etaAdapter

        // Load routes into spinner
        routeListener = FirebaseRepository.listenRoutes { routeList ->
            routes.clear()
            routes.addAll(routeList)
            val names = routeList.map { it.routeName }
            val routeSpinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
            routeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerRoute.adapter = routeSpinnerAdapter
        }

        binding.spinnerRoute.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, v: View?, pos: Int, id: Long) {
                selectedRoute = routes.getOrNull(pos)
                selectedRoute?.let { route ->
                    // Populate stop spinner
                    val stopAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, route.stops)
                    stopAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerStop.adapter = stopAdapter

                    // Listen to live pings for this route
                    currentRouteIdListened?.let { old ->
                        pingListener?.let { FirebaseRepository.removePingsListener(old, it) }
                    }
                    pingListener = FirebaseRepository.listenPings(route.routeId) { pings ->
                        val latestPing = pings.firstOrNull()
                        if (latestPing != null) {
                            val etaList = route.stops.map { stop ->
                                val etaMillis = latestPing.etaMap[stop] ?: 0L
                                StopEta(stop, etaMillis, stop == latestPing.currentStop)
                            }
                            etaAdapter.updateList(etaList)
                            binding.tvLastUpdate.text = "Last update: ${latestPing.userName} – ${latestPing.pingType}"
                            binding.tvLastUpdate.visibility = View.VISIBLE
                            binding.cardEta.visibility = View.VISIBLE
                        } else {
                            binding.cardEta.visibility = View.GONE
                            binding.tvLastUpdate.visibility = View.GONE
                        }
                    }
                    currentRouteIdListened = route.routeId
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }

        binding.spinnerStop.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, v: View?, pos: Int, id: Long) {
                selectedStop = selectedRoute?.stops?.getOrNull(pos) ?: ""
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }

        // Ping buttons
        binding.btnArrived.setOnClickListener { sendPing("Bus Arrived 🟢") }
        binding.btnPassed.setOnClickListener  { sendPing("Bus Passed 🔵") }
        binding.btnOnBus.setOnClickListener   { sendPing("I'm on the Bus 🚌") }
    }

    private fun sendPing(type: String) {
        val route = selectedRoute ?: run {
            Toast.makeText(context, "Please select a route", Toast.LENGTH_SHORT).show(); return
        }
        if (selectedStop.isEmpty()) {
            Toast.makeText(context, "Please select your current stop", Toast.LENGTH_SHORT).show(); return
        }
        val uid = FirebaseRepository.currentUserId() ?: run {
            Toast.makeText(context, "Please login to send pings", Toast.LENGTH_SHORT).show(); return
        }

        val now = System.currentTimeMillis()
        val etaMap = FirebaseRepository.computeEta(route.stops, selectedStop, now)

        FirebaseRepository.getUser(uid) { user ->
            val ping = BusPing(
                routeId = route.routeId,
                userId = uid,
                userName = user?.name ?: "Passenger",
                currentStop = selectedStop,
                pingType = type,
                timestamp = now,
                etaMap = etaMap
            )
            FirebaseRepository.sendPing(ping) { ok, _ ->
                if (ok) {
                    Toast.makeText(context, "✅ Ping sent! Bus status updated.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to send ping. Check internet.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        routeListener?.let { FirebaseRepository.removeRoutesListener(it) }
        currentRouteIdListened?.let { rid -> pingListener?.let { FirebaseRepository.removePingsListener(rid, it) } }
        _binding = null
    }
}
