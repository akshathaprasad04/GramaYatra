package com.gramayatri.app.ui.route

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ValueEventListener
import com.gramayatri.app.adapter.RouteAdapter
import com.gramayatri.app.databinding.FragmentRouteBinding
import com.gramayatri.app.repository.FirebaseRepository

class RouteFragment : Fragment() {

    private var _binding: FragmentRouteBinding? = null
    private val binding get() = _binding!!
    private var routeListener: ValueEventListener? = null

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentRouteBinding.inflate(inflater, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = RouteAdapter(emptyList())
        binding.rvRoutes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRoutes.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
        }

        routeListener = FirebaseRepository.listenRoutes { routes ->
            adapter.updateRoutes(routes)
            binding.tvEmpty.visibility = if (routes.isEmpty()) View.VISIBLE else View.GONE
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        routeListener?.let { FirebaseRepository.removeRoutesListener(it) }
        _binding = null
    }
}
