package com.gigzz.android.common

import android.content.Context
import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.showToast
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineExceptionHandler
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/*private var errorDialog: ErrorDialog? = null

fun showErrorDialog(fm: FragmentManager, message: String = "", type: String = ""){
    errorDialog?.let {
        if(it.dialog?.isShowing == true || it.isAdded) return
    }
    errorDialog = ErrorDialog.newInstance(message, type)
    errorDialog?.show(fm, ErrorDialog.TAG)

}*/

inline fun <reified T> exceptionHandler(livedata: MutableLiveData<Resource<T>>): CoroutineExceptionHandler {
    return CoroutineExceptionHandler { coroutineContext, exception ->
        Log.e("exception", exception.message.toString())

        val exceptionError = exception.handleThrowable()
        val message = exceptionError.message

        if(exception is ConnectException || exception is SocketTimeoutException){
            livedata.postValue(Resource.InternetError())
        }else {
            livedata.postValue(Resource.Error(message))
        }
    }
}

fun Throwable.handleThrowable(): GeneralResponse {
    return when {
        this is UnknownHostException -> GeneralResponse(message = "Oops! There is a problem connecting to the server. Please try again.")
        this is HttpException -> extractExceptionMessage(exception = this)!!
        this is ConnectException -> GeneralResponse()
        this is SocketTimeoutException -> GeneralResponse(message = "Oops, your connection seems off… Keep calm, check your signal, and try again.")
        this.message != null -> GeneralResponse(message = this.message!!)
        else -> GeneralResponse(message = "Unknown error")
    }
}

private fun extractExceptionMessage(exception: HttpException): GeneralResponse? {
    exception.response()?.run {
        when (exception.code()) {
            500 -> GeneralResponse(message = "Something went wrong.")
            else -> {
                errorBody()?.let {
                    val errorJson = it.string()
                    return if (errorJson.contains("{")) {
                        Gson().fromJson(errorJson, GeneralResponse::class.java)
                    }else {
                        GeneralResponse(message = errorJson)
                    }
                }
            }
        }
    }
    return null
}

fun <T> MutableLiveData<Resource<T>>.emitErrorMessage(response: Response<*>) {
    val error =
        Gson().fromJson(response.errorBody()?.string(), GeneralResponse::class.java)
    postValue(Resource.Error(error.message, response.code()))
}

fun <T> handleApiError(context: Context, res: Resource.Error<T>, fm: FragmentManager) {
    /*when (res.code) {
        405 -> showErrorDialog(fm, res.message.toString())
        401 -> showErrorDialog(fm, "Something went wrong, Please login again.")
        else -> showToast(context, res.message.toString())
    }*/
    showToast(context, res.message.toString())
}