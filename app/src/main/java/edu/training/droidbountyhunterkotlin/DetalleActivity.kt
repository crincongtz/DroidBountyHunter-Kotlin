package edu.training.droidbountyhunterkotlin

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import edu.training.droidbountyhunterkotlin.utils.PictureTools
import androidx.lifecycle.lifecycleScope
import edu.training.droidbountyhunterkotlin.data.DatabaseBountyHunter
import edu.training.droidbountyhunterkotlin.models.Fugitivo
import edu.training.droidbountyhunterkotlin.network.NetworkServices
import edu.training.droidbountyhunterkotlin.network.OnTaskListener
import kotlinx.coroutines.launch
import org.json.JSONObject
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat

class DetalleActivity : AppCompatActivity(){

    private var UDID: String? = ""
    var fugitivo: Fugitivo? = null
    var database: DatabaseBountyHunter? = null
    private var direccionImagen: Uri? = null
    private val REQUEST_CODE_PHOTO_IMAGE = 1787

    var botonCapturar: Button? = null
    var pictureFugitive: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        @SuppressLint("HardwareIds")
        UDID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle)
        fugitivo = intent.extras?.get("fugitivo") as Fugitivo
        // Se obtiene el nombre del fugitivo del intent y se usa como título
        title = fugitivo!!.name + " - " + fugitivo!!.id
        // Se identifica si es Fugitivo o capturado para el mensaje...
        val etiquetaMensaje = findViewById<TextView>(R.id.etiquetaMensaje)
        botonCapturar = findViewById(R.id.botonCapturar)
        pictureFugitive = findViewById(R.id.pictureFugitive)
        if (fugitivo!!.status == 0){
            etiquetaMensaje.text = "El fugitivo sigue suelto..."
        }else{
            etiquetaMensaje.text = "Atrapado!!!"
            botonCapturar?.visibility = View.GONE
            val bitmap = fugitivo!!.photo?.let {
                PictureTools.decodeSampledBitmapFromUri(
                    it,
                    200,200)
            }
            pictureFugitive?.setImageBitmap(bitmap)
        }
    }

    fun capturarFugitivoPresionado(view: View){
        database = DatabaseBountyHunter(this)
        fugitivo!!.status = 1
        if (fugitivo!!.photo.isNullOrEmpty()){
            Toast.makeText(this,
                "Es necesario tomar la foto antes de capturar al fugitivo",
                Toast.LENGTH_LONG).show()
            return
        }
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

        botonCapturar?.visibility = View.GONE
        val botonEliminar = findViewById<Button>(R.id.botonEliminar)
        botonEliminar.visibility = View.GONE
        setResult(1)
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

    fun OnFotoClick(view: View){
        if (PictureTools.permissionReadMemmory(this)){
            obtenFotoDeCamara()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PictureTools.REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Log.d("RequestPermissions", "Camera - Granted")
                obtenFotoDeCamara()
            } else {
                Log.d("RequestPermissions", "Camera - Not Granted")
            }
        }
    }

    private fun obtenFotoDeCamara() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        direccionImagen = PictureTools.getOutputMediaFileUri(this, MEDIA_TYPE_IMAGE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, direccionImagen)
        startActivityForResult(intent, REQUEST_CODE_PHOTO_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PHOTO_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                fugitivo!!.photo = PictureTools.currentPhotoPath
                val bitmap = PictureTools
                    .decodeSampledBitmapFromUri(PictureTools.currentPhotoPath, 200, 200)
                pictureFugitive?.setImageBitmap(bitmap)
            }
        }
    }


}