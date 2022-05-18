package com.smirnovpavel.ibet.database

import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.smirnovpavel.ibet.MainActivity
import com.smirnovpavel.ibet.data.Disput
import com.smirnovpavel.ibet.data.User

class DbManager (val readDataCallback: ReadDataCallback?, val act: AppCompatActivity) {
    val db = Firebase.database.getReference("main")
    //internal var user: User? = null


    fun publishDisput (disput: Disput) {
        db.child("Disputs")
            .child(disput.disputID ?: "empty_disput_ID")
            .setValue(disput)
    }


    fun putUserToDatabase (user: User) {
        db.child("Users")
            .child(user.userID ?: "empty_user_ID")
            .setValue(user)
    }

    fun readDisputFromDB () {
        db.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dispArray = ArrayList<Disput>()
                for (item in snapshot.child("Disputs").children) {
                    val disp = item.getValue(Disput::class.java)
                    if (disp != null && disp.disputDefendant==null)  dispArray.add(disp)
                }
                val dispArrReverse = dispArray.asReversed()
                readDataCallback?.readData(dispArrReverse)
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    fun readUsersDisputFromDB () {
        db.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dispArray = ArrayList<Disput>()
                for (item in snapshot.child("Disputs").children) {
                    val disp = item.getValue(Disput::class.java)
                    if (disp != null
                        && (disp.disputAuthor.equals(Firebase.auth.currentUser?.uid)
                                || disp.disputDefendant.equals(Firebase.auth.currentUser?.uid)))  dispArray.add(disp)
                }
                val dispArrReverse = dispArray.asReversed()
                readDataCallback?.readData(dispArrReverse)
            }
            override fun onCancelled(error: DatabaseError) {}

        })
    }


    fun findUserByID () {
        var curUser: User? = null
        db.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.child("Users").children) {
                    val tempUser = item.getValue(User::class.java)
                    if (Firebase.auth.currentUser?.uid == tempUser?.userID.toString()) {
                            curUser = tempUser
                            Log.d("IBETTEST", curUser?.userRatio.toString())
                    }
                }
                if (curUser != null) {
                    readDataCallback?.readUser(curUser)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun findOpponentByID (id : String?) {
        var curUser: User? = null
        db.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.child("Users").children) {
                    val tempUser = item.getValue(User::class.java)
                    Log.d("Testing", "параметры tempUser внутри БД = ${tempUser!!.userEmail}")
                    if (id == tempUser.userID) {
                        curUser = tempUser
                        Log.d("IBETTEST", curUser?.userRatio.toString())
                    }
                }
                if (curUser != null) {
                    Log.d("Testing", "opponent на выходе БД = ${curUser!!.userEmail}")
                    readDataCallback?.readUser(curUser)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun deleteDisput (disp: Disput) {
        if (disp.disputID == null) return
        db.child("Disputs").child(disp.disputID).removeValue().addOnCompleteListener {
            if (it.isSuccessful)
                Toast.makeText(act, "Спор был уалён. Ставка возвращена Вам на баланс.\nОбновите ленту, ударив в гонг!", Toast.LENGTH_LONG).show()
        }
    }

    fun checkUserByID () {
        db.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var count = 0
                for (item in snapshot.child("Users").children) {
                    val tempUser = item.getValue(User::class.java)
                    if (Firebase.auth.currentUser?.uid == tempUser?.userID.toString()) {
                        count++
                        Log.d("IBETTEST", count.toString())
                    }
                }
                if (count == 0) {
                    putUserToDatabase(createUser())
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun createUser () : User {
        var name : String? = ""
        if (FirebaseAuth.getInstance().currentUser?.displayName != null) {
            name = FirebaseAuth.getInstance().currentUser?.displayName
        }
        val curUser = User (
            name,
            FirebaseAuth.getInstance().currentUser?.uid,
            FirebaseAuth.getInstance().currentUser?.email

        )
        return curUser
    }
}