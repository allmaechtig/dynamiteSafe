package io.horizontalsystems.dynamitewallet

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

class SingleLiveEvent<T> : MutableLiveData<T>() {

    private val mPending = AtomicBoolean(false)

    override fun setValue(t: T?) {
        mPending.set(true)
        super.setValue(t)
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, Observer<T> { t ->
            if (mPending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        })
    }

    override fun postValue(value: T?) {
        mPending.set(true)
        super.postValue(value)
    }

    fun call() {
        value = null
    }

}