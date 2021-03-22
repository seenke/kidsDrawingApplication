package si.uni_lj.fri.pbd.drawingapplication

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup

class CanvasFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun onColorChange(color: Int) {
        val canvas = view?.findViewById<CustomView>(R.id.canvasView)
        canvas?.onColorChange(color)
    }

    fun onBrushSizeChange(size: Float?) {
        val canvas = view?.findViewById<CustomView>(R.id.canvasView)
        canvas?.onBrushSizeChange(size)
        Log.i("Change", "$size ")
    }

    fun onUndo () {
        val canvas = view?.findViewById<CustomView>(R.id.canvasView)
        canvas?.onUndo()
    }

    fun onSave () {
        val canvas = view?.findViewById<CustomView>(R.id.canvasView)
        canvas?.onSave()
    }

    fun onBgChange(bgBitmap: Bitmap) {
        val canvas = view?.findViewById<CustomView>(R.id.canvasView)
        canvas?.onBgChange(bgBitmap)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_canvas, container, false)
        val currentView = view.findViewById<CustomView>(R.id.canvasView)
        return view
    }


    interface Listener {
        fun onColorChange(color: Int)
    }
}