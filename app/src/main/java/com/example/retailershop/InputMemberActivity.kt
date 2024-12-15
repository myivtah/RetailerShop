package com.example.retailershop

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.retailershop.model.Member
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class InputMemberActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var memberName: EditText
    private lateinit var memberEmail: EditText
    private lateinit var memberPhone: EditText
    private lateinit var memberAddress: EditText
    private lateinit var btnAddMember: Button
    private var memberId: String? = null
    private lateinit var auth: FirebaseAuth
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_input_member)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi Firebase Realtime Database dan Auth
        database = FirebaseDatabase.getInstance().reference.child("members")
        auth = FirebaseAuth.getInstance()
        userEmail = auth.currentUser?.email?.replace(".", ",")

        // Inisialisasi view
        memberName = findViewById(R.id.memberName)
        memberEmail = findViewById(R.id.memberEmail)
        memberPhone = findViewById(R.id.memberPhone)
        memberAddress = findViewById(R.id.memberAddress)
        btnAddMember = findViewById(R.id.addMember)

        // Periksa apakah mengedit member yang sudah ada
        memberId = intent.getStringExtra("memberId")
        if (memberId != null) {
            loadMemberData(memberId!!)
        }

        // Set click listener untuk tombol Tambah Member
        btnAddMember.setOnClickListener {
            if (memberId == null) {
                addMemberToDatabase()
            } else {
                updateMemberInDatabase(memberId!!)
            }
        }
    }

    private fun loadMemberData(memberId: String) {
        database.child("$userEmail/$memberId").get().addOnSuccessListener { snapshot ->
            val member = snapshot.getValue(Member::class.java)
            member?.let {
                memberName.setText(it.name)
                memberEmail.setText(it.email)
                memberPhone.setText(it.phone)
                memberAddress.setText(it.address)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal memuat data member", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addMemberToDatabase() {
        val name = memberName.text.toString().trim()
        val email = memberEmail.text.toString().trim()
        val phone = memberPhone.text.toString().trim()
        val address = memberAddress.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Silakan isi semua bidang", Toast.LENGTH_SHORT).show()
            return
        }

        val currentEmail = userEmail
        if (currentEmail == null) {
            Toast.makeText(this, "Pengguna tidak terautentikasi", Toast.LENGTH_SHORT).show()
            return
        }

        database.child(currentEmail).get().addOnSuccessListener { snapshot ->
            val newMemberId = (snapshot.childrenCount + 1).toString()

            val memberData = HashMap<String, Any>().apply {
                put("id", newMemberId)
                put("name", name)
                put("email", email)
                put("phone", phone)
                put("address", address)
            }

            database.child("$currentEmail/$newMemberId").setValue(memberData)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Member berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                        // Hapus input fields
                        memberName.text.clear()
                        memberEmail.text.clear()
                        memberPhone.text.clear()
                        memberAddress.text.clear()
                        finish() // Kembali ke MemberActivity
                    } else {
                        Toast.makeText(this, "Gagal menambahkan member", Toast.LENGTH_SHORT).show()
                    }
                }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMemberInDatabase(memberId: String) {
        val name = memberName.text.toString().trim()
        val email = memberEmail.text.toString().trim()
        val phone = memberPhone.text.toString().trim()
        val address = memberAddress.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Silakan isi semua bidang", Toast.LENGTH_SHORT).show()
            return
        }

        val currentEmail = userEmail
        if (currentEmail == null) {
            Toast.makeText(this, "Pengguna tidak terautentikasi", Toast.LENGTH_SHORT).show()
            return
        }

        val memberData = HashMap<String, Any>().apply {
            put("name", name)
            put("email", email)
            put("phone", phone)
            put("address", address)
        }

        database.child("$currentEmail/$memberId").updateChildren(memberData)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Member berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    finish() // Kembali ke MemberActivity
                } else {
                    Toast.makeText(this, "Gagal memperbarui member", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Gagal memperbarui member", Toast.LENGTH_SHORT).show()
            }
    }
}
