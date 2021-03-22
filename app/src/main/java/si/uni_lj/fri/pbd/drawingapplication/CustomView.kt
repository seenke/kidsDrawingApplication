package si.uni_lj.fri.pbd.drawingapplication

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.OutputStream


class CustomView @JvmOverloads constructor(context: Context,
                                           attrs: AttributeSet? = null
                                           ) : SurfaceView(context, attrs) {

    private lateinit var canvasPaint: Paint
    private lateinit var paint: Paint
    private var canvas: Canvas? = null
    private var canvasBitmap: Bitmap? = null
    var color: Int = Color.BLACK
    private var brushSize : Float? = 1.toFloat()

    private var currentPath: MyPath
    private var currentPathList: ArrayList<MyPath> = ArrayList()

    init {
        setupPaint(color)
        setupBrush()
        currentPath = MyPath(color, brushSize)
    }

    inner class MyPath(var color:Int, var brushSize: Float?) : Path() {

    }

    override fun onSaveInstanceState(): Parcelable? {
        val myState = super.onSaveInstanceState()
        return myState
    }

    fun onColorChange(color: Int) {
        this.color = color
        setupPaint(color= this.color)
        currentPath.color = this.color
    }

    fun onBrushSizeChange(brushSize: Float?) {
        Log.i("Brush size changed !!", "$brushSize")
        setupBrush(brushSize)
        currentPath.brushSize = this.brushSize
    }

    fun onUndo() {
        Log.i("Size", " ${currentPathList.size}")
        if (this.currentPathList.size > 1) {
            this.currentPathList.removeAt(this.currentPathList.size - 1)
            this.currentPathList.removeAt(this.currentPathList.size - 1)
        }
    }


    private fun setupPaint(color: Int) {
        paint = Paint()
        paint.color = color
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND

        canvasPaint = Paint(Paint.DITHER_FLAG)
    }

    private fun setupBrush(newSize: Float? = 1f) {
        if (newSize != null) {
            brushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, resources.displayMetrics)
            paint.strokeWidth = brushSize as Float
        }
    }




    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawBitmap(canvasBitmap!!, 0f, 0f, paint)
        currentPathList.forEach { path ->
            setupPaint(path.color)
            paint.strokeWidth = path.brushSize as Float
            canvas?.drawPath(path, paint)
        }

        invalidate()
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event==null) {
            Log.i("Event:", "null")
            return false
        }

        val x = event.x
        val y = event.y

        Log.i("Current position", "$x, $y")

        when(event.action) {
            MotionEvent.ACTION_UP -> {
                currentPathList.add(currentPath)
                currentPath = MyPath(color, brushSize)
            }
            MotionEvent.ACTION_DOWN -> {
                currentPathList.add(currentPath)
                currentPath.reset()
                currentPath.moveTo(x, y)
            }
            MotionEvent.ACTION_MOVE -> {
                currentPath.lineTo(x, y)
            }
            else -> {
                return false
            }
        }

        invalidate()

        return true

    }

    fun getBitmapFromView(view: View): Bitmap? {
        //Define a bitmap with the same size as the view
        val returnedBitmap =
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        //Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        //Get the view's background
        val bgDrawable = view.background
        if (bgDrawable != null) //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas) else  //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE)
        // draw the view on the canvas
        view.draw(canvas)
        //return the bitmap
        return returnedBitmap
    }


    fun onSave() {
        saveImage(bitmap = getBitmapFromView(view = this)!!, context = context, folderName = "DrawingApplication")
        Toast.makeText(context, "Drawing saved to gallery", Toast.LENGTH_LONG).show()
    }

    fun onBgChange(bgBitmap: Bitmap) {
        Log.i("Got new bitmap", "bg bitmap")
        val scaledBitmap = Bitmap.createScaledBitmap(bgBitmap, this.width, this.height, false)
        this.canvasBitmap = scaledBitmap
    }

    private fun saveImage(bitmap: Bitmap, context: Context, folderName: String) {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            val values = contentValues()
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + folderName)
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            // RELATIVE_PATH and IS_PENDING are introduced in API 29.

            val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                context.contentResolver.update(uri, values, null, null)
            }
        } else {
            val directory = File(Environment.getExternalStorageDirectory().toString() + separator + folderName)
            // getExternalStorageDirectory is deprecated in API 29

            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = System.currentTimeMillis().toString() + ".png"
            val file = File(directory, fileName)
            saveImageToStream(bitmap, FileOutputStream(file))
            if (file.absolutePath != null) {
                val values = contentValues()
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                // .DATA is deprecated in API 29
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }
        }
    }

    private fun contentValues() : ContentValues {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        return values
    }

    private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        Log.i("Dispatched: ", "${event?.x}")
        return super.dispatchTouchEvent(event)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(canvasBitmap!!)
    }
}