package com.amaze.smartnotif.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArrayMap
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.amaze.smartnotif.adapter.FilterAdapter
import com.amaze.smartnotif.data.FilterObj
import com.amaze.smartnotif.data.FilterUI
import com.amaze.smartnotif.data.NotificationHolder
import com.amaze.smartnotif.data.getRulesForApp
import com.amaze.smartnotif.notificationlistenerexample.R
import com.amaze.smartnotif.notificationlistenerexample.databinding.ActivityViewFiltersBinding
import com.ort.gop.Data.db.ParsedNotification
import kotlinx.android.synthetic.main.activity_view_filters.*
import kotlinx.android.synthetic.main.content_view_filter.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ViewFilters : AppCompatActivity(), FilterAdapter.Clickcallback {
    override fun onClick(user: FilterUI) {

    }
    lateinit var binding: ActivityViewFiltersBinding
    lateinit var packageNameGlobal: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView<ActivityViewFiltersBinding>(this,R.layout.activity_view_filters)
        setSupportActionBar(toolbar)
        val showFab=intent.getBooleanExtra("showFab",false)
        if (!showFab)binding.fab.visibility= View.GONE
        val packageName=intent.getStringExtra("packageName")
        packageName?.let {
            packageNameGlobal=it
        }?:finish()
        var appname=""
        try {
            val icon = packageManager.getApplicationIcon(packageNameGlobal)
            icon_holder.setImageDrawable(icon)
            appname=packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageNameGlobal, PackageManager.GET_META_DATA)) as String
            app_name.text=appname
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        fab.setOnClickListener {
            startActivity(Intent(this,AddFilter::class.java).putExtra("notification",ParsedNotification("",appname,"","","",packageNameGlobal,"","",0,1,"",1)).putExtra("showFilters",false))
        }
    }

    override fun onResume() {
        super.onResume()
        GlobalScope.launch {
            val filters=getRulesForApp(packageNameGlobal)
            val uifilters= arrayListOf<FilterUI>()
            getAllRulesUI(filters,uifilters, FilterUI("","",""))
            runOnUiThread {
                binding.content.list.layoutManager=LinearLayoutManager(this@ViewFilters)
                binding.content.list.adapter=FilterAdapter(this@ViewFilters,this@ViewFilters,uifilters)
            }
        }

    }
    fun getAllRulesUI(filterObj: FilterObj?,arrayList: ArrayList<FilterUI>,temp:FilterUI){
        if (filterObj==null)return
        if (filterObj.done){
            arrayList.add(temp.copy())
        }
        when(filterObj.key){
            "title"->{
                for (x in filterObj.values){
                    temp.title=x.key
                    getAllRulesUI(x.value,arrayList,temp)
                }
            }
            "summary"->{
                for (x in filterObj.values){
                    temp.summmary=x.key
                    getAllRulesUI(x.value,arrayList,temp)
                }
            }
            "subtext"->{
                for (x in filterObj.values){
                    temp.subTitle=x.key
                    getAllRulesUI(x.value,arrayList,temp)
                }
            }
        }
    }

}
