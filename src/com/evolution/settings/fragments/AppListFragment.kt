/*
 * Copyright (C) 2021-2022 AOSP-Krypton Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolution.settings.fragment

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView

import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.android.settings.R
import com.google.android.material.appbar.AppBarLayout

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * [Fragment] that hosts a [RecyclerView] with a vertical
 * list of application info. Items display an icon, name
 * and package name of the application, along with a [CheckBox]
 * indicating whether the item is selected or not.
 */
abstract class AppListFragment: Fragment(R.layout.app_list_layout), MenuItem.OnActionExpandListener {

    private val mutex = Mutex()

    private lateinit var fragmentScope: CoroutineScope
    private lateinit var progressBar: ProgressBar
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var packageManager: PackageManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppListAdapter

    private val packageList = mutableListOf<PackageInfo>()

    private var searchText = ""
    private var displayCategory: Int = CATEGORY_USER_ONLY
    private var packageFilter: ((PackageInfo) -> Boolean) = { true }
    private var packageComparator: ((PackageInfo, PackageInfo) -> Int) = { a, b ->
        getLabel(a).compareTo(getLabel(b))
    }

    private var needsToHideProgressBar = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentScope = CoroutineScope(Dispatchers.Main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        packageManager = requireContext().packageManager
        packageList.addAll(packageManager.getInstalledPackages(0))
    }

