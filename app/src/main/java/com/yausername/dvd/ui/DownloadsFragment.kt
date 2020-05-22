package com.yausername.dvd.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.yausername.dvd.adapters.DownloadsAdapter
import com.yausername.dvd.R
import com.yausername.dvd.vm.DownloadsViewModel


class DownloadsFragment : Fragment() {

    private lateinit var downloadsViewModel: DownloadsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_downloads_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                adapter = DownloadsAdapter()
            }
            downloadsViewModel = ViewModelProvider(this).get(DownloadsViewModel::class.java)
            downloadsViewModel.allDownloads.observe(viewLifecycleOwner, Observer { downloads ->
                downloads?.let { (view.adapter as DownloadsAdapter).addItems(downloads)}
            })
        }

        return view
    }

}
