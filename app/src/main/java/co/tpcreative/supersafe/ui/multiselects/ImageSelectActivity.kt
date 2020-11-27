package co.tpcreative.supersafe.ui.multiselects
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.database.ContentObserver
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.*
import android.widget.Toast
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.extension.sizeInKb
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.ImageModel
import co.tpcreative.supersafe.model.MimeTypeFile
import co.tpcreative.supersafe.ui.multiselects.adapter.CustomImageSelectAdapter
import kotlinx.android.synthetic.main.activity_image_select.*
import java.io.File
import java.util.*

class ImageSelectActivity : HelperActivity() {
    private var images: ArrayList<ImageModel>? = null
    private var album: String? = null
    private var adapter: CustomImageSelectAdapter? = null
    private var actionMode: ActionMode? = null
    private var countSelected = 0
    private var observer: ContentObserver? = null
    private var handler: Handler? = null
    private var thread: Thread? = null
    private val projection: Array<String> = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_select)
        setView(findViewById(R.id.layout_image_select))
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val intent: Intent? = intent
        if (intent == null) {
            finish()
        }
        album = intent?.getStringExtra(Navigator.INTENT_EXTRA_ALBUM)
        text_view_error?.visibility = View.INVISIBLE
        grid_view_image_select?.setOnItemClickListener { parent, view, position, id ->
            if (actionMode == null) {
                actionMode = toolbar?.startActionMode(callback)
            }
            toggleSelection(position)
            actionMode?.title = countSelected.toString() + " " + getString(R.string.selected)
        }
    }

    override fun onStart() {
        super.onStart()
        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Navigator.PERMISSION_GRANTED -> {
                        loadImages()
                    }
                    Navigator.FETCH_STARTED -> {
                        progress_bar_image_select?.visibility = View.VISIBLE
                        grid_view_image_select?.visibility = View.INVISIBLE
                    }
                    Navigator.FETCH_COMPLETED -> {

                        /*
                        If adapter is null, this implies that the loaded images will be shown
                        for the first time, hence send FETCH_COMPLETED message.
                        However, if adapter has been initialised, this thread was run either
                        due to the activity being restarted or content being changed.
                         */if (adapter == null) {
                            adapter = CustomImageSelectAdapter(applicationContext, images)
                            grid_view_image_select?.adapter = adapter
                            progress_bar_image_select?.visibility = View.INVISIBLE
                            grid_view_image_select?.visibility = View.VISIBLE
                            orientationBasedUI(resources.configuration.orientation)
                        } else {
                            adapter?.notifyDataSetChanged()
                            /*
                            Some selected images may have been deleted
                            hence update action mode title
                             */if (actionMode != null) {
                                countSelected = msg.arg1
                                actionMode?.title = countSelected.toString() + " " + getString(R.string.selected)
                            }
                        }
                    }
                    Navigator.ERROR -> {
                        progress_bar_image_select?.visibility = View.INVISIBLE
                        text_view_error?.visibility = View.VISIBLE
                    }
                    else -> {
                        super.handleMessage(msg)
                    }
                }
            }
        }
        observer = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                loadImages()
            }
        }
        contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, observer!!)
        checkPermission()
    }

    override fun onResume() {
        super.onResume()
        onRegisterHomeWatcher()
    }

    override fun onStop() {
        super.onStop()
        stopThread()
        observer?.let { contentResolver.unregisterContentObserver(it) }
        observer = null
        if (handler != null) {
            handler?.removeCallbacksAndMessages(null)
            handler = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (actionBar != null) {
            actionBar?.setHomeAsUpIndicator(0)
        }
        images = null
        if (adapter != null) {
            adapter?.releaseResources()
        }
        grid_view_image_select?.onItemClickListener = null
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        orientationBasedUI(newConfig.orientation)
    }


    private fun orientationBasedUI(orientation: Int) {
        val metrics = DisplayMetrics()
        if (Build.VERSION_CODES.R>Build.VERSION.SDK_INT){
            val windowManager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getMetrics(metrics)
        }else{
            this.display?.getRealMetrics(metrics)
        }
        if (adapter != null) {
            val size: Int = if (orientation == Configuration.ORIENTATION_PORTRAIT) metrics.widthPixels / 3 else metrics.widthPixels / 5
            adapter?.setLayoutParams(size)
        }
        grid_view_image_select?.numColumns = if (orientation == Configuration.ORIENTATION_PORTRAIT) 3 else 5
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> {
                false
            }
        }
    }

    private val callback: ActionMode.Callback? = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            val menuInflater = mode?.menuInflater
            menuInflater?.inflate(R.menu.menu_contextual_action_bar, menu)
            actionMode = mode
            countSelected = 0
            window.statusBarColor = androidx.core.content.ContextCompat.getColor(applicationContext, R.color.material_orange_900)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            val i = item?.itemId
            if (i == R.id.menu_item_add_image) {
                sendIntent()
                return true
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            if (countSelected > 0) {
                deselectAll()
            }
            actionMode = null
            val themeApp: co.tpcreative.supersafe.model.ThemeApp? = co.tpcreative.supersafe.model.ThemeApp.getInstance()?.getThemeInfo()
            window.statusBarColor = androidx.core.content.ContextCompat.getColor(applicationContext, themeApp?.getPrimaryDarkColor()!!)
        }
    }

    private fun toggleSelection(position: Int) {
        if (!images?.get(position)?.isSelected!! && countSelected >= Navigator.limit) {
            Toast.makeText(
                    applicationContext, String.format(getString(R.string.limit_exceeded), Navigator.limit),
                    Toast.LENGTH_SHORT)
                    .show()
            return
        }
        images?.get(position)?.isSelected = !(images?.get(position)?.isSelected)!!
        if (images?.get(position)?.isSelected!!) {
            countSelected++
        } else {
            countSelected--
        }
        adapter?.notifyDataSetChanged()
    }

    private fun deselectAll() {
        var i = 0
        val l = images?.size
        while (i < l!!) {
            images?.get(i)?.isSelected = false
            i++
        }
        countSelected = 0
        adapter?.notifyDataSetChanged()
    }

    private fun getSelected(): ArrayList<ImageModel?>? {
        val selectedImages = ArrayList<ImageModel?>()
        var i = 0
        val l = images?.size
        while (i < l!!) {
            if (images?.get(i)?.isSelected!!) {
                selectedImages.add(images?.get(i))
            }
            i++
        }
        return selectedImages
    }

    private fun sendIntent() {
        val intent = Intent()
        intent.putParcelableArrayListExtra(Navigator.INTENT_EXTRA_IMAGES, getSelected())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun loadImages() {
        startThread(ImageLoaderRunnable())
    }

    /*Loading image*/
    private inner class ImageLoaderRunnable : Runnable {
        override fun run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            /*
            If the adapter is null, this is first time this activity's view is
            being shown, hence send FETCH_STARTED message to show progress bar
            while images are loaded from phone
             */if (adapter == null) {
                sendMessage(Navigator.FETCH_STARTED)
            }
            var file: File?
            val selectedImages = HashSet<Long?>()
            if (images != null) {
                var image: ImageModel?
                var i = 0
                val l = images?.size
                while (i < l!!) {
                    image = images?.get(i)
                    file = File(image?.path)
                    if (file.exists() && image?.isSelected!!) {
                        selectedImages.add(image?.id)
                    }
                    i++
                }
            }
            Utils.Log(TAG, "Album name $album")
            val queryUri: Uri = MediaStore.Files.getContentUri("external")
            val cursor = contentResolver.query(queryUri, projection,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " =?", arrayOf(album), MediaStore.Images.Media.DATE_ADDED)
            if (cursor == null) {
                sendMessage(Navigator.ERROR)
                return
            }

            /*
            In case this runnable is executed to onChange calling loadImages,
            using countSelected variable can result in a race condition. To avoid that,
            tempCountSelected keeps track of number of selected images. On handling
            FETCH_COMPLETED message, countSelected is assigned value of tempCountSelected.
             */
            var tempCountSelected = 0
            val temp = ArrayList<ImageModel>(cursor.count)
            if (cursor.moveToLast()) {
                do {
                    if (Thread.interrupted()) {
                        return
                    }
                    val id = cursor.getLong(cursor.getColumnIndex(projection[0]))
                    val name = cursor.getString(cursor.getColumnIndex(projection[1]))
                    val path = cursor.getString(cursor.getColumnIndex(projection[2]))
                    val isSelected = selectedImages.contains(id)
                    if (isSelected) {
                        tempCountSelected++
                    }
                    if (path != null) {
                        file = File(path)
                        if (file.exists()) {
                            val extensionFile: String? = Utils.getFileExtension(file.absolutePath)
                            val mimeTypeFile: MimeTypeFile? = Utils.mediaTypeSupport()[extensionFile]
                            if (mimeTypeFile != null) {
                                if (file.sizeInKb>0){
                                    temp.add(ImageModel(id, name, path, isSelected))
                                }
                            }
                        } else {
                            Utils.Log(TAG, "value " + file.absolutePath)
                        }
                    }
                } while (cursor.moveToPrevious())
            }
            cursor.close()
            if (images == null) {
                images = ArrayList()
            }
            images?.clear()
            images?.addAll(temp)
            sendMessage(Navigator.FETCH_COMPLETED, tempCountSelected)
        }
    }

    private fun startThread(runnable: Runnable?) {
        stopThread()
        thread = Thread(runnable)
        thread?.start()
    }

    private fun stopThread() {
        if (thread == null || !thread?.isAlive!!) {
            return
        }
        thread?.interrupt()
        try {
            thread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun sendMessage(what: Int, arg1: Int = 0) {
        if (handler == null) {
            return
        }
        val message = handler?.obtainMessage()
        message?.what = what
        message?.arg1 = arg1
        message?.sendToTarget()
    }

    override fun permissionGranted() {
        sendMessage(Navigator.PERMISSION_GRANTED)
    }

    override fun hideViews() {
        progress_bar_image_select?.visibility = View.INVISIBLE
        grid_view_image_select?.visibility = View.INVISIBLE
    }

    companion object {
        private val TAG = ImageSelectActivity::class.java.simpleName
    }
}