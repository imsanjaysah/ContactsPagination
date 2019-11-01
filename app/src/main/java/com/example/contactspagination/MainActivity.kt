package com.example.contactspagination

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.contactspagination.ui.main.Contact
import com.example.contactspagination.ui.main.ContactsViewModel
import com.example.contactspagination.ui.main.ContactsViewModelFactory
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: ContactsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val viewModelFactory = ContactsViewModelFactory(contentResolver)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ContactsViewModel::class.java)
        contactsList.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.VERTICAL,
            false
        )

        val diffCallback = object : DiffUtil.ItemCallback<Contact>() {
            override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
                return oldItem.id == newItem.id
            }
        }

        val adapter = ContactsAdapter(diffCallback)
        contactsList.adapter = adapter

        if (!hasPhoneContactsPermission(Manifest.permission.READ_CONTACTS)) {
            requestPermission(Manifest.permission.READ_CONTACTS);
        } else {
            //getAllContacts();
            /*if (savedInstanceState == null) {
                viewModel.loadContacts()
            } else {
                viewModel.restoreContacts()
            }*/

            viewModel.loadContacts()
            viewModel.contactsList?.observe(this, Observer {
                adapter.submitList(it)
                contactsEmpty.visibility = if (adapter.itemCount > 0) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            })

            viewModel.isLoading.observe(this, Observer {
                if (it) {
                    showLoading()
                } else {
                    hideLoading()
                }
            })
            //Toast.makeText(activity, "Contact data has been printed in the android monitor log..", Toast.LENGTH_SHORT).show();
        }
    }

    private fun hideLoading() {
        Log.d("Progress", "hide")
        Handler().postDelayed( { progress.visibility = View.GONE}, 700)

    }

    private fun showLoading() {
        Log.d("Progress", "show")
        progress.visibility = View.VISIBLE

    }


    // Check whether user has phone contacts manipulation permission or not.
    private fun hasPhoneContactsPermission(permission: String): Boolean {
        var ret = false

        // If android sdk version is bigger than 23 the need to check run time permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // return phone read contacts permission grant status.
            val hasPermission =
                ContextCompat.checkSelfPermission(applicationContext, permission)
            // If permission is granted then return true.
            if (hasPermission == PackageManager.PERMISSION_GRANTED) {
                ret = true
            }
        } else {
            ret = true
        }
        return ret
    }

    // Request a runtime permission to app user.
    private fun requestPermission(permission: String) {
        val requestPermissionArray = arrayOf(permission)
        ActivityCompat.requestPermissions(this, requestPermissionArray, 1)
    }

    // After user select Allow or Deny button in request runtime permission dialog
    // , this method will be invoked.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val length = grantResults.size
        if (length > 0) {
            val grantResult = grantResults[0]

            if (grantResult == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(
                    applicationContext,
                    "You allowed permission, please click the button again.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    applicationContext,
                    "You denied permission.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    class ContactsAdapter(diffCallback: DiffUtil.ItemCallback<Contact>) :
        PagedListAdapter<Contact, ContactViewHolder>(diffCallback) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
            return ContactViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.list_item_contact, parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
            holder.textView.text = getItem(position)?.name
        }

    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.contactName)
    }


}
