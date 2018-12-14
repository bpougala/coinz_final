package biko.pougala.coinz

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button

class SelectMenu: AppCompatActivity() {

    private var easyButton: Button? = null
    private var difficultButton: Button? = null


    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        setContentView(R.layout.menu_select_activity)

        easyButton = findViewById(R.id.easyButton)
        difficultButton = findViewById(R.id.advancedButton)

        easyButton?.setOnClickListener{
            coins.gameMode = "easy"
            val intent = Intent(this@SelectMenu, MainActivity::class.java)
            startActivity(intent)
        }

        difficultButton?.setOnClickListener{
            coins.gameMode = "difficult"
            val intent = Intent(this@SelectMenu, MainActivity::class.java)
            startActivity(intent)
        }


    }
}