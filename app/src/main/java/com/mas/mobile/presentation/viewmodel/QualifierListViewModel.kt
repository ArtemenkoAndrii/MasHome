package com.mas.mobile.presentation.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.mas.mobile.domain.message.CatchQualifier
import com.mas.mobile.domain.message.Qualifier
import com.mas.mobile.domain.message.QualifierRepository
import com.mas.mobile.domain.message.SkipQualifier
import com.mas.mobile.service.CoroutineService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class QualifierListViewModel @AssistedInject constructor(
    private val qualifierRepository: QualifierRepository,
    private val coroutineService: CoroutineService,
    @Assisted private val showSkipInsteadCatchQualifiers: Boolean
): ListViewModel<Qualifier>(coroutineService, qualifierRepository){
    private val newValues = MutableLiveData<List<Qualifier>>()

    val qualifiers = MediatorLiveData<List<Qualifier>>()

    init {
        val masterSource = if (showSkipInsteadCatchQualifiers) {
            qualifierRepository.live.getSkipQualifiers()
        } else {
            qualifierRepository.live.getCatchQualifiers()
        }

        qualifiers.addSource(masterSource) {
            qualifiers.value = it
        }

        qualifiers.addSource(newValues) {
            qualifiers.value = it.plus(masterSource.value!!.toList())
        }
    }

    fun addNew() {
        newValues.value = listOf(
            if (showSkipInsteadCatchQualifiers) {
                SkipQualifier(NEW_QUALIFIER)
            } else {
                CatchQualifier(NEW_QUALIFIER)
            }
        )
    }

    fun save(item: Qualifier) = process(item) {
        qualifierRepository.save(item)
    }

    override fun remove(item: Qualifier) = process(item) {
        qualifierRepository.remove(item)
    }

    private fun process(item: Qualifier, handle: suspend (Qualifier) -> Unit) {
        if (item.value.isNotBlank()) {
            coroutineService.backgroundTask {
                handle(item)
            }
        }
        newValues.value = emptyList()
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted showSkipInsteadCatchQualifiers: Boolean): QualifierListViewModel
    }

    companion object {
        const val NEW_QUALIFIER = ""
    }
}