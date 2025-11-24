package com.example.wiseman.hxjblesdk.ui.locklist

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.example.wiseman.hxjblesdk.R
import com.example.wiseman.hxjblesdk.adapters.LockListAdapter
import com.example.wiseman.hxjblesdk.db.beans.LockListBean
import com.example.wiseman.hxjblesdk.db.lock.Lock
import com.example.wiseman.hxjblesdk.ui.addLock.AddDeviceActivity
import com.example.wiseman.hxjblesdk.ui.lockfun.LockFunActivity
import com.example.wiseman.hxjblesdk.viewmodel.LockViewModel
import com.example.hxjblinklibrary.blinkble.scanner.HxjBluetoothDevice
import com.example.hxjblinklibrary.blinkble.scanner.HxjScanCallback
import com.example.hxjblinklibrary.blinkble.scanner.HxjScanner
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.xiasuhuei321.loadingdialog.view.LoadingDialog
import no.nordicsemi.android.support.v18.scanner.ScanResult

class LockListActivity : AppCompatActivity() {
    private lateinit var mLockViewModel: LockViewModel
    private lateinit var adapter: LockListAdapter
    private val NEW_LOCK_ACTIVITY_REQUEST_CODE = 1
    private var loadingDialog: LoadingDialog? = null
    private var mToast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_list)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        mLockViewModel = ViewModelProvider(this).get(LockViewModel::class.java)
        initView()
        initListener()

        mLockViewModel.getAllLocks().observe(this, Observer { locks ->
            val llbList: List<LockListBean> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locks.map { LockListBean(it) }
            } else {
                val list = ArrayList<LockListBean>()
                for (lock in locks) {
                    list.add(LockListBean(lock))
                }
                list
            }
            adapter.setList(llbList)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_device_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                // Handle settings action
            }
            R.id.action_dfu -> {
                // Handle DFU action
            }
        }
        return false
    }

    override fun onStart() {
        super.onStart()
    }

    private fun initListener() {
        adapter.setOnItemClickListener { adapter, view, position ->
            if (!requetPermission()) return@setOnItemClickListener
            val intent = Intent(this, LockFunActivity::class.java)
            val lockObj = (adapter.data[position] as LockListBean).lock
            intent.putExtra(LockFunActivity.LOCK_INFO, lockObj)
            startActivity(intent)
        }
    }

    private fun requetPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                ), 1)
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            for (i in permissions.indices) {
                if (grantResults[i] == PERMISSION_GRANTED) {
                    // Permission granted
                } else if (grantResults[i] == PERMISSION_DENIED) {
                    mToast?.cancel()
                    mToast = Toast.makeText(this, permissions[i] + getString(R.string.permission_tips), Toast.LENGTH_SHORT)
                    mToast?.show()
                }
            }
        }
    }

    private fun initView() {
        val recyclerView: RecyclerView = findViewById(R.id.recyclerview)
        adapter = LockListAdapter(ArrayList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            if (!requetPermission()) return@setOnClickListener
            val intent = Intent(this, AddDeviceActivity::class.java)
            startActivityForResult(intent, NEW_LOCK_ACTIVITY_REQUEST_CODE)
        }
    }

    private fun startScan(lock: Lock) {
        loadingDialog = LoadingDialog(this)
        loadingDialog?.setLoadingText("正在搜索设备..." + lock.lockMac)?.show()
        HxjScanner.getInstance().startScan(5000, applicationContext, object : HxjScanCallback() {
            override fun onHxjScanResults(results: List<HxjBluetoothDevice>) {
                super.onHxjScanResults(results)
                if (results.isNotEmpty()) {
                    for (result in results) {
                        if (lock.lockMac == result.mac) {
                            stopScan()
                            loadingDialog?.loadSuccess()
                            val intent = Intent(this@LockListActivity, LockFunActivity::class.java)
                            intent.putExtra(LockFunActivity.DEVICE, result)
                            startActivity(intent)
                        }
                    }
                }
            }
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                val hxjBluetoothDevice = HxjBluetoothDevice(result)
            }
            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                loadingDialog?.loadFailed()
            }
        })
    }

    private fun stopScan() {
        HxjScanner.getInstance().stopScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == NEW_LOCK_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: RESULT_OK$resultCode")
        } else {
            Log.d(TAG, "onActivityResult: $resultCode")
        }
    }

    companion object {
        private const val TAG = "LockListActivity"
        private const val PERMISSION_GRANTED = android.content.pm.PackageManager.PERMISSION_GRANTED
        private const val PERMISSION_DENIED = android.content.pm.PackageManager.PERMISSION_DENIED
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
