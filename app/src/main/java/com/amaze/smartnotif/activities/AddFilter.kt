package com.amaze.smartnotif.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.LocaleList
import android.preference.PreferenceManager
import android.text.Editable
import android.view.View
import android.view.textclassifier.TextClassificationManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.amaze.smartnotif.data.*
import com.amaze.smartnotif.data.tfidf.Tfidfworker
import com.amaze.smartnotif.notificationlistenerexample.R
import com.amaze.smartnotif.notificationlistenerexample.databinding.ActivityAddFilterBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.ort.gop.Data.db.ParsedNotification
import kotlinx.android.synthetic.main.activity_add_filter.*
import kotlinx.android.synthetic.main.content_add_filter.*
import kotlinx.coroutines.*
import java.lang.StringBuilder
import kotlin.coroutines.CoroutineContext


class AddFilter() : AppCompatActivity(), CoroutineScope {
    lateinit var notificationGlobal: ParsedNotification

    var title =ArrayList<String>()
    var summary =ArrayList<String>()
    var subtitle =ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding=DataBindingUtil.setContentView<ActivityAddFilterBinding>(this, R.layout.activity_add_filter)
        setSupportActionBar(toolbar)
        val notification=intent.getParcelableExtra<ParsedNotification>("notification")
        notification?.let {
            notificationGlobal=it
        }?:finish()
        val showFilters=intent.getBooleanExtra("showFilters",true)
        if (showFilters) {
            val existing_filter_size = countFilters(getRulesForApp(notification.packageName))
            existing_filters_text.text = getString(R.string.existing_filters_title).format(existing_filter_size)
            existing_filters_layout.setOnClickListener { if (existing_filter_size > 0) startActivity(Intent(this, ViewFilters::class.java).putExtra("packageName", notificationGlobal.packageName)) }
        }else{
            existing_filters_layout.visibility= View.GONE
        }
        binding.content.checkTitle.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isChecked)binding.content.titleTextLayout.isEnabled=false
            else binding.content.titleTextLayout.isEnabled=true
        }
        binding.content.checkSubTitle.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isChecked)binding.content.subtitleTextLayout.isEnabled=false
            else binding.content.subtitleTextLayout.isEnabled=true
        }
        binding.content.checkSummary.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isChecked)binding.content.summaryTextLayout.isEnabled=false
            else binding.content.summaryTextLayout.isEnabled=true
        }
        binding.content.blackList.setOnCheckedChangeListener { v, isChecked ->
            if (isChecked){
                binding.content.summaryTextLayout.isEnabled=false
                binding.content.subtitleTextLayout.isEnabled=false
                binding.content.titleTextLayout.isEnabled=false
            }else{
            binding.content.summaryTextLayout.isEnabled=true
                binding.content.subtitleTextLayout.isEnabled=true
            binding.content.titleTextLayout.isEnabled=true}
        }
        val worker=Tfidfworker.getInstance(this@AddFilter)
        notification?.let {
            try {
                val icon = packageManager.getApplicationIcon(it.packageName)
                icon_holder.setImageDrawable(icon)
                app_name.text=it.appName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            binding.content.checkTitle.isChecked=it.title.isNotBlank()
            binding.content.checkSubTitle.isChecked=it.subtext.isNotBlank()
            binding.content.checkSummary.isChecked=it.summary.isNotBlank()
            binding.content.summaryText.text=it.summary.toEditable()
            binding.content.titleText.text=it.title.toEditable()
            binding.content.subText.text=it.subtext.toEditable()
            var i=0

        }
        println("hey")
        fab.setOnClickListener { view ->
            notification?.let {

                val title = if (binding.content.checkTitle.isChecked && binding.content.titleText.text?.isNotBlank()?:false) binding.content.titleText.text.toString() else "*"
                val subtitle = if (binding.content.checkTitle.isChecked && binding.content.subText.text?.isNotBlank()?:false) binding.content.subText.text.toString() else "*"
                val summary = if (binding.content.checkSummary.isChecked && binding.content.summaryText.text?.isNotBlank()?:false) binding.content.summaryText.text.toString() else "*"
                if (binding.content.blackList.isChecked)blacklist(it.packageName,PreferenceManager.getDefaultSharedPreferences(this))
                else{
//                    var mainObj=getRulesForApp(it.packageName)?:FilterObj("title", STRING_OP_CONTAINS_IGNORECASE,false, arrayMapOf())
//                    var obj=mainObj
//                    var tempObj= obj.values.get(title)?: FilterObj("subtext", STRING_OP_CONTAINS_IGNORECASE,false, arrayMapOf())
//                    obj.values[title]=tempObj
//                    obj=tempObj
//                    tempObj= obj.values.get(subtitle)?: FilterObj("summary", STRING_OP_CONTAINS_IGNORECASE,false, arrayMapOf())
//                    obj.values[subtitle]=tempObj
//                    obj=tempObj
//                    tempObj= obj.values.get(summary)?: FilterObj(true,true)
//                    obj.values[summary]=tempObj
//                 //   addRule(it.packageName,mainObj,PreferenceManager.getDefaultSharedPreferences(this))

                }
                var sample=if (title.length>1)title.trim() else ""
                sample+=" "
                sample+=if (subtitle.length>1)subtitle.trim() else ""
                sample+=" "
                sample+=if (summary.length>1)summary.trim() else ""
                sample+=" "
                sample+=it.appName
                worker.addTrainingSample(sample)
                Toast.makeText(this,"Added",Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    fun countFilters(root:FilterObj?):Int{
        if (root==null)return 0
        if(root.done)return  1
        var i=0
        for(x in root?.values?: arrayMapOf()){
            i+=countFilters(x.value)
        }
        return i
    }

    fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)
    protected var job= Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main
}
