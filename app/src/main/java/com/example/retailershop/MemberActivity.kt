package com.example.retailershop

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.retailershop.adapter.MemberAdapter
import com.example.retailershop.model.Member
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MemberActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MemberAdapter
    private lateinit var database: DatabaseReference
    private lateinit var memberList: MutableList<Member>
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member)

        // Inisialisasi Firebase Auth, RecyclerView, dan referensi Database
        auth = FirebaseAuth.getInstance()
        recyclerView = findViewById(R.id.rv_members)
        recyclerView.layoutManager = LinearLayoutManager(this)
        memberList = mutableListOf()
        adapter = MemberAdapter(memberList, this::onEditMember, this::onDeleteMember)
        recyclerView.adapter = adapter

        val userEmail = auth.currentUser?.email?.replace(".", ",") ?: return
        database = FirebaseDatabase.getInstance().reference.child("members").child(userEmail)

        // Ambil data members dari Firebase
        fetchMembers()

        // Atur tombol tambah member
        val btnAddMember = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_add_member)
        btnAddMember.setOnClickListener {
            val intent = Intent(this, InputMemberActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchMembers() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                memberList.clear()
                for (memberSnapshot in snapshot.children) {
                    val member = memberSnapshot.getValue(Member::class.java)
                    member?.let {
                        memberList.add(it)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MemberActivity, "Gagal memuat data member", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun onEditMember(member: Member) {
        // Pindah ke InputMemberActivity untuk mengedit
        val intent = Intent(this, InputMemberActivity::class.java)
        intent.putExtra("memberId", member.id)
        startActivity(intent)
    }

    private fun onDeleteMember(member: Member) {
        val userEmail = auth.currentUser?.email?.replace(".", ",") ?: return

        // Logika hapus member
        database.child(member.id).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Member berhasil dihapus", Toast.LENGTH_SHORT).show()
                fetchMembers()  // Segarkan daftar member
            } else {
                Toast.makeText(this, "Gagal menghapus member", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