    /**
     * Override this function to set the title of this fragment.
     */
    abstract protected fun getTitle(): Int

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().setTitle(getTitle())
        appBarLayout = requireActivity().findViewById(R.id.app_bar)
        progressBar = view.findViewById(R.id.loading_progress)
        adapter = AppListAdapter()
        recyclerView = view.findViewById<RecyclerView>(R.id.apps_list).also {
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = adapter
        }
        needsToHideProgressBar = true
        refreshList()
    }

    /**
     * Abstract function for subclasses to override for providing
     * an inital list of packages that should appear as selected.
     */
    abstract protected fun getInitialCheckedList(): List<String>

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.app_list_menu, menu)
        val searchItem = menu.findItem(R.id.search).also {
            it.setOnActionExpandListener(this)
        }
        val searchView = searchItem.actionView as SearchView
        searchView.setQueryHint(getString(R.string.search_apps));
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String) = false

            override fun onQueryTextChange(newText: String): Boolean {
                fragmentScope.launch {
                    mutex.withLock {
                        searchText = newText
                    }
                    refreshList()
                }
                return true
            }
        })
    }

    override fun onMenuItemActionExpand(item: MenuItem): Boolean {
        // To prevent a large space on tool bar.
        appBarLayout.setExpanded(false /*expanded*/, false /*animate*/)
        // To prevent user expanding the collapsing tool bar view.
        ViewCompat.setNestedScrollingEnabled(recyclerView, false)
        return true
    }

    override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
        // We keep the collapsed status after user cancel the search function.
        appBarLayout.setExpanded(false /*expanded*/, false /*animate*/)
        // Allow user to expande the tool bar view.
        ViewCompat.setNestedScrollingEnabled(recyclerView, true)
        return true
    }

    override fun onDetach() {
        fragmentScope.cancel()
        super.onDetach()
    }

    /**
     * Set the type of apps that should be displayed in the list.
     * Defaults to [CATEGORY_USER_ONLY].
     *
     * @param category one of [CATEGORY_SYSTEM_ONLY],
     * [CATEGORY_USER_ONLY], [CATEGORY_BOTH]
     */
    fun setDisplayCategory(category: Int) {
        fragmentScope.launch {
            mutex.withLock {
                displayCategory = category
            }
        }
    }

    /**
     * Set a custom filter to filter out items from the list.
     *
     * @param customFilter a function that takes a [PackageInfo] and
     * returns a [Boolean] indicating whether to show the item or not. 
     */
    fun setCustomFilter(customFilter: ((packageInfo: PackageInfo) -> Boolean)) {
        fragmentScope.launch {
            mutex.withLock {
                packageFilter = customFilter
            }
        }
    }

    /**
     * Set a [Comparator] for sorting the elements in the list..
     *
     * @param comparator a function that takes two [PackageInfo]'s and returns
     * an [Int] representing their relative priority.
     */
    fun setComparator(comparator: ((a: PackageInfo, b: PackageInfo) -> Int)) {
        fragmentScope.launch {
            mutex.withLock {
                packageComparator = comparator
            }
        }
    }

    /**
     * Called when user selected list is updated.
     *
     * @param list a [List<String>] of selected items.
     */
    open protected fun onListUpdate(list: List<String>) {}

    /**
     * Called when user selected an application.
     *
     * @param packageName the package name of the selected app.
     */
    open protected fun onAppSelected(packageName: String) {}

    /**
     * Called when user deselected an application.
     *
     * @param packageName the package name of the deselected app.
     */
    open protected fun onAppDeselected(packageName: String) {}

    protected fun refreshList() {
        fragmentScope.launch {
            val list = withContext(Dispatchers.Default) {
                mutex.withLock {
                    packageList.filter {
                        when (displayCategory) {
                            CATEGORY_SYSTEM_ONLY -> it.applicationInfo.isSystemApp()
                            CATEGORY_USER_ONLY -> !it.applicationInfo.isSystemApp()
                            else -> true
                        } &&
                        getLabel(it).contains(searchText, true) &&
                        packageFilter(it)
                    }.sortedWith(packageComparator).map { appInfofromPackage(it) }
                }
            }
            adapter.submitList(list)
            if (needsToHideProgressBar) {
                progressBar.visibility = View.GONE
                needsToHideProgressBar = false
            }
        }
    }

    private fun appInfofromPackage(packageInfo: PackageInfo): AppInfo =
        AppInfo(
            packageInfo.packageName,
            getLabel(packageInfo),
            packageInfo.applicationInfo.loadIcon(packageManager),
        )

    private fun getLabel(packageInfo: PackageInfo) =
        packageInfo.applicationInfo.loadLabel(packageManager).toString()

    private inner class AppListAdapter :
            ListAdapter<AppInfo, AppListViewHolder>(itemCallback)
    {
        private val checkedList = getInitialCheckedList().toMutableList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            AppListViewHolder(layoutInflater.inflate(
                R.layout.app_list_item, parent, false))

        override fun onBindViewHolder(holder: AppListViewHolder, position: Int) {
            val item = getItem(position)
            val pkg = item.packageName
            holder.label.setText(item.label)
            holder.packageName.setText(pkg)
            holder.icon.setImageDrawable(item.icon)
            holder.checkBox.setChecked(checkedList.contains(pkg))
            holder.itemView.setOnClickListener {
                if (checkedList.contains(pkg)){
                    checkedList.remove(pkg)
                    onAppDeselected(pkg)
                } else {
                    checkedList.add(pkg)
                    onAppSelected(pkg)
                }
                notifyItemChanged(position)
                onListUpdate(checkedList.toList())
            }
        }
    }

    private class AppListViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val label: TextView = itemView.findViewById(R.id.label)
        val packageName: TextView = itemView.findViewById(R.id.packageName)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
    }

    private data class AppInfo(
        val packageName: String,
        val label: String,
        val icon: Drawable,
    )

    companion object {
        private const val TAG = "AppListFragment"

        const val CATEGORY_SYSTEM_ONLY = 0
        const val CATEGORY_USER_ONLY = 1
        const val CATEGORY_BOTH = 2

        private val itemCallback = object : DiffUtil.ItemCallback<AppInfo>() {
            override fun areItemsTheSame(oldInfo: AppInfo, newInfo: AppInfo) =
                oldInfo.packageName == newInfo.packageName

            override fun areContentsTheSame(oldInfo: AppInfo, newInfo: AppInfo) =
                oldInfo == newInfo
        }
    }
}
