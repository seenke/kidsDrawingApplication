package si.uni_lj.fri.pbd.drawingapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.slider.Slider
import java.lang.ClassCastException

class BrushPickerFragment : DialogFragment() {

    private lateinit var listener: NoticeDialogListener
    var brushSize: Float? = 0F

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = activity.let {
            val builder = AlertDialog.Builder(it)

            val inflater = requireActivity().layoutInflater

            builder.setView(inflater.inflate(R.layout.fragment_brush_picker, null))
                .setPositiveButton(R.string.confirm,
                    DialogInterface.OnClickListener{dialog, id ->
                        listener.onDialogPositiveClick(brushSize)
                    })
                .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, id ->
                    getDialog()?.cancel()

                    listener.onDialogNegativeClick()
                })
            builder.create()
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()

        val brushSlider = dialog?.findViewById<Slider>(R.id.brushSlider)
        Log.i("Brush slider,","$brushSlider")
        brushSlider?.addOnChangeListener { slider, value, fromUser ->
            brushSize = value
            Log.i("Changed value", "$brushSize")
        }

    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)

        try{
            listener = context as NoticeDialogListener
        }catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement NoticeDialogListener")
        }
    }

    interface NoticeDialogListener {
        fun onDialogPositiveClick(brushSize: Float?)
        fun onDialogNegativeClick()
    }

}