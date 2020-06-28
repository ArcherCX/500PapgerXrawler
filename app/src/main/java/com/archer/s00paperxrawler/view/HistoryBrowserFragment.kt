package com.archer.s00paperxrawler.view

import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.ListFragment
import com.archer.s00paperxrawler.R
import com.archer.s00paperxrawler.db.PaperInfoColumns
import com.archer.s00paperxrawler.db.ResolverHelper
import com.archer.s00paperxrawler.utils.prefs
import com.bumptech.glide.Glide
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File

private const val TAG = "HistoryBrowserFragment"

/**
 * Created by Chen Xin on 2020/6/25.
 */
class HistoryBrowserFragment : ListFragment() {
    private lateinit var cursorAdapter: SimpleCursorAdapter
    private lateinit var historyCursor: Cursor

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cursorAdapter = SimpleCursorAdapter(requireContext(), R.layout.history_item_layout, null,
                arrayOf(PaperInfoColumns.PHOTO_NAME, PaperInfoColumns.PH, PaperInfoColumns.PHOTO_ID),
                intArrayOf(R.id.photo_name_tv, R.id.photographer_tv, R.id.thumbnail_iv), 0).apply {
            viewBinder = MyViewBinder()
        }
        Observable.create<Cursor> {
            /*0:_id, 1:photo name, 2:photographer, 3:photo id*/
            historyCursor = ResolverHelper.INSTANCE.getWebHistory()
            Log.i(TAG, "onActivityCreated() called history cursor = $historyCursor")
            it.onNext(historyCursor)
            it.onComplete()
        }.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).map {
            cursorAdapter.changeCursor(it)
            listAdapter = cursorAdapter
        }.subscribe()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView() called")
        if (::cursorAdapter.isInitialized) {
            cursorAdapter.changeCursor(null)
        }
        if (::historyCursor.isInitialized) {
            historyCursor.close()
        }

    }

    inner class MyViewBinder : SimpleCursorAdapter.ViewBinder {
        override fun setViewValue(view: View?, cursor: Cursor?, columnIndex: Int): Boolean {
            when (view!!.id) {
                R.id.photo_name_tv -> {
                    (view as TextView).text = cursor!!.getString(columnIndex)
                }
                R.id.photographer_tv -> {
                    (view as TextView).text = cursor!!.getString(columnIndex)
                }
                R.id.thumbnail_iv -> {
                    val photo = File("${prefs().photosHistoryPath}/${cursor!!.getLong(columnIndex)}")
                    Glide.with(this@HistoryBrowserFragment).load(photo).placeholder(R.drawable.empty_image_24).thumbnail(0.1f).fitCenter().into(view as ImageView)
                }
                else -> return false
            }
            return true
        }
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)
        val supportFragmentManager = requireActivity().supportFragmentManager
        val transaction = supportFragmentManager.beginTransaction()
        val historyBrowserFragment = HistoryDetailFragment(position)
        transaction.replace(R.id.container, historyBrowserFragment).addToBackStack("history_detail").commit()
    }
}