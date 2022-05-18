package com.smirnovpavel.ibet.adapter

import android.content.Intent
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.smirnovpavel.ibet.DisputActivity
import com.smirnovpavel.ibet.MainActivity
import com.smirnovpavel.ibet.ProfileActivity
import com.smirnovpavel.ibet.data.Disput
import com.smirnovpavel.ibet.data.User
import com.smirnovpavel.ibet.database.DbManager
import com.smirnovpavel.ibet.databinding.UsersDisputItemBinding


class UsersDisputRcVAdapter (val act: ProfileActivity) : RecyclerView.Adapter<UsersDisputRcVAdapter.UsersDisputHolder>() {
    val disputArray = ArrayList<Disput>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersDisputHolder {
        val binding = UsersDisputItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UsersDisputHolder(binding, act)
    }

    override fun onBindViewHolder(holder: UsersDisputHolder, position: Int) {
        holder.setData(disputArray[position])
    }

    override fun getItemCount(): Int {
        return disputArray.size
    }

    fun updateAdapter (newList: List<Disput>) {
        disputArray.clear()
        disputArray.addAll(newList)
        notifyDataSetChanged()
    }

    class UsersDisputHolder(val binding : UsersDisputItemBinding, val act: ProfileActivity) : RecyclerView.ViewHolder(binding.root) {

        fun setData (disp: Disput) = with (binding) {
            tvDisputTheme.text = disp.disputName
            tvDisputDescr.text = disp.disputDesription
            if (disp.disputBetType == "ratio") {
                tvBet.text = "Ставка: ${disp.disputBetValue} ед. рейтинга"
            } else if (disp.disputBetType == "money") {
                tvBet.text = "Ставка: ${disp.disputBetValue} руб."
            } else {
                tvBet.text = "Ставка не указана"
            }
            if (disp.disputDefendant!=null) {}
            if (isAuthor(disp)) {
                tvAuthor.text = "Ваш спор"
            } else {
                tvAuthor.text = "${disp.disputAuthorName} спорит"
            }

            tvDefendant.text = disp.disputDefendant

            showDelBtn(isAuthor(disp))
            if (disp.disputDefendant!=null) ivDelete.visibility=View.GONE
            if (disp.disputDefendant==null) {
                tvDefendant.text = "Пока никто с Вами не спорит"
            } else {
                tvDefendant.text = "Против Вас спорит ${disp.disputDefendantName}"
            }

            if (disp.disputStatus == "activ") {
                tvStatus.text = "Активно"
                clBackground.setBackgroundColor(0xFFFFFFFF.toInt())
                tvAuthor.visibility = View.VISIBLE
                tvDefendant.visibility = View.VISIBLE
                tvBet.visibility = View.VISIBLE
                ivDelete.visibility = View.VISIBLE
            } else if (disp.disputStatus == "in disput") {
                tvStatus.text = "СПОР ИДЁТ!"
                clBackground.setBackgroundColor(0xFF93FFB1.toInt())
                ivDelete.visibility = View.GONE
                tvAuthor.visibility = View.VISIBLE
                tvDefendant.visibility = View.VISIBLE
                tvBet.visibility = View.VISIBLE
            } else if (disp.disputStatus == "close") {
                if (disp.disputWinner == act.currentUser?.userID) {
                    tvStatus.text = "ВЫИГРАНО!"
                    clBackground.setBackgroundColor(0xFF98C3FF.toInt())
                    tvAuthor.visibility = View.GONE
                    tvDefendant.visibility = View.GONE
                    tvBet.visibility = View.GONE
                    //ivDelete.visibility = View.VISIBLE
                } else if (disp.disputLoser == act.currentUser?.userID) {
                    tvStatus.text = "Вы проиграли этот спор..."
                    clBackground.setBackgroundColor(0xFFFFCFCF.toInt())
                    tvAuthor.visibility = View.GONE
                    tvDefendant.visibility = View.GONE
                    tvBet.visibility = View.GONE
                    //ivDelete.visibility = View.VISIBLE
                }
            } else {
                tvStatus.text = "empty_status"
            }

            if (disp.disputDefendant == act.currentUser?.userID) {
                tvDefendant.text = "против Вас"
            }



            itemView.setOnClickListener(onClickDisp(disp))
            ivDelete.setOnClickListener{
                onClickDel(disp)
            }
        }

        private fun onClickDisp (disp: Disput): View.OnClickListener {
            return View.OnClickListener {
                val i = Intent (act, DisputActivity::class.java).apply {
                    putExtra (ProfileActivity.OPEN_DISP, disp)
                    putExtra (MainActivity.PROFILE, act.currentUser)
                }
                act.startActivity(i)
            }
        }

        private fun onClickDel (disp: Disput) {
            val dbManager = DbManager(act, act)
            dbManager.deleteDisput(disp)
            val current = act.currentUser
            val tempCurrent = User(
                current?.userName,
                current?.userID,
                current?.userEmail,
                current?.userDisputCount?.minus(1),
                current?.userDisputWin,
                current?.userDisputLose,
                current?.userDisputActive?.minus(1),
                current?.userRatio?.plus(disp.disputBetValue!!),
                current?.userMoney
            )
            dbManager.putUserToDatabase(tempCurrent)
            dbManager.readUsersDisputFromDB()

        }


        private fun isAuthor (disp: Disput) : Boolean {
            return disp.disputAuthor == Firebase.auth.uid
        }

        private fun isDefendant (disp: Disput) : Boolean {
            return disp.disputDefendant == Firebase.auth.uid
        }

        private fun showDelBtn (isAuthor: Boolean) = with (binding){
            if (isAuthor) {
                ivDelete.visibility = View.VISIBLE
            } else {
                ivDelete.visibility = View.GONE
            }
        }

    }

}