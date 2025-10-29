package com.dianca.synced

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class SyncedFriendsActivity : AppCompatActivity() {

    private lateinit var rvFriends: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var uid: String
    private lateinit var adapter: SyncedFriendsAdapter
    private val friendsList = mutableListOf<SyncedFriend>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_synced_friends)


        // Bottom Navigation setup
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, TopMatchesActivity::class.java))
                R.id.nav_geo -> startActivity(Intent(this, GeolocationActivity::class.java))
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.nav_help -> startActivity(Intent(this, HelpActivity::class.java))
            }
            true
        }

        rvFriends = findViewById(R.id.rvSyncedFriends)
        rvFriends.layoutManager = LinearLayoutManager(this)

        db = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        adapter = SyncedFriendsAdapter(friendsList) { friend ->
            openFriendOptions(friend)
        }
        rvFriends.adapter = adapter

        loadSyncedFriends()
    }

    private fun loadSyncedFriends() {
        db.collection("users").document(uid)
            .collection("synced")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                friendsList.clear()
                for (doc in snapshot.documents) {
                    val friend = doc.toObject<SyncedFriend>() ?: continue
                    friendsList.add(friend)
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun openFriendOptions(friend: SyncedFriend) {
        db.collection("users").document(friend.friendId).get().addOnSuccessListener { doc ->
            val whatsapp = doc.getString("whatsapp")?.trim()
            val instagram = doc.getString("instagram")?.trim()

            val options = mutableListOf<String>()
            if (!whatsapp.isNullOrEmpty()) options.add("WhatsApp")
            if (!instagram.isNullOrEmpty()) options.add("Instagram")
            options.add("Play Games")

            AlertDialog.Builder(this)
                .setTitle("Interact with ${friend.name}")
                .setItems(options.toTypedArray()) { _, which ->
                    when (options[which]) {
                        "WhatsApp" -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$whatsapp")))
                        "Instagram" -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/$instagram")))
                        "Play Games" -> {
                            val intent = Intent(this, FriendGamesActivity::class.java)
                            intent.putExtra("friendId", friend.friendId)
                            intent.putExtra("friendName", friend.name)
                            startActivity(intent)
                        }
                    }
                }.show()
        }
    }
}
