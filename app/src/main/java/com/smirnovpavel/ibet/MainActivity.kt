package com.smirnovpavel.ibet

import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.smirnovpavel.ibet.adapter.DisputRcVAdapter
import com.smirnovpavel.ibet.adapter.UserAdapter
import com.smirnovpavel.ibet.data.Disput
import com.smirnovpavel.ibet.data.User
import com.smirnovpavel.ibet.database.DbManager
import com.smirnovpavel.ibet.database.ReadDataCallback
import com.smirnovpavel.ibet.databinding.ActivityMainBinding
import java.io.IOException


class MainActivity : AppCompatActivity(), ReadDataCallback {

    lateinit var binding: ActivityMainBinding
    val dbManager = DbManager(this, this)
    val adapter = DisputRcVAdapter(this)
    val userAdapter = UserAdapter()
    var currentUser: User? = null

//---------------------------------------For sound------------------------------------------//
    private lateinit var soundPool: SoundPool                                               //
    private lateinit var assetManager: AssetManager                                         //
    private var gongSound: Int = 0                                                          //
    private var streamID = 0                                                                //
//------------------------------------------------------------------------------------------//


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//---------------------------------------For sound------------------------------------------//
        val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)     //
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()              //
        soundPool = SoundPool.Builder().setAudioAttributes(attributes).build()              //
        assetManager = assets                                                               //
        gongSound = loadSound("gong_sound.ogg")                                     //
//------------------------------------------------------------------------------------------//


//------------------find current User from DB
        dbManager.findUserByID()
        currentUser = userAdapter.currentUser

        //------------------------create new user

        dbManager.checkUserByID()


//--------------Load disputs from DB in rcv
        initRcView()
        dbManager.readDisputFromDB()


//--------------Gong
        binding.ivNotification.setOnClickListener{
            dbManager.readDisputFromDB()
            playSound(gongSound)
            Log.d("IBETTEST1", currentUser?.userEmail ?: "null")
        }

    }

    override fun onResume() {
        super.onResume()
        dbManager.findUserByID()
        currentUser = userAdapter.currentUser
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
        currentUser = userAdapter.currentUser
        intent.putExtra(PROFILE, currentUser)
        startActivity(intent)

    }

    fun onClickPageNotExist(view: View) {
        Toast.makeText(this, "Раздел в разработке", Toast.LENGTH_SHORT).show()
    }

    fun onClickProfile (view: View) {
        currentUser = userAdapter.currentUser
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra(PROFILE, currentUser)
        Log.d("finish_try", currentUser!!.userRatio.toString() ?: "null")
        startActivity(intent)
    }

    private fun initRcView () {
        binding.apply {
            rcvMainField.layoutManager = LinearLayoutManager(this@MainActivity)
            rcvMainField.adapter = adapter
        }
    }

    override fun readData(list: List<Disput>) {
        adapter.updateAdapter(list)
    }

    override fun readUser(user: User?) {
        //if (user != null)
        userAdapter.updateAdapter(user)
    }

/*
    fun createUser () : User {
        val curUser = User (
            FirebaseAuth.getInstance().currentUser?.displayName,
            FirebaseAuth.getInstance().currentUser?.uid,
            FirebaseAuth.getInstance().currentUser?.email
        )
        return curUser
    }

    fun createUser (curUser: User?) : User {
        val tempUser = User (
            curUser?.userName,
            curUser?.userID,
            curUser?.userEmail,
            curUser?.userDisputCount ?: 0,
            curUser?.userDisputWin ?: 0,
            curUser?.userDisputLose ?: 0,
            curUser?.userDisputActive ?: 0,
            curUser?.userRatio ?: 100,
            curUser?.userMoney ?: 0
        )
        return tempUser
    }*/

    companion object {

        const val OPEN_DISP = "open_disput"
        const val PROFILE = "users_profile"
    }

}