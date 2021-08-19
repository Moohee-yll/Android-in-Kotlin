package com.example.filepersistencetest

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import com.example.filepersistencetest.GetFilePath.getFilePathByUri
import java.io.*
import java.lang.IndexOutOfBoundsException
import java.nio.charset.Charset

object FileOperateUtils {

    val contentResolver = myApplication.context.contentResolver

    // Request code for creating a PDF document.

    fun createFile(pickerInitialUri: Uri? = null):Intent {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/text"//指定文件类型
            putExtra(Intent.EXTRA_TITLE, "Document.txt")//指定文件名称
            pickerInitialUri?.let { putExtra(DocumentsContract.EXTRA_INITIAL_URI, it) } //打开初始文件夹
        }
        return intent
    }

    fun openDirectory(pickerInitialUri: Uri? = null):Intent {
        // Choose a directory using the system's file picker.
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            // Provide read access to files and sub-directories in the user-selected
            // directory.
//            val takeFlags: Int = (resultData.getFlags()
//                    and (Intent.FLAG_GRANT_READ_URI_PERMISSION
//                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
            flags =Intent.FLAG_GRANT_WRITE_URI_PERMISSION//设置权限 Intent.FLAG_GRANT_READ_URI_PERMISSION or
            pickerInitialUri?.let{ putExtra(DocumentsContract.EXTRA_INITIAL_URI, it) } //打开初始文件夹
        }
        return intent
    }

    //获取图片的元数据：名称+大小
    fun dumpImageData(dataUri:Uri){
        val TAG = "dumpImageData"
        val cursor:Cursor?  =contentResolver.query(dataUri,null,null,null,null)
        cursor?.use {
            if(it.moveToFirst()){
                val displayName: String =
                    it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                Log.i(TAG, "Display Name: $displayName")
                val sizeIndex: Int = it.getColumnIndex(OpenableColumns.SIZE)
                val size: String = if (!it.isNull(sizeIndex)) {
                    // Technically the column stores an int, but cursor.getString()
                    // will do the conversion automatically.
                    it.getString(sizeIndex)
                } else {
                    "Unknown"
                }
                Log.i(TAG, "Size: $size")
            }
        }
    }

    //获取位图,以Bitmap返回
    fun getBitmapFromUri(uri:Uri):Bitmap{
        val parcelFileDescriptor:ParcelFileDescriptor? =
            contentResolver.openFileDescriptor(uri,"r")
        val fileDescriptor:FileDescriptor? = parcelFileDescriptor?.fileDescriptor
        val image:Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor?.close()
        return image
    }

    //读文件的内容，以字符串返回
    fun readTextFromUri(uri:Uri):ByteArray{
        val stringBuilder  = StringBuilder()
        contentResolver.openInputStream(uri)?.use { inputStream ->
//            val MAX_SIZE = 1024*1000
//            val text = ByteArray(MAX_SIZE) //text大小是MAX_SIZE
//            inputStream.read(text)
            return inputStream.readBytes()



//            BufferedReader(InputStreamReader(inputStream)).use{ reader ->
//                var line:String? = reader.readLine()
//                while (line != null){
//                    stringBuilder.append(line)
//                    line = reader.readLine()
//                }
//            }


        }
        Log.e("ReadFile","Fail")
        return ByteArray(0)

//        return stringBuilder.toString()
    }

    //修改文档的内容
    //Document.COLUMN_FLAGS 中 FLAG_SUPPORTS_WRITE的值为true，才可以修改
    fun alterDocument(uri:Uri){
        try{
            contentResolver.openFileDescriptor(uri,"w")?.use{
                FileOutputStream(it.fileDescriptor).use{
                    it.write(
                        ("4444")
                            .toByteArray()
                    )
                }
            }
        }catch (e:FileNotFoundException){
            e.printStackTrace()
        }catch (e: IOException){
            e.printStackTrace()
        }
    }

    fun alterDocumentByAbsolutePath(uri: Uri):Boolean{
        val filePath = getFilePathByUri(uri)
        val file = File(filePath)
        Log.e("AlterFile",filePath.toString())
        if (file.exists()){
            try{
                file.writeBytes(("101001001101011110101010101010101010101001010101").toByteArray())
                return true
            }catch (e:FileNotFoundException){
                e.printStackTrace()
                return false
            }catch (e: IOException){
                e.printStackTrace()
                return false
            }
        }else{
            Log.e("AlterFile","Path Error!")
            return false
        }

    }

    //删除某文件
    //Document.COLUMN_FLAGS 包含 SUPPORTS_DELETE，才可以修改
    fun deleteDocument(uri:Uri){
        val TAG = "deleteDocument"
        //contentResolver.takePersistableUriPermission(uri,)
        try {
            DocumentsContract.deleteDocument(contentResolver, uri)
            Log.i(TAG, "Successful!")
        }catch (e:FileNotFoundException){
            e.printStackTrace()
        }catch (e: IOException){
            e.printStackTrace()
        }
    }

    fun deleteDocumentByAbsolutePath(uri:Uri):Boolean{
        val filePath = getFilePathByUri(uri)
        val file = File(filePath)
        Log.e("DeleteFile",filePath.toString())
        if (file.exists()){
            try{
                file.delete()
                return true
            }catch (e:FileNotFoundException){
                e.printStackTrace()
                return false
            }catch (e: IOException){
                e.printStackTrace()
                return false
            }
        }else{
            Log.e("deleteFile","Path Error!")
            return false
        }

    }




}