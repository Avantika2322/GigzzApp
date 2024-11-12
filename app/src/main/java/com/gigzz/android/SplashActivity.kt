package com.gigzz.android

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gigzz.android.data.preference.PreferenceDataStoreModule
import com.gigzz.android.databinding.ActivitySplashBinding
import com.gigzz.android.presentation.SplashViewModel
import com.gigzz.android.ui.home.HomeActivity
import com.gigzz.android.ui.onboarding.MainActivity
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.showToast
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private lateinit var preferenceModule: PreferenceDataStoreModule
    private var isLogin = false
    private val viewModel: SplashViewModel by viewModels()
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivitySplashBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(viewBinding.root)
        FirebaseApp.initializeApp(this)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        preferenceModule = PreferenceDataStoreModule(applicationContext)
        viewModel.getAppSetting()

        viewModel.isLoggedIn.observe(this) { isLoggedIn ->
            if (isLoggedIn) startActivity(Intent(this, HomeActivity::class.java))
            else startActivity(Intent(this, MainActivity::class.java))
            viewModel.getToken()
            finish()
        }

        settingObserver()
    }

    private fun getVersionName(): String {
        try {
            val packageManager = applicationContext.packageManager
            val packageName = applicationContext.packageName
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            return packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun settingObserver() {
        viewModel.getSettingResponse.observe(this) {
            when (it) {
                is Resource.Loading -> {}

                is Resource.Success -> {}

                is Resource.Error -> {}

                is Resource.InternetError -> {
                    showToast(this, getString(R.string.no_internet))
                }
                else -> {}
            }
        }
    }

}