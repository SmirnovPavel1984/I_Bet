package com.smirnovpavel.ibet

import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.smirnovpavel.ibet.adapter.UsersDisputRcVAdapter
import com.smirnovpavel.ibet.data.Disput
import com.smirnovpavel.ibet.data.User
import com.smirnovpavel.ibet.database.DbManager
import com.smirnovpavel.ibet.database.ReadDataCallback
import com.smirnovpavel.ibet.databinding.ActivityProfileBinding
import com.squareup.picasso.Picasso
import java.io.IOException

class ProfileActivity : AppCompatActivity(), ReadDataCallback {

    lateinit var binding: ActivityProfileBinding
    lateinit var imAcc: ImageView
    val dbManager = DbManager(this, this)
    var currentUser: User? = null


    val adapter = UsersDisputRcVAdapter(this)

//---------------------------------------For sound------------------------------------------//
    private lateinit var soundPool: SoundPool                                               //
    private lateinit var assetManager: AssetManager                                         //
    private var gongSound: Int = 0                                                          //
    private var streamID = 0                                                                //
//------------------------------------------------------------------------------------------//

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



//---------------------------------------For sound------------------------------------------//
        val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)     //
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()              //
        soundPool = SoundPool.Builder().setAudioAttributes(attributes).build()              //
        assetManager = assets                                                               //
        gongSound = loadSound("gong_sound.ogg")                                             //
//------------------------------------------------------------------------------------------//

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = intent.getSerializableExtra(MainActivity.PROFILE) as User
        currentUser?.let { fillViews(it) }


        initRcView()
        dbManager.readUsersDisputFromDB()

        binding.ivNotification.setOnClickListener{
            dbManager.readUsersDisputFromDB()
            playSound(gongSound)

        }

//-----------------------Take Google Photo
        imAcc=binding.ivUserPhoto
        if (Firebase.auth.currentUser?.photoUrl != null) {
            Picasso.get().load(Firebase.auth.currentUser?.photoUrl).into(imAcc)
        } else {
            imAcc.setImageResource(R.drawable.user_logo_800x800)
        }


    }


//---------------------------------------For sound------------------------------------------//
    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }

    private fun playSound(sound: Int): Int {
        if (sound > 0) {
            streamID = soundPool.play(sound, 1F, 1F, 1, 0, 1F)
        }
        return streamID
    }

    private fun loadSound(fileName: String): Int {
        val afd: AssetFileDescriptor = try {
            application.assets.openFd(fileName)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("IBET_ERRORS", "Не могу загрузить файл $fileName")

            return -1
        }
        return soundPool.load(afd, 1)
    }
//------------------------------------------------------------------------------------------//

    fun onClickNewDisput (view: View) {
        val intent = Intent(this, CreateDisputActivity::class.java)
        //currentUser = userAdapter.currentUser
        intent.putExtra(MainActivity.PROFILE, currentUser)
        startActivity(intent)
    }

    fun onClickPageNotExist(view: View) {
        Toast.makeText(this, "Раздел в разработке", Toast.LENGTH_SHORT).show()
    }

    fun onClickOpenLenta(view: View) {
        finish()
    }

    override fun readData(list: List<Disput>) {
        adapter.updateAdapter(list)
    }

    override fun readUser(user: User?)  {
           }

    private fun initRcView () {
        binding.apply {
            rcvUsersDisputs.layoutManager = LinearLayoutManager(this@ProfileActivity)
            rcvUsersDisputs.adapter = adapter
        }
    }

    private fun fillViews (user: User) = with (binding) {
        if (!user?.userName?.isEmpty()!!) {
            tvUserName.text = user?.userName
        } else {
            tvUserName.text = user?.userEmail
        }
        //tvUserName.text = user.userEmail
        tvWins.text = "побед: ${user.userDisputWin}"
        tvRatio.text = "рейтинг: ${user.userRatio}"
        tvLose.text = "поражений: ${user.userDisputLose}"
        tvActiv.text = "активно: ${user.userDisputActive}"
        tvMoney.text = "деньги: ${user.userMoney}"

    }

    companion object {
        const val OPEN_DISP = "open_disput"

    }
}