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
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.AlbumMultiItems
import co.tpcreative.supersafe.model.MimeTypeFile
import co.tpcreative.supersafe.ui.multiselects.adapter.CustomAlbumSelectAdapter
import kotlinx.android.synthetic.main.activity_album_select.*
import java.io.File
import java.util.*

class AlbumSelectActivity : HelperActivity() {
    private var albums: ArrayList<AlbumMultiItems>? = null
    private var progressBar: ProgressBar? = null
    private var adapter: CustomAlbumSelectAdapter? = null
    private var observer: ContentObserver? = null
    private var handler: Handler? = null
    private var thread: Thread? = null

    private val projection: Array<String?> = arrayOf(
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_select)
        setView(findViewById(R.id.layout_album_select))
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setTitle(R.string.album_view)
        val intent: Intent? = intent
        if (intent == null) {
            finish()
        }
        Navigator.limit = intent?.getIntExtra(Navigator.INTENT_EXTRA_LIMIT, Navigator.DEFAULT_LIMIT)!!
        text_view_error?.visibility = View.INVISIBLE
        progressBar = findViewById<View?>(R.id.progress_bar_album_select) as ProgressBar?
        grid_view_album_select?.setOnItemClickListener { parent, view, position, id ->
            val mIntent = Intent(applicationContext, ImageSelectActivity::class.java)
            mIntent.putExtra(Navigator.INTENT_EXTRA_ALBUM, albums?.get(position)?.name)
            startActivityForResult(mIntent, Navigator.REQUEST_CODE)
        }
    }

    override fun onStart() {
        super.onStart()
        handler = object : Handler(Looper.myLooper()!!) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Navigator.PERMISSION_GRANTED -> {
                        loadAlbums()
                    }
                    Navigator.FETCH_STARTED -> {
                        progressBar?.visibility = View.VISIBLE
                        grid_view_album_select?.visibility = View.INVISIBLE
                    }
                    Navigator.FETCH_COMPLETED -> {
                        if (adapter == null) {
                            adapter = CustomAlbumSelectAdapter(applicationContext, albums)
                            grid_view_album_select?.setAdapter(adapter)
                            progressBar?.visibility = View.INVISIBLE
                            grid_view_album_select?.visibility = View.VISIBLE
                            orientationBasedUI(resources.configuration.orientation)
                        } else {
                            adapter?.notifyDataSetChanged()
                        }
                    }
                    Navigator.ERROR -> {
                        progressBar?.visibility = View.INVISIBLE
                        text_view_error?.visibility = View.VISIBLE
                    }
                    else -> {
                        super.handleMessage(msg)
                    }
                }
            }
        }
        observer = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                loadAlbums()
            }
        }
        contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, observer!!)
        checkPermission()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
        stopThread()
        contentResolver.unregisterContentObserver(observer!!)
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
        albums = null
        if (adapter != null) {
            adapter?.releaseResources()
        }
        grid_view_album_select?.onItemClickListener = null
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
            val size: Int = if (orientation == Configuration.ORIENTATION_PORTRAIT) metrics.widthPixels / 2 else metrics.widthPixels / 4
            adapter?.setLayoutParams(size)
        }
        grid_view_album_select?.numColumns = (if (orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 4)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Navigator.REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> {
                false
            }
        }
    }

    private fun loadAlbums() {
        startThread(AlbumLoaderRunnable())
    }

    private inner class AlbumLoaderRunnable : Runnable {
        override fun run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            if (adapter == null) {
                sendMessage(Navigator.FETCH_STARTED)
            }
            val selection: String = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                    + " OR "
                    + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                    + " OR "
                    + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO)
            val queryUri: Uri = MediaStore.Files.getContentUri("external")
            val cursor = applicationContext.contentResolver
                    .query(queryUri, projection,
                            selection, null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC")
            if (cursor == null) {
                sendMessage(Navigator.ERROR)
                return
            }
            val temp: ArrayList<AlbumMultiItems> = ArrayList<AlbumMultiItems>(cursor.count)
            val albumSet = HashSet<Long?>()
            var file: File?
            if (cursor.moveToLast()) {
                do {
                    if (Thread.interrupted()) {
                        return
                    }
                    val albumId = cursor.getLong(cursor.getColumnIndex(projection.get(0)))
                    val album = cursor.getString(cursor.getColumnIndex(projection.get(1)))
                    val image = cursor.getString(cursor.getColumnIndex(projection.get(2)))
                    if (!albumSet.contains(albumId)) {
                        /*
                        It may happen that some image file paths are still present in cache,
                        though image file does not exist. These last as long as media
                        scanner is not run again. To avoid get such image file paths, check
                        if image file exists.
                         */
                        file = File(image)
                        if (file.exists()) {
                            val extensionFile: String? = Utils.getFileExtension(file.absolutePath)
                            val mimeTypeFile: MimeTypeFile? = Utils.mediaTypeSupport()[extensionFile]
                            if (mimeTypeFile != null && album != null) {
                                temp.add(AlbumMultiItems(album, image))
                                albumSet.add(albumId)
                            } else {
                                Utils.Log(TAG, "value $extensionFile")
                            }
                        }
                    }
                } while (cursor.moveToPrevious())
            }
            cursor.close()
            if (albums == null) {
                albums = ArrayList<AlbumMultiItems>()
            }
            albums?.clear()
            albums?.addAll(temp)
            sendMessage(Navigator.FETCH_COMPLETED)
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

    private fun sendMessage(what: Int) {
        if (handler == null) {
            return
        }
        val message = handler?.obtainMessage()
        message?.what = what
        message?.sendToTarget()
    }

    override fun permissionGranted() {
        val message = handler?.obtainMessage()
        message?.what = Navigator.PERMISSION_GRANTED
        message?.sendToTarget()
    }

    override fun hideViews() {
        progressBar?.visibility = View.INVISIBLE
        grid_view_album_select?.visibility = View.INVISIBLE
    }

    companion object {
        private val TAG = AlbumSelectActivity::class.java.simpleName
    }
}