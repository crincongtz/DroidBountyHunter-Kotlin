package edu.training.droidbountyhunterkotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import edu.training.droidbountyhunterkotlin.data.DatabaseBountyHunter
import edu.training.droidbountyhunterkotlin.models.Fugitivo
import kotlinx.android.synthetic.main.activity_detalle.*

class DetalleActivity : AppCompatActivity(){

    var fugitivo: Fugitivo? = null
    var database: DatabaseBountyHunter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle)
        fugitivo = intent.extras?.get("fugitivo") as Fugitivo
        // Se obtiene el nombre del fugitivo del intent y se usa como título
        title = fugitivo!!.name + " - " + fugitivo!!.id
        // Se identifica si es Fugitivo o capturado para el mensaje...
        if (fugitivo!!.status == 0){
            etiquetaMensaje.text = "El fugitivo sigue suelto..."
        }else{
            etiquetaMensaje.text = "Atrapado!!!"
            botonCapturar.visibility = View.GONE
        }
    }

    fun capturarFugitivoPresionado(view: View){
        database = DatabaseBountyHunter(this)
        fugitivo!!.status = 1
        database!!.actualizarFugitivo(fugitivo!!)
        setResult(0)
        finish()
    }

    fun eliminarFugitivoPresionado(view: View){
        database = DatabaseBountyHunter(this)
        database!!.borrarFugitivo(fugitivo!!)
        setResult(0)
        finish()
    }

}