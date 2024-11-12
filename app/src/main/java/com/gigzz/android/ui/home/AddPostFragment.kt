package com.gigzz.android.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.gigzz.android.R
import com.gigzz.android.common.viewBinding
import com.gigzz.android.databinding.FragmentAddPostBinding
import com.gigzz.android.domain.req.CreatePostReq
import com.gigzz.android.domain.req.Media
import com.gigzz.android.domain.req.Medias
import com.gigzz.android.domain.res.PostData
import com.gigzz.android.presentation.HomeViewModel
import com.gigzz.android.utils.AppConstants
import com.gigzz.android.utils.Resource
import com.gigzz.android.utils.S3Utils
import com.gigzz.android.utils.loadCachedImg
import com.gigzz.android.utils.remove
import com.gigzz.android.utils.resize
import com.gigzz.android.utils.show
import com.gigzz.android.utils.showToast
import com.google.android.material.tabs.TabLayoutMediator
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddPostFragment : Fragment(R.layout.fragment_add_post) {
    private val binding by viewBinding(FragmentAddPostBinding::bind)
    private val MAX_IMAGES = 5
    private var selectedImageCount = 0
    private lateinit var mediaAdapter: UploadPostViewPagerAdapter
    private var mediaList = ArrayList<Medias>()
    private lateinit var postType: String
    private var isFromFrag= "create"
    private var selected = 1
    private lateinit var postData: PostData
    lateinit var imageUrl: String
    private var postReqList = ArrayList<Media>()
    private val homeViewModel: HomeViewModel by activityViewModels()
    private var cropCount = MutableLiveData<Int>()


    private val pickImagesLauncher =
    registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        binding.progressBar.remove()
        if ((uris?.size ?: 0) <= MAX_IMAGES - selectedImageCount) {
            // If the total number of selected images does not exceed the limit, continue with cropping
            uris?.forEachIndexed {index, uri ->
                cropCount.observe(this, Observer {
                    if (cropCount.value==index){
                        cropImage(uri)
                        selectedImageCount++
                    }
                })
            }
        } else {
            showToast(requireContext(),"Maximum $MAX_IMAGES images can be selected")
            selectedImageCount = 0
        }
    }

    private val cropImageLauncher =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        binding.progressBar.remove()
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            // for (i in 0 until count) {
            data?.let {
                val uri = UCrop.getOutput(it)
                val bitmap =
                    MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                val resizedImage = resize(bitmap, 1024, 1024)
                val file = getImageFile(resizedImage)
                cropCount.value = cropCount.value!! + 1
                if (mediaList.size < 5) {
                    mediaList.add(
                        mediaList.size,
                        Medias(
                            "I",
                            file,
                            file,
                        )
                    )
                }
                setSelectedImageAdapter()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cropCount.value=0
        initView()
        clickListeners()
        shareThoughtWatcher()
        handleCreatePostResponse()
        //handleEditPostResponse()
    }

    private fun initView() = binding.apply {
        if (isFromFrag == "EditPost") {
            postText.text = getString(R.string.edit)
            actionPost.text = getString(R.string.editText)
            when (postData.privacy) {
                1 -> etPeopleType.setText(getString(R.string.everyone))
                2 -> etPeopleType.setText(getString(R.string.connection_only))
            }
            shareThoughtsText.setText(postData.caption)

            postData.cacheImage = userPic.loadCachedImg(
                postData.userProfileImageUrl,
                postData.cacheImage,
                R.drawable.user_placeholder
            )

            if (!postData.mediaItem.isNullOrEmpty()) {
                mediaRecyclerList.show()
                progressBar.show()
                for (i in 0 until postData.mediaItem?.size!!) {
                    val im = S3Utils.generateS3ShareUrl(
                        requireActivity(),
                        postData.mediaItem!![i]?.mediaUrl
                    )
                    val gfgPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                    StrictMode.setThreadPolicy(gfgPolicy)

                    val bitmap = loadImageBitmapFromUrl(im) { bitmap ->
                        //val bitmap= getBitmapFromURL(im)
                        val file = bitmap?.let { getImageFile(it) }
                        if (postData.mediaItem?.size!! <= 5) {
                            mediaList.add(
                                mediaList.size,
                                Medias(
                                    mediaUrl = file,
                                    mediaThumbnail = file,
                                )
                            )
                        }

                        setSelectedImageAdapter()
                    }
                }
            }
        }

        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(
                requireActivity(),
                android.R.layout.simple_dropdown_item_1line,
                resources.getStringArray(R.array.people_seen)
            )
        etPeopleType.threshold = 2
        etPeopleType.setAdapter(adapter)
        etPeopleType.setOnItemClickListener { adapterView, view, i, l ->
            // selected = adapterView.getItemAtPosition(i).toString()
            selected = i.plus(1)
        }
    }

    private fun shareThoughtWatcher() = binding.apply {
        shareThoughtsText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Check if the user has typed @ or #
                if (!s.isNullOrEmpty()) {
                    for (i in s.indices) {
                        if (s[i] == '@' || s[i] == '#') {
                            shareThoughtsText.text.setSpan(
                                ForegroundColorSpan(
                                    ContextCompat.getColor(
                                        requireActivity(),
                                        R.color.blue_shade_1
                                    )
                                ),
                                i,
                                i + 1,
                                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                    /*val lastChar = s[start + count - 1]
                    if (lastChar == '@' || lastChar == '#') {
                        suggestionRecycler.visible()
                        if (isInternetAvailable()) {
                            circleViewModel.getMyConnections()
                            handleMyConnectionResponse()
                        } else toast(R.string.no_internet)
                    }*/
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun clickListeners() = binding.apply {
        backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        galleryPick.setOnClickListener {
            mediaRecyclerList.show()
            mediaList.clear()
            selectedImageCount=0
            pickImages()
            progressBar.show()
        }

        videoPick.setOnClickListener {
            mediaRecyclerList.show()
            mediaList.clear()
            selectedImageCount=0
            videoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
            progressBar.show()
        }

        actionPost.setOnClickListener {
            val model = CreatePostReq(
                caption = shareThoughtsText.text.toString().trim(),
                postType = "T",
                medias = arrayListOf(),
                privacy = selected,
                taggedUsers = arrayListOf()
            )
            if (shareThoughtsText.text?.isNotEmpty() == true) {
                if (mediaList.isNotEmpty()) {
                    createPathForS3()
                    model.medias = postReqList
                    model.postType = "M"
                }
                if (isFromFrag == "EditPost") {
                    model.postId = postData.postId
                    //viewModel.editPost(model)
                    //handleEditPostResponse()
                } else {
                    homeViewModel.createPost(model)
                    //handleCreatePostResponse()
                }
            } else if (shareThoughtsText.text?.isEmpty() == true) {
                showToast(requireContext(),"Please Share your thoughts!")
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setSelectedImageAdapter() {
        mediaAdapter =
            UploadPostViewPagerAdapter(
                mediaList, "post",
            ) { pos ->
                if (pos != mediaList.size) {
                    mediaList.removeAt(pos)
                    mediaAdapter.notifyDataSetChanged()
                }
            }
        binding.mediaRecyclerList.adapter = mediaAdapter
        postType = "I"
        binding.tabLayout.show()
        TabLayoutMediator(
            binding.tabLayout,
            binding.mediaRecyclerList
        ) { tab, position ->
            //Some implementation...
        }.attach()
        mediaAdapter.notifyDataSetChanged()
    }
/*
    private fun handleCreatePostResponse() {
        viewModel.createPostLiveData.observe(viewLifecycleOwner) { res ->
            toast(res?.message!!)
            isFrom = "PostCreate"
            findNavController().popBackStack(R.id.feedFragment, false)
        }
    }*/

    private fun handleCreatePostResponse() {
        homeViewModel.createPostResponse.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> binding.progressBar.show()

                is Resource.Success -> {
                    binding.progressBar.remove()
                    findNavController().popBackStack()
                }

                is Resource.Error -> binding.progressBar.remove()

                is Resource.InternetError -> {
                    binding.progressBar.remove()
                    showToast(requireContext(), getString(R.string.no_internet))
                }

                else -> binding.progressBar.remove()
            }
        }
    }

    /*private fun handleEditPostResponse() {
        viewModel.editPostLiveData.observe(viewLifecycleOwner) { res ->
            toast(res?.message!!)
            isFrom = "PostCreate"
            findNavController().popBackStack(R.id.feedFragment, false)
        }
    }*/

    private val videoLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            binding.progressBar.remove()
            if (uri != null) {
                val selectedMediaUri: Uri = uri
                val parcelFileDescriptor = requireActivity().contentResolver.openFileDescriptor(
                    selectedMediaUri,
                    "r",
                    null
                )
                val inputStream = FileInputStream(parcelFileDescriptor?.fileDescriptor)
                val file = File(
                    requireActivity().cacheDir,
                    requireActivity().contentResolver.getFileName(selectedMediaUri)
                )
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)

                try {
                    val mp: MediaPlayer = MediaPlayer.create(requireActivity(), selectedMediaUri)
                    val duration = mp.duration
                    mp.release()
                    if (duration / 1000 > 30) {
                        showToast(requireContext(),"You can select only 30 seconds duration video")
                    } else {
                        binding.tabLayout.remove()
                        val videoThumbnailFile =
                            getImageFile(getVideoThumbnail(requireContext(), file.toUri())!!)
                        mediaList.add(
                            mediaList.size, Medias("V", file, videoThumbnailFile)
                        )
                        mediaAdapter = UploadPostViewPagerAdapter(
                            mediaList,
                            "post"
                        ) { pos -> onImageRemove(pos) }
                        binding.mediaRecyclerList.adapter = mediaAdapter
                        postType = "V"
                        mediaAdapter.notifyItemRangeChanged(0, mediaList.size)
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }

    @SuppressLint("NotifyDataSetChanged")
    private fun onImageRemove(pos: Int) {
        if (pos != mediaList.size) {
            mediaList.removeAt(pos)
            mediaAdapter.notifyDataSetChanged()
            // mediaAdapter.notifyItemRangeChanged(0,mediaList.size)
        }
    }

    private fun pickImages() {
        val mimeTypes = arrayOf("image/*")
        pickImagesLauncher.launch(mimeTypes)
    }

    private fun createPathForS3() {
        val formatMonth = SimpleDateFormat("MMMM", Locale.ENGLISH)
        val curMonth: String = formatMonth.format(Date(System.currentTimeMillis()))
        val month = curMonth.substring(0, 3)
        val cal = Calendar.getInstance()
        val year = cal[Calendar.YEAR]
        for (i in 0..<mediaList.size) {
            val post = mediaList[i].mediaUrl!!
            imageUrl = "post_image/${year}/${month}/${System.currentTimeMillis()}/$post"
            val postUrl = uploadToS3(post)
            val thumbnail = mediaList[i].mediaThumbnail!!
            imageUrl = "post_image/${year}/${month}/${System.currentTimeMillis()}/$thumbnail}"
            val thumbnailUrl = uploadToS3(thumbnail)
            postUrl.let {
                postReqList.add(
                    Media(
                        mediaType = postType,
                        mediaUrl = postUrl,
                        mediaThumbnail = thumbnailUrl,
                        mediaThumbnail100X100 = "1024*1024",
                        widthInPixels = "1024",
                        heightInPixels = "1024"
                    )
                )
            }
        }
    }

    private fun uploadToS3(file: File): String {
        val transferUtility = S3Utils.getTransferUtility(requireActivity())
        val observer: TransferObserver = transferUtility!!.upload(
            AppConstants.BUCKET_NAME, imageUrl, file, CannedAccessControlList.Private
        )
        observer.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, newState: TransferState?) {
                if (newState == TransferState.COMPLETED) {
                } else if (newState == TransferState.FAILED) {
                    imageUrl = ""
                }
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {}

            override fun onError(id: Int, ex: java.lang.Exception?) {
                imageUrl = ""
            }
        })
        return imageUrl
    }

    private fun cropImage(uri: Uri) {
        val destinationDir = File(requireContext().externalCacheDir, "cropped_images")
        if (!destinationDir.exists()) {
            destinationDir.mkdirs()
        }
        val destinationUri =
            Uri.fromFile(File(destinationDir, "cropped_image_${System.currentTimeMillis()}.jpg"))
        val uCrop = UCrop.of(uri, destinationUri)
            .withAspectRatio(16f, 9f) // Set aspect ratio (optional)
        cropImageLauncher.launch(uCrop.getIntent(requireContext()))
    }

    private fun getImageFile(inImage: Bitmap): File {
        val tempFile = File.createTempFile("temp" + mediaList.size, ".png")
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes)
        val bitmapData = bytes.toByteArray()
        val fileOutPut = FileOutputStream(tempFile)
        fileOutPut.write(bitmapData)
        fileOutPut.flush()
        fileOutPut.close()
        return tempFile
    }

    private fun getVideoThumbnail(context: Context, videoUri: Uri): Bitmap? {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        return try {
            mediaMetadataRetriever.setDataSource(context, videoUri)

            val timeMicroseconds =
                1000000L // Retrieve thumbnail from the 1st second (adjust as needed)
            val bitmap = mediaMetadataRetriever.getFrameAtTime(
                timeMicroseconds,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            )
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            mediaMetadataRetriever.release()
        }
    }

    private fun ContentResolver.getFileName(selectedImageUri: Uri): String {
        var name = ""
        val returnCursor = this.query(selectedImageUri, null, null, null, null)
        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            name = returnCursor.getString(nameIndex)
            returnCursor.close()
        }
        return name
    }

    private fun loadImageBitmapFromUrl(imageUrl: String, callback: (Bitmap?) -> Unit) {
        val handler = Handler(Looper.getMainLooper())
        Thread {
            var bitmap: Bitmap? = null
            var connection: HttpURLConnection? = null
            var inputStream: InputStream? = null
            try {
                val url = URL(imageUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.connect()
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    inputStream = connection.inputStream
                    bitmap = BitmapFactory.decodeStream(inputStream)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                connection?.disconnect()
                inputStream?.close()
            }
            handler.post { callback(bitmap) }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        homeViewModel.clearCreatePostResponse()
    }
}