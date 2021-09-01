package com.sejigner.glee

import android.app.Activity
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sejigner.glee.Scroll.isPainting
import com.sejigner.glee.paint.CustomView
import kotlinx.android.synthetic.main.activity_canvas.*
import kotlinx.android.synthetic.main.fragment_home.*
import petrov.kristiyan.colorpicker.ColorPicker
import petrov.kristiyan.colorpicker.ColorPicker.OnFastChooseColorListener
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


object Scroll {
    var isPainting: Boolean = true
}

class CanvasActivity : AppCompatActivity() {
    private var mCustomView: CustomView? = null
    private var colorList = ArrayList<String>()
    var title : String ?= null
    var author : String ?= null
    var content : String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canvas)
        transparentStatusAndNavigation()
        mCustomView = findViewById<View>(R.id.customView) as CustomView
        setColorList()
        val metrics = DisplayMetrics()
        val seek = findViewById<SeekBar>(R.id.seek_bar_brush_size)
        initWork()

        val transription = findViewById<CustomView>(R.id.customView)
        transription.setBackgroundColor(resources.getColor(R.color.white))

        tv_canvas_save.setOnClickListener {
            val bitmap = getScreenShotFromView(transription)

            if (bitmap != null) {

                saveMediaToStorage(bitmap)
            }
        }




        windowManager.defaultDisplay.getMetrics(metrics)
        /*
        customView!!.doOnLayout {
            var width = it.measuredWidth
            var height = it.measuredHeight
            customView!!.init(width, height)
        }
        */
        Toast.makeText(this, "원하는 글씨체를 선택하고 따라써보세요.", Toast.LENGTH_LONG).show()

        btn_toggle_scroll.setOnClickListener {
            if (isPainting) {
                btn_toggle_scroll.setImageResource(R.drawable.btn_scroll)
                mCustomView!!.setOnTouchListener { view, event -> // 터치 이벤트 제거 (필사 기능 off)
                    true
                }
                isPainting = false
            } else {
                btn_toggle_scroll.setImageResource(R.drawable.btn_draw)
                mCustomView!!.setOnTouchListener { view, event -> // 터치 이벤트
                    false
                }
                isPainting = true
            }

        }

        btn_canvas_back.setOnClickListener {
            onBackPressed()
        }


        rb_canvas_cafe24SurroundAir.setOnClickListener {
            tv_canvas_work.typeface = Typeface.createFromAsset(applicationContext.assets, "fonts/cafe24_surround_air.ttf")
        }

        rb_canvas_aritaBuri.setOnClickListener {
            tv_canvas_work.typeface = Typeface.createFromAsset(applicationContext.assets, "fonts/arita_buri.otf")
        }

        rb_canvas_mapoFlowerIsland.setOnClickListener {
            tv_canvas_work.typeface = Typeface.createFromAsset(applicationContext.assets, "fonts/mapo_flower_island.ttf")
        }

        rb_canvas_hambaksnow.setOnClickListener {
            tv_canvas_work.typeface = Typeface.createFromAsset(applicationContext.assets, "fonts/hambaksnow.ttf")
        }

        view_btn_undo.setOnClickListener {
            btn_undo.performClick()
            btn_undo.setPressed(true)
            btn_undo.invalidate()
            btn_undo.setPressed(false)
            btn_undo.invalidate()
        }
        btn_undo.setOnClickListener { mCustomView!!.onClickUndo() }

        view_btn_redo.setOnClickListener {
            btn_redo.performClick()
            btn_redo.setPressed(true)
            btn_redo.invalidate()
            btn_redo.setPressed(false)
            btn_redo.invalidate()
        }
        btn_redo.setOnClickListener { mCustomView!!.onClickRedo() }

        btn_color_change.setOnClickListener {
            val colorPicker = ColorPicker(this@CanvasActivity)
            colorPicker.setOnFastChooseColorListener(object : OnFastChooseColorListener {
                override fun setOnFastChooseColorListener(position: Int, color: Int) {
                    // get the integer value of color
                    // selected from the dialog box and
                    // set it as the stroke color
                    customView.setColor(color)
                    btn_color_change.setColorFilter(color)
                }

                override fun onCancel() {
                    colorPicker.dismissDialog()
                }
            }) // set the number of color columns
                // you want  to show in dialog.
                .setColumns(4) // set a default color selected
                // in the dialog
                .setColors(colorList)
                .setRoundColorButton(true)
                .setTitle("필사에 이용할 잉크 색을 골라주세요!")
                .show()
        }
        view_btn_color.setOnClickListener { btn_color_change.performClick() }

        seek?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var progressChanged = 0
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                progressChanged = progress
                tv_progress_seek_bar.setText(getString(R.string.draw_thickness, progress))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                tv_progress_seek_bar.setText(getString(R.string.draw_thickness, seek.progress))
                progressChanged = seek.progress

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                /*
                Toast.makeText(this@CanvasActivity,
                "획 두께가 " + seek.progress + "포인트에요.",
                Toast.LENGTH_SHORT).show()
                */
                tv_progress_seek_bar.setText(getString(R.string.draw_thickness, seek.progress))
                Log.d("Debug", "brush : $progressChanged pt")
                mCustomView!!.setBrushSize(progressChanged.toFloat())
                mCustomView!!.setLastBrushSize(progressChanged.toFloat())


            }
        })
    }

    private fun getScreenShotFromView(v: View): Bitmap? {
        // create a bitmap object
        var screenshot: Bitmap? = null
        try {
            // inflate screenshot object
            // with Bitmap.createBitmap it
            // requires three parameters
            // width and height of the view and
            // the background color
            screenshot = Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
            // Now draw this bitmap on a canvas
            val canvas = Canvas(screenshot)
            v.draw(canvas)
        } catch (e: Exception) {
            Log.e("GFG", "Failed to capture screenshot because:" + e.message)
        }
        // return the bitmap
        return screenshot
    }

    // this method saves the image to gallery
    private fun saveMediaToStorage(bitmap: Bitmap) {
        // Generating a file name
        val filename = "${System.currentTimeMillis()}.jpg"

        // Output stream
        var fos: OutputStream? = null

        // For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // getting the contentResolver
            this.contentResolver?.also { resolver ->

                // Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    // putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                // Inserting the contentValues to
                // contentResolver and getting the Uri
                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                // Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            // These for devices running on android < Q
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            // Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this , "Captured View and saved to Gallery" , Toast.LENGTH_SHORT).show()
        }
    }

    private fun initWork() {
        if(intent.getStringExtra("TITLE")!=null) {
            title = intent.getStringExtra("TITLE")
            author = intent.getStringExtra("AUTHOR")
            content = intent.getStringExtra("CONTENT")

            tv_canvas_title.text = title
            tv_canvas_author.text = author
            tv_canvas_work.text = content

        }
    }

    private fun setColorList() {
        colorList.add("#000000")
        colorList.add("#FFFFFF")
        colorList.add("#FF7777")
        colorList.add("#FFA427")
        colorList.add("#B7E831")
        colorList.add("#74A9FF")
        colorList.add("#2B45D6")
        colorList.add("#7C55FF")
        colorList.add("#FFCD93")
        colorList.add("#00ED52")
        colorList.add("#74DFFF")
        colorList.add("#ACACF8")
        colorList.add("#1F1F1F")
        colorList.add("#404040")
        colorList.add("#797979")
        colorList.add("#C4C4C4")
        Log.d("colorPicker", "color check-the last color: " + colorList.get(15))


    }

    private fun Activity.transparentStatusAndNavigation(
        systemUiScrim: Int = Color.parseColor("#40000000") // 25% black
    ) {
        var systemUiVisibility = 0
        // Use a dark scrim by default since light status is API 23+
        var statusBarColor = systemUiScrim
        //  Use a dark scrim by default since light nav bar is API 27+
        val winParams = window.attributes


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            statusBarColor = Color.TRANSPARENT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            systemUiVisibility = systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.decorView.systemUiVisibility = systemUiVisibility
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            winParams.flags = winParams.flags or
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            winParams.flags = winParams.flags and
                    (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS).inv()
            window.statusBarColor = statusBarColor
        }

        window.attributes = winParams
    }
}