package com.fingerprintjs.android.playground


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.fingerprintjs.android.fingerprint.Configuration
import com.fingerprintjs.android.fingerprint.FingerprinterFactory
import com.fingerprintjs.android.playground.R.layout
import com.fingerprintjs.android.playground.fingerprinters_screen.*
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var presenter: PlaygroundPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        init(savedInstanceState)
        presenter.attachView(
            PlaygroundViewImpl(
                this
            )
        )
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        outState.putParcelable(PLAYGROUND_PRESENTER_STATE_KEY, presenter.onSaveState())
    }

    private fun init(state: Bundle?) {
        val fingerprinter =
            FingerprinterFactory.getInstance(
                applicationContext,
                Configuration(version = DEFAULT_FINGERPRINTER_VERSION)
            )
        val presenterState: Parcelable? = state?.getParcelable(PLAYGROUND_PRESENTER_STATE_KEY)
        val externalStorageDir = applicationContext.getExternalFilesDir(null)?.absolutePath
        presenter =
            PlaygroundPresenterImpl(
                fingerprinter, externalStorageDir, presenterState
            )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_about) {
            AboutDialog().show(this)
        }
        if (item.itemId == R.id.menu_share) {
            presenter.shareActionClicked { shareActionClicked(it) }
        }
        return true
    }

    private fun shareActionClicked(path: String) {
        val uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", File(path))
        val sendEmailIntent = Intent(
            Intent.ACTION_SEND, Uri.fromParts(
                "mailto", "", null
            )
        ).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(DEVELOPERS_EMAIL))
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (sendEmailIntent.resolveActivity(packageManager) != null) {
            startActivity(sendEmailIntent)
        }


    }
}

private const val PLAYGROUND_PRESENTER_STATE_KEY = "PlaygroundPresenterState"
private const val DEVELOPERS_EMAIL = "android@fingerprintjs.com"