package com.example.filepersistencetest

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import java.io.*

object GetAbsolutePath {
    public fun getFilePathFromURI(context:Context, contentUri: Uri):String? {
        val rootDataDir: File? = context.getExternalFilesDir(null)
//        MyApplication.getMyContext().getExternalFilesDir(null).getPath()
        val fileName:String? = getFileName(contentUri)
        if (!TextUtils.isEmpty(fileName)) {
            val copyFile:File = File(rootDataDir.toString() + File.separator + fileName)
            copyFile(context, contentUri, copyFile)
            return copyFile.getAbsolutePath()
        }
        return null
    }

    private fun copyFile(context: Context, uri: Uri, dstFile: File) {
        try {
            val inputStream: InputStream? = context.getContentResolver ().openInputStream(uri)
            if (inputStream == null) {
                return
            }else{
                val outputStream: OutputStream = FileOutputStream(dstFile)
                copyStream(inputStream, outputStream)
                inputStream.close()
                outputStream.close()
            }
        }catch (e:Exception) {
            e.printStackTrace()
        }
    }

    private fun copyStream(input: InputStream, output: OutputStream):Int {//throws Exception, IOException
        val BUFFER_SIZE:Int = 1024 * 2
        val buffer = ByteArray(BUFFER_SIZE)
        val getIn = BufferedInputStream(input, BUFFER_SIZE)
        val getOut = BufferedOutputStream(output, BUFFER_SIZE)
        var count = 0
        try {
            val n = buffer?.let { getIn.read(it,0, BUFFER_SIZE) }
            while (n != -1) {
                getOut.write(buffer, 0, n)
                count += n
            }
            getOut.flush()
        } finally {
            try {
                getOut.close()
                getIn.close()
            } catch ( e:IOException) {
                e.printStackTrace()
            }
        }
        return count
    }

    private fun getFileName(uri: Uri?): String? {
        val path = uri?.getPath()
        val cut: Int? = path?.lastIndexOf('/');
        if (cut != -1 && cut != null) {
            return path?.substring(cut + 1);
        }
        return null
    }

    public fun getFileAbsolutePath(context: Context, imageUri: Uri){

    }




}