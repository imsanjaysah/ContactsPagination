package com.example.contactspagination.ui.main

import android.content.ContentResolver
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.paging.PositionalDataSource

class ContactsViewModel(private val contentResolver: ContentResolver) : ViewModel() {

    var contactsList: LiveData<PagedList<Contact>>? = null
    var isLoading = MutableLiveData<Boolean>()

    companion object {
        private val PROJECTION = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
        )
    }
    fun loadContacts() {
        if (contactsList == null) {
            val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setEnablePlaceholders(false)
                .build()
            contactsList = LivePagedListBuilder<Int, Contact>(
                ContactsDataSourceFactory(contentResolver), config
            ).build()
        }
    }


    inner class ContactsDataSourceFactory(private val contentResolver: ContentResolver) :
        DataSource.Factory<Int, Contact>() {

        override fun create(): DataSource<Int, Contact> {
            return ContactsDataSource(contentResolver)
        }
    }

    inner class ContactsDataSource(private val contentResolver: ContentResolver) :
        PositionalDataSource<Contact>() {



        override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Contact>) {
            callback.onResult(getContacts(10, params.requestedStartPosition), 0)
        }

        override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Contact>) {
            callback.onResult(getContacts(params.loadSize, params.startPosition))
        }

        private fun getContacts(limit: Int, offset: Int): MutableList<Contact> {
            isLoading.postValue(true)
            Log.d("Contacts", "$limit - $offset")
            val cursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                PROJECTION,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY +
                        " ASC LIMIT " + limit + " OFFSET " + offset
            )

            cursor!!.moveToFirst()
            val contacts: MutableList<Contact> = mutableListOf()
            while (!cursor.isAfterLast) {
                val id = cursor.getLong(cursor.getColumnIndex(PROJECTION[0]))
                val lookupKey = cursor.getString(cursor.getColumnIndex(PROJECTION[0]))
                val name = cursor.getString(cursor.getColumnIndex(PROJECTION[2]))
                contacts.add(Contact(id, lookupKey, name))
                cursor.moveToNext()
            }
            cursor.close()

            isLoading.postValue(false)

            return contacts
        }
    }
}


