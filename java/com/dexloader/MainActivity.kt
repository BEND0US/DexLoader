package com.dexloader

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dalvik.system.DexClassLoader
import dalvik.system.InMemoryDexClassLoader
import dalvik.system.PathClassLoader
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.File
import java.net.URL
import java.net.URLConnection
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    lateinit var tvDexContactCheck: TextView
    lateinit var tvPathRandomNumber: TextView
    lateinit var tvinMemoryRandomNumber: TextView
    lateinit var buffer: ByteArray
    lateinit var btBuffer: ByteBuffer
    val dexFilename: String = "contacts.dex"
    val pathFilename: String = "classes.dex"
    lateinit var loader: DexClassLoader
    lateinit var pathLoader: PathClassLoader

    val PERMISSION_ALL = 1
    val PERMISSIONS = arrayOf(Manifest.permission.READ_CONTACTS)

    val inMemoryDownloadURL: String = "http://192.168.1.1/classes.dex"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //Permission
        if (!hasPermissions(this, *PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL)
            return
        } else {
            proceedAfterPermission()
        }
    }

    //Permission
    fun hasPermissions(context: Context?, vararg permissions: String?): Boolean {
        if (context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission!!
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_ALL) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                proceedAfterPermission()
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dexClassLoader(dexFileName: String): DexClassLoader {
        // Create a dex dir to hold the DEX file to be loaded
        val dexFile: File = File.createTempFile("pref", ".dex")
        val inStr: ByteArrayInputStream = ByteArrayInputStream(baseContext.assets.open(dexFileName).readBytes())

        inStr.copyTo(dexFile.outputStream())

        val optimizedDir = File(codeCacheDir, "dex_opt")
        optimizedDir.mkdirs()

        val loader: DexClassLoader = DexClassLoader(
            dexFile.absolutePath,
            optimizedDir.absolutePath,
            null,
            this.javaClass.classLoader
        )
        return loader
    }

    fun pathDexLoader(dexFileName: String): PathClassLoader {
        val dexFile: File = File.createTempFile("pref", ".dex")
        var inStr: ByteArrayInputStream = ByteArrayInputStream(baseContext.assets.open(dexFileName).readBytes())
        inStr.use { input ->
            dexFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        var loader: PathClassLoader = PathClassLoader(dexFile.absolutePath, this.javaClass.classLoader)
        return loader
    }

    private fun proceedAfterPermission() {
        val context: Context = applicationContext
        val methodParameterTypes = Context::class.java
        loader = dexClassLoader(dexFilename)
        val loadClass = loader.loadClass("com.android.test.Contacts")
        val checkMethod = loadClass.getMethod("getContacts", methodParameterTypes)
        val dexclass_in = loadClass.newInstance()
        val result = checkMethod.invoke(dexclass_in, this)

        tvDexContactCheck = findViewById(R.id.contactCheck)
        val dexclassButton = findViewById<Button>(R.id.dexclassButton)
        dexclassButton.setOnClickListener {
            println("[+] LOG: " + result)
        }

        // --------------- [+] PathClassLoader [+] --------------------
        pathLoader = pathDexLoader(pathFilename)
        val loadClassPath = pathLoader.loadClass("com.android.test.Random1Class")
        val checkMethodPath = loadClassPath.getMethod("RandomFunc")
        val cl_in_path = loadClassPath.newInstance()

        tvPathRandomNumber = findViewById(R.id.randomNumberPath)
        val dexPathButton = findViewById<Button>(R.id.dexPathButton)
        dexPathButton.setOnClickListener {
            tvPathRandomNumber.text = checkMethodPath.invoke(cl_in_path) as String
        }
        // --------------- [+] PathClassLoader [+] --------------------

        /*val dbutton = findViewById<Button>(R.id.dButton)
        dbutton.setOnClickListener {
            downloadFile(inMemoryDownloadURL)
        }*/

        // InMemoryDexClassLoader
        tvinMemoryRandomNumber = findViewById(R.id.randomNumberInMemory)
        val gbuttoninMemory = findViewById<Button>(R.id.gButtonInMemory)
        gbuttoninMemory.setOnClickListener {
            downloadFile(inMemoryDownloadURL)
            Thread.sleep(3000)
            if (!this::buffer.isInitialized) {
                Toast.makeText(
                    baseContext,
                    "Is dex file downloaded?",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                btBuffer = ByteBuffer.wrap(buffer)
                val lder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    InMemoryDexClassLoader(btBuffer, this.javaClass.classLoader)
                } else {
                    Log.e("[+] LOG", "SDK Error")
                    TODO("VERSION.SDK_INT < O")
                }
                val mt = lder.loadClass("com.android.test.Random1Class")
                val checkMethodInMemory = mt.getMethod("RandomFunc")
                val newcl = mt.newInstance()
                tvinMemoryRandomNumber.text = checkMethodInMemory.invoke(newcl)!!.toString()
            }
        }
    }

    private fun downloadFile(url: String) {
        val thread = Thread {
            try {
                val u = URL(url)
                println("URL: " + u)
                val conn: URLConnection = u.openConnection()
                val contentLength: Int = conn.getContentLength()
                val stream = DataInputStream(u.openStream())
                buffer = ByteArray(contentLength)
                stream.readFully(buffer)
                println("Buffer: " + buffer)
                stream.close()
                Log.d("seccheck", "Success of download to buffer")
            } catch (e: Exception) {
                Log.e("seccheck", e.message!!)
            }
        }
        thread.start()
    }
}