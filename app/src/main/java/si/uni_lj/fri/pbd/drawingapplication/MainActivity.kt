package si.uni_lj.fri.pbd.drawingapplication

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Path
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import dev.sasikanth.colorsheet.ColorSheet
import java.io.FileNotFoundException

class MainActivity : AppCompatActivity(), BrushPickerFragment.NoticeDialogListener {

    lateinit var toolbar: ActionBar

    var colors = IntArray(6)
    var color: Int = Color.BLUE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = supportActionBar!!
        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigation_bar)
        bottomNavigation.setOnNavigationItemSelectedListener (mOnNavigationItemSelectedListener)

        colors[0] = Color.BLACK
        colors[1] = Color.YELLOW
        colors[2] = Color.BLUE
        colors[3] = Color.GREEN
        colors[4] = Color.GRAY
        colors[5] = Color.RED

        val homeFragment = CanvasFragment()
        openFragment(homeFragment)
        toolbar.title = "Drawing application"

    }

    companion object {
        private const val RESULT_LOAD_IMAGE = 1
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when(item.itemId) {
            R.id.navigation_color_picker -> {
                ColorSheet().colorPicker(
                    colors = colors,
                    listener = { color ->
                        val fragment = supportFragmentManager.findFragmentById(R.id.androidx_navigation_fragment_NavHostFragment) as CanvasFragment
                        fragment.onColorChange(color)
                    }
                ).show(supportFragmentManager)
                item.isChecked = false
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_brush_picker -> {
                val dialog = BrushPickerFragment()
                dialog.show(supportFragmentManager, "BrushPickerFragment")
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_save -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.androidx_navigation_fragment_NavHostFragment) as CanvasFragment
                fragment.onSave()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_load -> {

                val photoPickerIntent = Intent(Intent.ACTION_PICK)
                photoPickerIntent.type = "image/*"
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMAGE)

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_undo -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.androidx_navigation_fragment_NavHostFragment) as CanvasFragment
                fragment.onUndo()
                return@OnNavigationItemSelectedListener true
            }
        }
        return@OnNavigationItemSelectedListener false
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.androidx_navigation_fragment_NavHostFragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onDialogPositiveClick(brushSize: Float?) {
        val fragment = supportFragmentManager.findFragmentById(R.id.androidx_navigation_fragment_NavHostFragment) as CanvasFragment
        fragment.onBrushSizeChange(brushSize)
    }

    override fun onDialogNegativeClick() {
        return
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            when(requestCode) {
                RESULT_LOAD_IMAGE -> {
                    try {
                        val imageUri = data?.data
                        val imageStream = contentResolver.openInputStream(imageUri!!)
                        val selectedImage = BitmapFactory.decodeStream(imageStream)

                        val fragment = supportFragmentManager.findFragmentById(R.id.androidx_navigation_fragment_NavHostFragment) as CanvasFragment
                        fragment.onBgChange(selectedImage)

                    }catch (e: FileNotFoundException) {
                        Toast.makeText(this, "Please select image", Toast.LENGTH_LONG).show()
                    }
                }
            }

        }
    }

}