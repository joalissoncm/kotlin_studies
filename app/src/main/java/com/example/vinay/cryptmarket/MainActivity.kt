package com.example.vinay.cryptmarket

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import com.example.vinay.cryptmarket.databinding.ActivityMainBinding
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback



class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: CryptoAdapter
    val cryptoList = mutableListOf<CryptoResult>()
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        binding.cryptoList.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        binding.cryptoList.addItemDecoration(LinearLayoutSpaceItemDecoration(16))
        adapter = CryptoAdapter(cryptoList)
        binding.cryptoList.adapter = adapter
        prepareSwipeRefreshLayout()

        binding.progress.indeterminateDrawable.setColorFilter(ContextCompat.getColor(this, R.color.color_values), PorterDuff.Mode.MULTIPLY)

        binding.progress.visibility = View.VISIBLE
        callApi()

        doTheAutoRefresh()
    }

    override fun onRefresh() {
        callApi()
    }

    private fun prepareSwipeRefreshLayout() {
        binding.swipeLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)
        binding.swipeLayout.setOnRefreshListener(this)
    }

    fun callApi() {
        val apiService = ApiService.create()
        val call = apiService.getCrypto()
        Log.d("REQUEST", call.toString() + "")
        call.enqueue(object : Callback<MutableList<CryptoResult>> {
            override fun onResponse(call: Call<MutableList<CryptoResult>>, response: retrofit2.Response<MutableList<CryptoResult>>?) {
                if (response != null) {
                    binding.progress.visibility = View.GONE

                    Log.d("RESPONCE", response.toString() + "")
                    runOnUiThread {
                        cryptoList.clear()
                        cryptoList.addAll(response.body())
                        adapter.notifyItemInserted(cryptoList.size)
                    }

                    binding.swipeLayout.isRefreshing = false
                }
            }

            override fun onFailure(call: Call<MutableList<CryptoResult>>, t: Throwable) {
                binding.progress.visibility = View.GONE
                Log.e("error", t.toString())
                binding.swipeLayout.isRefreshing = false
            }
        })
    }

    private fun doTheAutoRefresh() {
        handler.postDelayed({
            // auto refresh
            binding.progress.visibility = View.VISIBLE
            callApi()
            doTheAutoRefresh()
        }, 60000)
    }
}
