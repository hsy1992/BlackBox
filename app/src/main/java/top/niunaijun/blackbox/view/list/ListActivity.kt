package top.niunaijun.blackbox.view.list

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.roger.catloadinglibrary.CatLoadingView
import top.niunaijun.blackbox.R
import top.niunaijun.blackbox.databinding.ActivityListBinding
import top.niunaijun.blackbox.util.InjectionUtil
import top.niunaijun.blackbox.util.LoadingUtil
import top.niunaijun.blackbox.util.inflate


class ListActivity : AppCompatActivity() {

    private val viewBinding: ActivityListBinding by inflate()

    private lateinit var mAdapter: ListAdapter

    private lateinit var viewModel: ListViewModel

    private lateinit var loadingView: CatLoadingView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        setSupportActionBar(viewBinding.toolbarLayout.toolbar)
        mAdapter = ListAdapter()
        viewBinding.recyclerView.adapter = mAdapter
        viewBinding.recyclerView.layoutManager = LinearLayoutManager(this)

        mAdapter.setOnItemClick { _, _, data ->
            finishWithPath(data.sourceDir)
        }

        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this, InjectionUtil.getListFactory()).get(ListViewModel::class.java)
        viewBinding.stateView.showLoading()
        viewModel.getInstalledApps()
        viewModel.appsLiveData.observe(this) {
            if (it != null) {
                mAdapter.replaceData(it)
                if (it.isNotEmpty()) {
                    viewBinding.stateView.showContent()
                } else {
                    viewBinding.stateView.showEmpty()
                }
            }
        }

        viewModel.copyFileLiveData.observe(this) {
            hideLoading()
            if (it != null) {
                finishWithPath(it)
            }
        }
    }

    private val openDocumentedResult = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it?.run {
            copyFile(this)
        }
    }


    private fun finishWithPath(apkPath: String) {
        intent.putExtra("apkPath", apkPath)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun copyFile(uri: Uri) {
        showLoading()
        viewModel.copyFile(uri)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.list_choose) {
            openDocumentedResult.launch("application/vnd.android.package-archive")
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_list, menu)
        return true
    }

    private fun showLoading() {
        if (!this::loadingView.isInitialized) {
            loadingView = CatLoadingView()
        }
        LoadingUtil.showLoading(loadingView,supportFragmentManager)
    }


    private fun hideLoading() {
        if (loadingView.isAdded && loadingView.isResumed) {
            loadingView.dismiss()
        }
    }
}