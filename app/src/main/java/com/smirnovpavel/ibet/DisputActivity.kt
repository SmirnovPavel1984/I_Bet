package com.smirnovpavel.ibet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.smirnovpavel.ibet.adapter.MessageAdapter
import com.smirnovpavel.ibet.adapter.UserAdapter
import com.smirnovpavel.ibet.data.Disput
import com.smirnovpavel.ibet.data.Message
import com.smirnovpavel.ibet.data.User
import com.smirnovpavel.ibet.database.DbManager
import com.smirnovpavel.ibet.database.ReadDataCallback
import com.smirnovpavel.ibet.databinding.ActivityDisputBinding
import com.smirnovpavel.ibet.dialogs.DialogLose
import com.smirnovpavel.ibet.dialogs.DialogReg

class DisputActivity : AppCompatActivity(), ReadDataCallback {
    lateinit var binding: ActivityDisputBinding
    lateinit var mesAdapt: MessageAdapter
    private val dbManager = DbManager(this, this)
    private val dialogLose = DialogLose(this)
    var currentUser: User? = null
    val userAdapter = UserAdapter()
    var opponent: User? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentDisput = intent.getSerializableExtra(MainActivity.OPEN_DISP) as Disput
        fillViews(currentDisput)
        currentUser = intent.getSerializableExtra(MainActivity.PROFILE) as User

        //---------------------------------find opponent
        var opponentID: String? = null
        if (currentDisput.disputAuthor == currentUser!!.userID) {
            opponentID = currentDisput.disputDefendant
        } else if (currentDisput.disputDefendant == currentUser!!.userID) {
            opponentID = currentDisput.disputAuthor
        }
        dbManager.findOpponentByID(opponentID)
        opponent = userAdapter.opponent
        //Log.d("Testing", "параметры opponent внутри активити = ${opponent!!.userEmail} ${opponent!!.userID}")
        //------------------------------------------------


        binding.btnComfirm.setOnClickListener{
            onClickConfirm(currentDisput)
        }

        binding.buttonILose.setOnClickListener{
            onClickLose (currentDisput, currentUser/*, opponent*/)
        }


