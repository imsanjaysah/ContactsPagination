package com.example.contactspagination.ui.main

import android.content.ContentResolver
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ContactsViewModelFactory(private val contentResolver: ContentResolver) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ContactsViewModel(contentResolver) as T
    }
}