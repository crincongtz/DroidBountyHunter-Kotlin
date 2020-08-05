package edu.training.droidbountyhunterkotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detalle.*

class DetalleActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle)
        // Se obtiene el nombre del fugitivo del intent y se usa como título
        title = intent.extras?.get("titulo") as CharSequence?
        // Se identifica si es Fugitivo o capturado para el mensaje...
        if (intent.extras?.get("modo") == 0){
            etiquetaMensaje.text = "El fugitivo sigue suelto..."
        }else{
            etiquetaMensaje.text = "Atrapado!!!"
        }
    }
}