        //-------------------------------------------Messages
        val database = Firebase.database
        val dispID = currentDisput.disputID
        val path = database.getReference("main/messages/${dispID}")
        binding.buttonSendMessage.setOnClickListener{
            path.child(path.push().key ?: "error_message").setValue(createMessage())
            binding.etMessage.setText("")
        }
        onMessageChangeListener(path)
        initRcVMessage()
    }

    private fun initRcVMessage () = with (binding) {
        mesAdapt = MessageAdapter()
        rcvChat.layoutManager = LinearLayoutManager(this@DisputActivity)
        rcvChat.adapter = mesAdapt
    }

    private fun onMessageChangeListener (dRef: DatabaseReference) {
        dRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<Message>()
                for (s in snapshot.children) {
                    val mes = s.getValue(Message::class.java)
                    if (mes != null) list.add(mes)
                }
                mesAdapt.submitList(list)
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    fun createMessage () : Message {
        val message = Message(
            currentUser?.userID,
            binding.etMessage.text.toString()
        )
        return message
    }


    /*fun onClickFindOPPO(oppo: String?) {
        if (oppo != null) {
            Log.d("Testing", "opponent = ${oppo}")
        }
        opponent = userAdapter.opponent
        Log.d("Testing", "параметры opponent внутри вызова кнопки = ${opponent!!.userEmail} ${opponent!!.userID}")
    }*/

    fun closeDisputCreate(view: View) {
        finish()
    }

    private fun onClickLose (disp: Disput, current: User?/*, opponent: User?*/) {
        //opponent = userAdapter.opponent
        if (opponent != null) {
            Log.d("Testing", "параметры opponent там, где надо! = ${opponent!!.userEmail.toString()}")
        }

        val winnerNewRatio = opponent?.userRatio?.let { disp.disputBetValue?.plus(it) }
        val winnerNewActive = opponent?.userDisputActive?.minus(1)
        val winnerNewVictory = opponent?.userDisputWin?.plus(1)
        val loserNewActive = current?.userDisputActive?.minus(1)
        val loserNewLoses = current?.userDisputLose?.plus(1)

        val tempDisput = Disput(
            disp.disputID,
            disp.disputName,
            disp.disputDesription,
            disp.disputAuthor,
            disp.disputAuthorName,
            disp.disputDefendant,
            disp.disputDefendantName,
            disp.disputBetType,
            0,
            opponent?.userID,
            current?.userID,
            disp.disputDate,
            "close"
        )

        val tempCurrent = User(
            current?.userName,
            current?.userID,
            current?.userEmail,
            current?.userDisputCount,
            current?.userDisputWin,
            loserNewLoses,
            loserNewActive,
            current?.userRatio,
            currentUser?.userMoney
        )

        val tempOpponent = User(
            opponent?.userName,
            opponent?.userID,
            opponent?.userEmail,
            opponent?.userDisputCount,
            winnerNewVictory,
            opponent?.userDisputLose,
            winnerNewActive,
            winnerNewRatio,
            currentUser?.userMoney
        )

        dbManager.putUserToDatabase(tempCurrent)
        dbManager.putUserToDatabase(tempOpponent)
        dbManager.publishDisput(tempDisput)

        dialogLose.createLoseDial()
        //finish()
    }


    private fun fillViews (disp: Disput) = with (binding) {
        tvDisputTheme.text=disp.disputName
        tvDefendant.text = disp.disputDefendant
        tvDisputText.text = disp.disputDesription

        if (disp.disputBetType == "ratio") {
            tvBetText.text = "Ставка: ${disp.disputBetValue} ед. рейтинга"
        } else if (disp.disputBetType == "money") {
            tvBetText.text = "Ставка: ${disp.disputBetValue} руб."
        } else {
            tvBetText.text = "Ставка не указана"
        }

        if (isAuthor(disp)) {
            tvAuthor.text = "Вы утверждаете, что:"
        } else {
            tvAuthor.text = "${disp.disputAuthorName} говорит, что:"
        }

        if (disp.disputDefendant == null) {
            tvDefendant.text = "и с этим пока никто не спорит"
        } else if (isDefendant(disp)) {
            tvDefendant.text = "А Вы с ним не согласны"
        } else {
            tvDefendant.text = "а ${disp.disputDefendantName} не согласен."
        }

        //---------------Я не автор, и не ответчик
        if ((!isAuthor(disp)) && (!isDefendant(disp))) {
            btnComfirm.visibility = View.VISIBLE
            cardChat.visibility = View.GONE
            cardButton.visibility = View.GONE
        }

        //----------------Я ответчик или Я автор и есть ответчик
        if (((!isAuthor(disp)) && (isDefendant(disp))) ||
            ((isAuthor(disp)) && (disp.disputDefendant!=null))) {
            btnComfirm.visibility = View.GONE
            cardChat.visibility = View.VISIBLE
            cardButton.visibility = View.VISIBLE
        }

        //---------------Я автор. но ответчика нет
        if ((isAuthor(disp)) && (disp.disputDefendant==null)) {
            btnComfirm.visibility = View.GONE
            cardChat.visibility = View.GONE
            cardButton.visibility = View.GONE
        }

        if (disp.disputStatus=="close") {
            btnComfirm.visibility = View.GONE
            cardChat.visibility = View.GONE
            cardButton.visibility = View.GONE
            tvBetText.text = "Спор завершён"
        }

    }

    fun onClickConfirm (disp: Disput) {

        if (notEnoughRatio(disp.disputBetValue)) {
            Toast.makeText(this, "Вы не можете ставить больше, чем у вас есть...", Toast.LENGTH_SHORT).show()
            return
        }

        val newBet = (disp.disputBetValue)?.times(2)
        val name: String?
        if (!currentUser?.userName?.isEmpty()!!) {
            name = currentUser?.userName
        } else {
            name = currentUser?.userEmail
        }
        val tempDisput = Disput(
            disp.disputID,
            disp.disputName,
            disp.disputDesription,
            disp.disputAuthor,
            disp.disputAuthorName,
            currentUser?.userID,
            name,
            disp.disputBetType,
            newBet,
            null,
            null,
            disp.disputDate,
            "in disput"
        )

        var newRatio: Int? = currentUser?.userRatio!! - disp.disputBetValue!!
        if (newRatio==0) newRatio=1
        val newCount = (currentUser?.userDisputCount ?: 0) +1
        val newActivCount = (currentUser?.userDisputActive ?: 0) +1

        val tempUser = User(
            currentUser?.userName,
            currentUser?.userID,
            currentUser?.userEmail,
            newCount,
            currentUser?.userDisputWin,
            currentUser?.userDisputLose,
            newActivCount,
            newRatio,
            currentUser?.userMoney
        )
        dbManager.putUserToDatabase(tempUser)
        dbManager.publishDisput(tempDisput)
        binding.btnComfirm.visibility = View.GONE
        binding.cardChat.visibility = View.VISIBLE
        binding.cardButton.visibility = View.VISIBLE
        binding.tvDefendant.text = "А Вы с ним не согласны"
        Toast.makeText(this, "Вы вступили в спор", Toast.LENGTH_LONG).show()
    }


    private fun isAuthor (disp: Disput) : Boolean {
        return disp.disputAuthor == Firebase.auth.uid
    }

    private fun isDefendant (disp: Disput) : Boolean {
        return disp.disputDefendant == Firebase.auth.uid
    }

    private fun notEnoughRatio (betRatio : Int?) : Boolean {
        if ((betRatio == null) || (currentUser?.userRatio == null)) return false
        return currentUser!!.userRatio!! < betRatio
    }

    override fun readData(list: List<Disput>) {

    }

    override fun readUser(user: User?) {
        userAdapter.updateAdapterToFindOpponent (user)
        Log.d("Testing", "параметры user внутри интерфейса = ${user!!.userEmail} ${user.userID}")
    }


}