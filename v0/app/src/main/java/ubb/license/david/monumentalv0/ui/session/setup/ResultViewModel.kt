package ubb.license.david.monumentalv0.ui.session.setup

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import ubb.license.david.foursquareapi.FoursquareApi
import ubb.license.david.foursquareapi.model.Venue
import ubb.license.david.monumentalv0.persistence.SessionRepository
import ubb.license.david.monumentalv0.persistence.model.Landmark

class ResultViewModel(private val repository: SessionRepository,
                      private val fourSquareApi: FoursquareApi) : ViewModel() {

    val venuesObservable = MutableLiveData<Array<Venue>>()
    val errorsObservable = MutableLiveData<String>()
    val sessionIdObservable = BehaviorSubject.create<Long>()

    private val mDisposables = CompositeDisposable()

    fun searchVenues(location: String, radius: Int, categories: String) =
        loadVenues(fourSquareApi.searchVenues(location, radius, categories))

    fun searchVenuesLimited(location: String, radius: Int, limit: Int, categories: String) =
        loadVenues(fourSquareApi.searchVenuesLimited(location, radius, limit, categories))

    fun setupSession(userId: String, city: String, venues: Array<Venue>) {
        val landmarks = venues.map { venue -> Landmark.fromVenue(venue) }.toTypedArray()
        val disposable = repository.setupSession(userId, city, landmarks)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ id ->
                sessionIdObservable.onNext(id)
            }, {
                errorsObservable.postValue(it.message)
            })
        mDisposables.add(disposable)
    }

    fun cancelRequests() {
        mDisposables.dispose()
    }

    private fun loadVenues(observable: Single<Array<Venue>>) {
        val disposable = observable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                venuesObservable.postValue(it)
            }, {
                errorsObservable.postValue(it.message)
            })
        mDisposables.add(disposable)
    }
}