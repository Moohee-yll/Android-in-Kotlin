package com.example.filepersistencetest

import android.Manifest.permission.MANAGE_DOCUMENTS
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.*
import java.util.jar.Manifest


class MainActivity : AppCompatActivity() {
    var globalUri:Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val buttonPickImage = findViewById<Button>(R.id.buttonPickImage)
        val buttonSelectFile  = findViewById<Button>(R.id.buttonSelectFile)
        val buttonCreatFile = findViewById<Button>(R.id.buttonCreatFile)
        val buttonPickDirec = findViewById<Button>(R.id.buttonPickDirec)
        val buttonDeleteFile = findViewById<Button>(R.id.deleFileButton)
        val buttonAlterFile = findViewById<Button>(R.id.alterFileButton)

        buttonPickImage.setOnClickListener {
            //使用Intent调用相册
            val intent = Intent(Intent.ACTION_PICK, null)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")//设置要获取的文件类型，
            startActivityForResult(intent,100)

        }
        buttonSelectFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                setType("*/*")
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse(Environment.getExternalStorageDirectory().toString()))
            }
            startActivityForResult(intent,101)
        }

        buttonCreatFile.setOnClickListener {
            val intent = FileOperateUtils.createFile(Uri.parse(Environment.getExternalStorageDirectory().toString()))
            startActivityForResult(intent,102)
        }

        buttonPickDirec.setOnClickListener {
            val intent = FileOperateUtils.openDirectory(null)//Uri.parse(Environment.getExternalStorageDirectory().toString())
            startActivityForResult(intent, 103)
        }

        buttonDeleteFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                setType("*/*")
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse(Environment.getExternalStorageDirectory().toString()))
            }
            startActivityForResult(intent,104)
        }

        buttonAlterFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                setType("*/*")
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse(Environment.getExternalStorageDirectory().toString()))
            }
            startActivityForResult(intent,105)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            100 -> if(resultCode == RESULT_OK){
                try {
                    val imageUri :Uri? = data?.data
                    Log.e("imageUri",imageUri.toString())
                    //获取绝对路径
                    val selectPhotoPath:String? = getRealPathFromUri(this, imageUri)
                    if (selectPhotoPath != null) {
                        Log.e("selectPhotoPath",selectPhotoPath)
                        //显示路径
                        val editText = findViewById<EditText>(R.id.editText1)
                        editText.setText(selectPhotoPath)
                        editText.setSelection(selectPhotoPath.length) //将光标移至末尾
                    }
                    //获取图片
                    val resolver: ContentResolver = getContentResolver()
                    val inStream :InputStream? = imageUri?.let { resolver.openInputStream(it) }
                    //以下两句将原图缩小，防止文件过大内存溢出
                    val options = BitmapFactory.Options()
                    options.inSampleSize = 4 //表示图片缩小至原来的几分之一
                    val bitmap : Bitmap? = BitmapFactory.decodeStream(inStream,null,options)
                    val imageView = findViewById<ImageView>(R.id.imageView)
                    imageView.setImageBitmap(bitmap)
                }catch (e:IOException){
                    Log.e("Media","Error!")
                }
            }else{
                Log.e("Media", "LoadMediaFail.")
            }


            101 -> if(resultCode == RESULT_OK){
                try{
                    val fileUri :Uri? = data?.data
                    Log.e("fileUri:",fileUri.toString())
                    //获取绝对路径
//                    val root = Environment.getExternalStorageDirectory()
//                    Log.e("MainActivity",root.toString())
//                    val selectFilePath = fileUri?.let { GetFilePath.getFilePathByUri(this,it) }
                    val readByte = fileUri?.let{ FileOperateUtils.readTextFromUri(it) }
                    var selectFilePath = ""
                    if (readByte != null) {
                        for( i in readByte){
                            selectFilePath = selectFilePath + Integer.toHexString(i.toInt())+"  "
                        }

                        Log.e("Data in Selectedfile:",selectFilePath)
                        //显示路径
                        val editText = findViewById<EditText>(R.id.fileEdiText)
                        editText.setText(selectFilePath.toString())
                        editText.setSelection(selectFilePath.length) //将光标移至末尾
                    }else{
                        val editText = findViewById<EditText>(R.id.fileEdiText)
                        editText.setText("")
                    }
                }catch (e:Exception){
                    Log.e("File","Error!  $e")
                    val editText = findViewById<EditText>(R.id.fileEdiText)
                    editText.setText("")
                }
            }else{
                Log.e("File", "LoadFileFail.")
                val editText = findViewById<EditText>(R.id.fileEdiText)
                editText.setText("")
            }

            102 -> if(resultCode == RESULT_OK){
                try{
                    data?.data?.also { uri ->
                        Log.e("CreatFile",uri.toString())
                        Toast.makeText(this, "文件创建成功", Toast.LENGTH_SHORT).show()
                    }
                }catch (e:Exception){
                    Log.e("CreatFile","Error!  $e")
                }
            }else{
                Log.e("CreatFile", "Fail.")
            }

            103 -> if(resultCode == RESULT_OK){
                try{
                    data?.data?.also { uri ->
                        Log.e("PickedDirec",uri.toString())

                        // 获取权限
                        val takeFlags: Int = (data.flags and
                                (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
                        contentResolver.takePersistableUriPermission(uri, takeFlags)
                        // 保存获取的目录权限
                        val sp = getSharedPreferences("DirPermission", MODE_PRIVATE)
                        val editor = sp.edit()
                        editor.putString("uriTree", uri.toString())
                        editor.apply()

                    }
                }catch (e:Exception){
                    Log.e("PickDirec","Error!  $e")
                }
            }else{
                Log.e("PickDirec", "Fail.")
            }


            104 -> if(resultCode == RESULT_OK){
                try{
                    data?.data?.also { uri ->
                        Log.e("DeleteFile",uri.toString())
                        FileOperateUtils.deleteDocument(uri)
                        Toast.makeText(this, "文件删除成功！", Toast.LENGTH_SHORT).show()
                    }
                }catch (e:Exception){
                    Log.e("DeleteFile","Error!  $e")
                    Toast.makeText(this, "文件删除失败！", Toast.LENGTH_SHORT).show()
                }
            }else{
                Log.e("DeleteFile", "Choose NO Files.")
                Toast.makeText(this, "请选择需要删除的文件！", Toast.LENGTH_SHORT).show()
            }

            105 -> if(resultCode == RESULT_OK){
                try{
                    data?.data?.also { uri ->
                        Log.e("AlterFile",uri.toString())
                        FileOperateUtils.alterDocument(uri)
                        Toast.makeText(this, "文件修改成功", Toast.LENGTH_SHORT).show()

                    }
                }catch (e:Exception){
                    Log.e("AlterFile","Error!  $e")
                    Toast.makeText(this, "文件修改失败！", Toast.LENGTH_SHORT).show()
                }
            }else{
                Log.e("AlterFile", "Choose NO Files.")
                Toast.makeText(this, "请选择需要修改的文件！", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //接收申请权限的返回结果
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            1051 -> {
                for (i in grantResults){
                    Log.e("grantResults", i.toString())
                }
                if (!grantResults.isNotEmpty()){
                    Log.e("grantResults", "is empty.")
                }
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    globalUri?.let { FileOperateUtils.alterDocument(it) }
                }else{
                    for (permission in permissions){
                        val shoudShoow = ActivityCompat.shouldShowRequestPermissionRationale(this,permission)
                        Log.e("Denail always",shoudShoow.toString())
                    }

                    Toast.makeText(this, "获取权限才能修改文件", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun getFilePathFromUri(context: Context, fileUri: Uri?): String? {
        if (fileUri != null){
            return getDataColumn(context,fileUri,null, null)
        }else{
            return null
        }
    }

    private fun getRealPathFromUri(context: Context, uri: Uri?): String? {
        val sdkVersion = Build.VERSION.SDK_INT
        if (sdkVersion >= 19){
            return getRealPathFromUriAboveAPI19(context,uri)
        }else{
            return getRealPathFromUriBelowAPI19(context,uri)
        }
    }

    private fun getRealPathFromUriBelowAPI19(context: Context, uri: Uri?): String? {
        if (uri != null){
            return getDataColumn(context,uri,null, null)
        }else{
            return null
        }
    }



    private fun getRealPathFromUriAboveAPI19(context: Context, uri: Uri?): String? {
        if (uri != null){
            return getDataColumn(context,uri,null, null)
        }else{
            return null
        }
    }

    private fun getDataColumn(context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?): String? {
        var path:String? = null
//        val projection  = arrayOf(MediaStore.Images.Media.DATA)
//        for (i in projection){
//            Log.e("Projection[$i]",projection[0])
//        }

        var cursor :Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, null, selection,selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()){
//                val columnName  = cursor.columnNames
//                for(i in columnName){
//                    Log.e("ColumnName", i)
//                }

                val columnIndex:Int = cursor.getColumnIndexOrThrow("_data")//返回指定名称的列名称
                path = cursor.getString(columnIndex)//获取列的数据
            }
        }catch(e:Exception){
                cursor?.close()
        }
        return path
    }


    private fun load() : String{
        val content = StringBuilder()
        try{
            val input = openFileInput("data")//会自动去/data/data/<package name>/files/目录下寻找
            val reader = BufferedReader(InputStreamReader(input))
            reader.use {
                reader.forEachLine {
                    content.append(it)
                }
            }
        }catch ( e:IOException ){
            e.printStackTrace()
        }
        return content.toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        val inputText = findViewById<EditText>(R.id.editText1).text.toString()
        save(inputText)
    }

    private fun save(inputText: String) {
        try {
            val output = openFileOutput("data", Context.MODE_PRIVATE)//第一个参数是文件名
            val write = BufferedWriter(OutputStreamWriter(output))
            write.use{
                it.write(inputText)
            }
        }catch (e: IOException){
            e.printStackTrace()
        }
    }


}