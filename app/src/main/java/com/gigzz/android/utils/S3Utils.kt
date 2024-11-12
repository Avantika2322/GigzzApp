package com.gigzz.android.utils

import android.content.Context
import android.webkit.MimeTypeMap
import com.amazonaws.ClientConfiguration
import com.amazonaws.HttpMethod
import com.amazonaws.Protocol
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.ResponseHeaderOverrides
import kotlinx.coroutines.DelicateCoroutinesApi
import java.util.Date

object S3Utils {
    private var amzonS3Client: AmazonS3Client? = null
    private var amzonS3: AmazonS3? = null
    private var credProvider: CognitoCachingCredentialsProvider? = null
    private var transferUtility: TransferUtility? = null
//    private lateinit var prefModule: PreferenceDataStoreModule

    //    private lateinit var preferenceModule: PreferenceModule

    // Initialize the Amazon Cognito credentials provider
    /* fun getCredProvider(context: Context?): CognitoCachingCredentialsProvider? {
         preferenceModule = PreferenceModule(context!!)
         prefModule = PreferenceDataStoreModule(context!!)
         if (credProvider == null) {
             credProvider = CognitoCachingCredentialsProvider(
                 context,
                 preferenceModule.getString(AppConstants.COGNITO_ID),  // Identity Pool ID
                 AppConstants.MY_REGION // Region
             )
         }
         return credProvider
     }*/

    @OptIn(DelicateCoroutinesApi::class)
    private fun getS3Client(context: Context?): AmazonS3? {
        if (amzonS3 == null) {
//            preferenceModule = PreferenceModule(context!!)
//            prefModule = PreferenceDataStoreModule(context!!)
            val credentials:AWSCredentials =BasicAWSCredentials(
                AppConstants.ACCESS_KEY,
                AppConstants.SECRET_KEY)
            /*GlobalScope.launch {
                credentials = BasicAWSCredentials(
                    prefModule.getFirstPreference(PrefKeys.ACCESS_KEY_ID, ""),
                    prefModule.getFirstPreference(PrefKeys.SECRET_ACCESS_KEY, "")
                )
            }*/
            val clientConfig = ClientConfiguration()
            clientConfig.protocol = Protocol.HTTP
            amzonS3 = AmazonS3Client(credentials, Region.getRegion(AppConstants.MY_REGION))
            //amzonS3 = AmazonS3Client(getCredProvider(context), Region.getRegion(AppConstants.MY_REGION))
            //amzonS3!!.setRegion(Region.getRegion(AppConstants.MY_REGION))
        }
        return amzonS3
    }

    fun getTransferUtility(context: Context): TransferUtility? {
        if (transferUtility == null) {
            transferUtility = TransferUtility.builder()
                .s3Client(getS3Client(context.applicationContext))
                .context(context.applicationContext)
                .build()
        }
        return transferUtility
    }


    /**
     * Method to generate a presignedurl for the image
     * @param applicationContext context
     * @param path image path
     * @return presignedurl
     */
    @OptIn(DelicateCoroutinesApi::class)
    @JvmStatic
    fun generateS3ShareUrl(applicationContext: Context?, path: String?): String {
        val s3client: AmazonS3 = getS3Client(applicationContext)!!
        val expiration = Date()
        var msec = expiration.time
        msec += 1000 * 60 * 60.toLong() // 1 hour.
        expiration.time = msec

        val overrideHeader = ResponseHeaderOverrides()
        overrideHeader.contentType = getMimeType(path)
        //val mediaUrl = f.name
        val generatePreSignedUrlRequest = GeneratePresignedUrlRequest(AppConstants.BUCKET_NAME, path)
        /*GlobalScope.launch {
           generatePreSignedUrlRequest = GeneratePresignedUrlRequest(
                prefModule.getFirstPreference(PrefKeys.BUCKET_NAME, ""),
                path
            )
        }*/
        generatePreSignedUrlRequest.method = HttpMethod.GET // Default.
        generatePreSignedUrlRequest.expiration = expiration
        generatePreSignedUrlRequest.responseHeaders = overrideHeader
        val url = s3client.generatePresignedUrl(generatePreSignedUrlRequest)
        //Log.e("Generated Url - ", url.toString())
        return url.toString()
    }

    private fun getMimeType(url: String?): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }
}