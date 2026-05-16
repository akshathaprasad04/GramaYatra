package com.gramayatri.app.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gramayatri.app.model.BusPing
import com.gramayatri.app.model.BusRoute
import com.gramayatri.app.model.User

object FirebaseRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    // ── Auth ──────────────────────────────────────────────────────────────────

    fun currentUserId() = auth.currentUser?.uid

    fun isLoggedIn() = auth.currentUser != null

    fun logout() = auth.signOut()

    fun login(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onResult(true, "") }
            .addOnFailureListener { onResult(false, it.message ?: "Login failed") }
    }

    fun register(
        name: String, email: String, password: String, phone: String, village: String,
        onResult: (Boolean, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user!!.uid
                val user = User(uid, name, email, phone, village)
                db.child("users").child(uid).setValue(user)
                    .addOnSuccessListener { onResult(true, "") }
                    .addOnFailureListener { onResult(false, it.message ?: "Failed to save user") }
            }
            .addOnFailureListener { onResult(false, it.message ?: "Registration failed") }
    }

    // ── User ─────────────────────────────────────────────────────────────────

    fun getUser(uid: String, onResult: (User?) -> Unit) {
        db.child("users").child(uid).get()
            .addOnSuccessListener { snap ->
                onResult(snap.getValue(User::class.java))
            }
            .addOnFailureListener { onResult(null) }
    }

    // ── Routes ────────────────────────────────────────────────────────────────

    fun listenRoutes(onUpdate: (List<BusRoute>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val list = snap.children.mapNotNull { it.getValue(BusRoute::class.java) }
                onUpdate(list)
            }
            override fun onCancelled(error: DatabaseError) { onUpdate(emptyList()) }
        }
        db.child("routes").addValueEventListener(listener)
        return listener
    }

    fun removeRoutesListener(listener: ValueEventListener) {
        db.child("routes").removeEventListener(listener)
    }

    fun addRoute(route: BusRoute, onResult: (Boolean, String) -> Unit) {
        val key = db.child("routes").push().key ?: return
        val r = route.copy(routeId = key)
        db.child("routes").child(key).setValue(r)
            .addOnSuccessListener { onResult(true, key) }
            .addOnFailureListener { onResult(false, it.message ?: "Error") }
    }

    fun deleteRoute(routeId: String, onResult: (Boolean) -> Unit) {
        db.child("routes").child(routeId).removeValue()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    // ── Pings ─────────────────────────────────────────────────────────────────

    fun sendPing(ping: BusPing, onResult: (Boolean, String) -> Unit) {
        val key = db.child("pings").child(ping.routeId).push().key ?: return
        val p = ping.copy(pingId = key)
        db.child("pings").child(ping.routeId).child(key).setValue(p)
            .addOnSuccessListener { onResult(true, "") }
            .addOnFailureListener { onResult(false, it.message ?: "Error") }
    }

    fun listenPings(routeId: String, onUpdate: (List<BusPing>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val list = snap.children
                    .mapNotNull { it.getValue(BusPing::class.java) }
                    .sortedByDescending { it.timestamp }
                onUpdate(list)
            }
            override fun onCancelled(error: DatabaseError) { onUpdate(emptyList()) }
        }
        db.child("pings").child(routeId).addValueEventListener(listener)
        return listener
    }

    fun removePingsListener(routeId: String, listener: ValueEventListener) {
        db.child("pings").child(routeId).removeEventListener(listener)
    }

    fun deletePing(routeId: String, pingId: String, onResult: (Boolean) -> Unit) {
        db.child("pings").child(routeId).child(pingId).removeValue()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    // ── ETA Calculator ────────────────────────────────────────────────────────
    // Average inter-village travel = 10 minutes

    fun computeEta(stops: List<String>, currentStop: String, pingTime: Long): Map<String, Long> {
        val idx = stops.indexOf(currentStop)
        if (idx < 0) return emptyMap()
        val etaMap = mutableMapOf<String, Long>()
        stops.forEachIndexed { i, stop ->
            val stepsAhead = i - idx
            if (stepsAhead > 0) {
                etaMap[stop] = pingTime + (stepsAhead * 10 * 60 * 1000L)
            }
        }
        return etaMap
    }

    // ── Seed sample routes ────────────────────────────────────────────────────

    fun seedSampleRoutesIfEmpty() {
        db.child("routes").get().addOnSuccessListener { snap ->
            if (!snap.exists()) {
                val routes = listOf(
                    BusRoute(
                        routeName = "Bangalore – Kolar Village Route",
                        fromVillage = "Bangalore Majestic",
                        toVillage = "Kolar Gold Fields",
                        stops = listOf("Bangalore Majestic", "Hosur Road", "Attibele", "Sarjapura", "Malur", "Bangarpet", "Kolar Gold Fields"),
                        departureTime = "06:30 AM",
                        isActive = true
                    ),
                    BusRoute(
                        routeName = "Mysore – Mandya Rural Route",
                        fromVillage = "Mysore",
                        toVillage = "Mandya",
                        stops = listOf("Mysore", "Srirangapatna", "Pandavapura", "Maddur", "Malavalli", "Mandya"),
                        departureTime = "07:00 AM",
                        isActive = true
                    ),
                    BusRoute(
                        routeName = "Hubli – Dharwad Feeder Route",
                        fromVillage = "Hubli",
                        toVillage = "Dharwad",
                        stops = listOf("Hubli Bus Stand", "Vidyanagar", "Navalur", "Tarihal", "Dharwad"),
                        departureTime = "08:15 AM",
                        isActive = true
                    )
                )
                routes.forEach { route ->
                    val key = db.child("routes").push().key ?: return@forEach
                    db.child("routes").child(key).setValue(route.copy(routeId = key))
                }
            }
        }
    }
}
