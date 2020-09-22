package edu.training.droidbountyhunterkotlin

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import edu.training.droidbountyhunterkotlin.data.DatabaseBountyHunter
import edu.training.droidbountyhunterkotlin.models.Fugitivo
import edu.training.droidbountyhunterkotlin.network.NetworkServices
import edu.training.droidbountyhunterkotlin.network.OnTaskListener
import kotlinx.android.synthetic.main.activity_detalle.*
import kotlinx.coroutines.launch
import org.json.JSONObject

class DetalleActivity : AppCompatActivity(){

    private var UDID: String? = ""
    var fugitivo: Fugitivo? = null
    var database: DatabaseBountyHunter? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        @SuppressLint("HardwareIds")
        UDID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

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

        lifecycleScope.launch {
            NetworkServices.execute("Atrapar", object: OnTaskListener {
                override fun tareaCompletada(respuesta: String) {
                    val obj = JSONObject(respuesta)
                    val mensaje = obj.optString("mensaje","")
                    mensajeDeCerrado(mensaje)
                }

                override fun tareaConError(codigo: Int, mensaje: String, error: String) {
                    Toast.makeText(applicationContext,
                        "Ocurrio un problema en la comunicación con el WebService!!!",
                        Toast.LENGTH_LONG).show()
                }
            }, UDID)
        }

        botonCapturar.visibility = View.GONE
        botonEliminar.visibility = View.GONE
        setResult(0)
    }

    fun eliminarFugitivoPresionado(view: View){
        database = DatabaseBountyHunter(this)
        database!!.borrarFugitivo(fugitivo!!)
        setResult(0)
        finish()
    }

    fun mensajeDeCerrado(mensaje: String){
        val builder = AlertDialog.Builder(this)
        builder.create()
        builder.setTitle("Alerta!!!")
            .setMessage(mensaje)
            .setOnDismissListener {
                setResult(fugitivo!!.status)
                finish()
            }.show()
    }

}
