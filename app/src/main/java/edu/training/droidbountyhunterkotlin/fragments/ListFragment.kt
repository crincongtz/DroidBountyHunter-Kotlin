package edu.training.droidbountyhunterkotlin.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.activityViewModels
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import edu.training.droidbountyhunterkotlin.FugitivoViewModel
import edu.training.droidbountyhunterkotlin.R
import edu.training.droidbountyhunterkotlin.data.DatabaseBountyHunter
import edu.training.droidbountyhunterkotlin.models.Fugitivo
import edu.training.droidbountyhunterkotlin.network.JSONUtils
import edu.training.droidbountyhunterkotlin.network.NetworkServices
import edu.training.droidbountyhunterkotlin.network.OnTaskListener
import kotlinx.coroutines.launch

const val SECTION_NUMBER : String = "section_number"

class ListFragment : Fragment() {

    private val viewModel: FugitivoViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Se hace referencia al Fragment generado por XML en los Layouts y
        // se instancia en una View...
        return inflater.inflate(R.layout.fragment_list, container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val modo = requireArguments()[SECTION_NUMBER] as Int
        val fugitivosCapturadosList = view.findViewById<ListView>(R.id.listaFugitivosCapturados)
        actualizarDatos(fugitivosCapturadosList, modo)
        fugitivosCapturadosList.setOnItemClickListener { adapterView, view, position, id ->
            val fugitivoList = fugitivosCapturadosList.tag as Array<Fugitivo>
            viewModel.selectFugitivo(fugitivoList[position])
        }
    }

    private fun actualizarDatos(listView: ListView?, modo: Int) {
        val database = DatabaseBountyHunter(requireContext())
        val fugitivos = database.obtenerFugitivos(modo)
        if (fugitivos.isNotEmpty()){
            val values = ArrayList<String?>()
            fugitivos.mapTo(values){ it.name }
            val adaptador = ArrayAdapter<String>(requireContext(), R.layout.item_fugitivo_list, values)
            listView!!.adapter = adaptador
            listView.tag = fugitivos
        } else { // La base de datos se encuentra vacía
            if (modo == 0){
                lifecycleScope.launch {
                    NetworkServices.execute("Fugitivos", object: OnTaskListener {
                        override fun tareaCompletada(respuesta: String) {
                            JSONUtils.parsearFugitivos(respuesta, context!!)
                            actualizarDatos(listView, modo)
                        }

                        override fun tareaConError(codigo: Int, mensaje: String,
                                                   error: String) {
                            Toast.makeText(
                                context,
                                "Ocurrio un problema con el WebService!!! --- Código de error: $codigo \nMensaje: $mensaje",
                                Toast.LENGTH_LONG).show()
                        }
                    })
                }

            }

        }

    }

}
