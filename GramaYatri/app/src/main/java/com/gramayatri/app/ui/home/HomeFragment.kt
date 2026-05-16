package com.gramayatri.app.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ValueEventListener
import com.gramayatri.app.adapter.PingFeedAdapter
import com.gramayatri.app.databinding.FragmentHomeBinding
import com.gramayatri.app.model.BusPing
import com.gramayatri.app.repository.FirebaseRepository

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val allPings = mutableListOf<BusPing>()
    private lateinit var feedAdapter: PingFeedAdapter
    private val listeners = mutableMapOf<String, ValueEventListener>()
    private val routeIds = mutableListOf<String>()

    // Refresh ETA display every 30s
    private val handler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            feedAdapter.notifyDataSetChanged()
            handler.postDelayed(this, 30_000)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        feedAdapter = PingFeedAdapter(allPings)
        binding.rvFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeed.adapter = feedAdapter

        binding.swipeRefresh.setOnRefreshListener {
            feedAdapter.notifyDataSetChanged()
            binding.swipeRefresh.isRefreshing = false
        }

        loadAllPings()
        handler.postDelayed(refreshRunnable, 30_000)
    }

    private fun loadAllPings() {
        // Listen to routes first, then pings per route
        FirebaseRepository.listenRoutes { routes ->
            if (routes.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.rvFeed.visibility = View.GONE
                return@listenRoutes
            }
            binding.tvEmptyState.visibility = View.GONE
            binding.rvFeed.visibility = View.VISIBLE

            routes.forEach { route ->
                if (routeIds.contains(route.routeId)) return@forEach
                routeIds.add(route.routeId)
                val l = FirebaseRepository.listenPings(route.routeId) { pings ->
                    allPings.removeAll { it.routeId == route.routeId }
                    // Keep only latest ping per route
                    pings.firstOrNull()?.let { allPings.add(it) }
                    allPings.sortByDescending { it.timestamp }
                    feedAdapter.notifyDataSetChanged()
                    binding.tvEmptyState.visibility =
                        if (allPings.isEmpty()) View.VISIBLE else View.GONE
                }
                listeners[route.routeId] = l
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(refreshRunnable)
        listeners.forEach { (routeId, l) ->
            FirebaseRepository.removePingsListener(routeId, l)
        }
        _binding = null
    }
}
