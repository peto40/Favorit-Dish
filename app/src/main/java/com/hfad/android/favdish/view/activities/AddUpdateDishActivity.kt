package com.hfad.android.favdish.view.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.hfad.android.favdish.R
import com.hfad.android.favdish.databinding.ActivityAddUpdateDishBinding
import com.hfad.android.favdish.databinding.DialogCustomImageSelectorBinding
import com.hfad.android.favdish.databinding.DialogCustomListBinding
import com.hfad.android.favdish.utils.Constants
import com.hfad.android.favdish.view.adapter.CustomListItemAdapter
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class AddUpdateDishActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var mBinding: ActivityAddUpdateDishBinding

    private lateinit var mCustomListDialog: Dialog

    private var mImagePath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityAddUpdateDishBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setupActionBar()

        mBinding.ivAddDishImage.setOnClickListener(this)
        mBinding.etType.setOnClickListener(this)
        mBinding.etCategory.setOnClickListener(this)
        mBinding.etTimeInMinutes.setOnClickListener(this)

        mBinding.btnAddDish.setOnClickListener(this)
    }

    private fun setupActionBar() {
        setSupportActionBar(mBinding.toolbarAddDishActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mBinding.toolbarAddDishActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.iv_add_dish_image -> {
                    customImageSelectionDialog()
                    return
                }
                R.id.et_type -> {
                    customItemListDialog(resources.getString(R.string.title_select_dish_type),
                        Constants.dishTypes(), Constants.DISH_TYPE)
                    return
                }
                R.id.et_category -> {
                    customItemListDialog(resources.getString(R.string.title_select_dish_category),
                        Constants.dishCategory(), Constants.DISH_CATEGORY)
                    return
                }
                R.id.et_time_in_minutes -> {
                    customItemListDialog(resources.getString(R.string.title_select_dish_cooking_time),
                        Constants.dishCookTimes(), Constants.DISH_COOKING_TIME)
                    return
                }
                R.id.btn_add_dish -> {
                    val title = mBinding.etTitle.text.toString().trim { it <= ' '}
                    val type = mBinding.etType.text.toString().trim { it <= ' '}
                    val category = mBinding.etCategory.text.toString().trim { it <= ' '}
                    val ingredients = mBinding.etIngredients.text.toString().trim { it <= ' '}
                    val cookingTimeInMinutes = mBinding.etTimeInMinutes.text.toString().trim { it <= ' '}
                    val cookingDirection = mBinding.etDirectionToCook.text.toString().trim { it <= ' '}

                    when {
                        TextUtils.isEmpty(mImagePath) -> {
                            Toast.makeText(this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_image),
                                Toast.LENGTH_SHORT).show()
                        }
                        TextUtils.isEmpty(title) -> {
                            Toast.makeText(this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_title),
                                Toast.LENGTH_SHORT).show()
                        }
                        TextUtils.isEmpty(type) -> {
                            Toast.makeText(this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_type),
                                Toast.LENGTH_SHORT).show()
                        }
                        TextUtils.isEmpty(category) -> {
                            Toast.makeText(this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_category),
                                Toast.LENGTH_SHORT).show()
                        }
                        TextUtils.isEmpty(ingredients) -> {
                            Toast.makeText(this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_ingredient),
                                Toast.LENGTH_SHORT).show()
                        }
                        TextUtils.isEmpty(category) -> {
                            Toast.makeText(this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_category),
                                Toast.LENGTH_SHORT).show()
                        }
                        TextUtils.isEmpty(cookingTimeInMinutes) -> {
                            Toast.makeText(this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_cooking_times),
                                Toast.LENGTH_SHORT).show()
                        }
                        TextUtils.isEmpty(cookingDirection) -> {
                            Toast.makeText(this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_cooking_direction),
                                Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(this@AddUpdateDishActivity,
                                "All the entries are valid.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }


    private fun customImageSelectionDialog() {
        val dialog = Dialog(this)
        val binding: DialogCustomImageSelectorBinding =
                DialogCustomImageSelectorBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        binding.tvCamera.setOnClickListener {
            Dexter.withContext(this).withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                //    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if (report.areAllPermissionsGranted()) {
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            startActivityForResult(intent, CAMERA)
                        }
                    }

                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            }).onSameThread()
                .check()

            dialog.dismiss()
        }

        binding.tvGallery.setOnClickListener {
            Dexter.withContext(this).withPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    val galleryIntent = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    startActivityForResult(galleryIntent, GALLERY)
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(
                        this@AddUpdateDishActivity,
                        "You have denied the storage permission to select image.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }

            }).onSameThread()
                .check()

            dialog.dismiss()

        }
        dialog.show()
    }

    fun selectedItem(item: String, selection: String){
        when(selection) {
            Constants.DISH_TYPE -> {
                mCustomListDialog.dismiss()
                mBinding.etType.setText(item)
            }
            Constants.DISH_CATEGORY -> {
                mCustomListDialog.dismiss()
                mBinding.etCategory.setText(item)
            }
            else -> {
                mCustomListDialog.dismiss()
                mBinding.etTimeInMinutes.setText(item)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA) {
                data?.extras?.let {
                    val thumbnail: Bitmap = data.extras!!.get("data") as Bitmap
                    //mBinding.ivDishImage.setImageBitmap(thumbnail)

                    Glide.with(this)
                        .load(thumbnail)
                        .centerCrop()
                        .into(mBinding.ivDishImage)

                    mImagePath = saveImageToInternalStorage(thumbnail)
                    Log.i("imagePath", mImagePath)

                    mBinding.ivAddDishImage.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.ic_vector_edit)
                    )
                }
            }

            if (requestCode == GALLERY) {
                data?.let {
                    val selectedPhotoUri = data.data
                    //mBinding.ivDishImage.setImageURI(selectedPhotoUri)

                    Glide.with(this)
                        .load(selectedPhotoUri)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                resource?.let {
                                    val bitmap: Bitmap = resource.toBitmap()
                                    mImagePath = saveImageToInternalStorage(bitmap)
                                }
                                return false
                            }

                        })
                        .into(mBinding.ivDishImage)

                    mBinding.ivAddDishImage.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.ic_vector_edit)
                    )
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("canceled", "User canceled image selection")
        }
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this).setMessage(
            "It look like you have turned off permissions" +
                    "required for this feature. It can be enable under Application Settings"
        )
            .setPositiveButton("GO TO SETTINGS")
            { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val wrapper = ContextWrapper(applicationContext)

        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file.absolutePath
    }


    private fun customItemListDialog(title: String, itemList: List<String>, selection: String) {
        mCustomListDialog = Dialog(this)
        val binding: DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)
        mCustomListDialog.setContentView(binding.root)

        binding.tvTitle.text = title

        binding.rvList.layoutManager = LinearLayoutManager(this)

        val adapter = CustomListItemAdapter(this, itemList, selection)
        binding.rvList.adapter = adapter
        mCustomListDialog.show()

    }

    companion object {
        private const val CAMERA = 1
        private const val GALLERY = 2

        private const val IMAGE_DIRECTORY = "FavDishImages"
    }
}