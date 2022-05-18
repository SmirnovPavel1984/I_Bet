package com.smirnovpavel.ibet.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.smirnovpavel.ibet.DisputActivity
import com.smirnovpavel.ibet.MainActivity
import com.smirnovpavel.ibet.data.Disput
import com.smirnovpavel.ibet.data.User
import com.smirnovpavel.ibet.database.DbManager
import com.smirnovpavel.ibet.databinding.DisputItemBinding
import java.io.Serializable

class DisputRcVAdapter (val act: MainActivity) : RecyclerView.Adapter<DisputRcVAdapter.DisputHolder>() {
    val disputArray = ArrayList<Disput>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DisputHolder {
        val binding = DisputItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DisputHolder(binding, act)
    }

    override fun onBindViewHolder(holder: DisputHolder, position: Int) {
        holder.setData(disputArray[position])
    }

    override fun getItemCount(): Int {
        return disputArray.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter (newList: List<Disput>) {
        disputArray.clear()
        disputArray.addAll(newList)
        notifyDataSetChanged()
    }

    class DisputHolder(val binding : DisputItemBinding, val act: MainActivity) : RecyclerView.ViewHolder(binding.root) {

        fun setData (disp: Disput) = with (binding){
            tvDisputTheme.text = disp.disputName
            tvDisputDescr.text = disp.disputDesription
            if (isAuthor(disp)) {
                tvAuthor.text = "Вы спорите"
            } else {
                tvAuthor.text = "${disp.disputAuthorName} спорит"
            }
            if (disp.disputBetType.equals("ratio")) {

                tvBet.text = "Ставка: ${disp.disputBetValue} ед. рейтинга"
            } else if (disp.disputBetType.equals("money")) {
                tvBet.text = "Ставка: ${disp.disputBetValue} руб."
            } else {
                tvBet.text = "Ставка не указана"
            }
            showDelBtn(isAuthor(disp))
            if (disp.disputDefendant!=null) ivDelete.visibility=View.GONE

            itemView.setOnClickListener(onClickDisp(disp))
            ivDelete.setOnClickListener{
                onClickDel(disp)
            }
        }

        private fun onClickDisp (disp: Disput): View.OnClickListener {
            return View.OnClickListener {
                val i = Intent (act, DisputActivity::class.java).apply {
                    putExtra (MainActivity.OPEN_DISP, disp)
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
        }


        private fun isAuthor (disp: Disput) : Boolean {
            return disp.disputAuthor == Firebase.auth.uid
        }

        private fun showDelBtn (isAuthor: Boolean){
            if (isAuthor) {
                binding.ivDelete.visibility = View.VISIBLE
            } else {
                binding.ivDelete.visibility = View.GONE
            }
        }
    }

}