package com.example.filepersistencetest

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import com.example.filepersistencetest.myApplication.Companion.context

object GetFilePath {
    public fun getFilePathByUri(uri: Uri):String?{
        var path:String? = null


        if (ContentResolver.SCHEME_FILE == uri.scheme){
            Log.e("getFilePath","SCHEME_FILE")
            path = uri.path
            return path
        }


        if (ContentResolver.SCHEME_CONTENT == uri.scheme){
            Log.e("getFilePath","SCHEME_CONTENT--->")
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){//API19之前的
                Log.e("getFilePath","BelowAPI19--->")
                val cursor:Cursor? = context.contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DATA),null,null,null)
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    if (columnIndex > -1) {
                        path = cursor.getString(columnIndex)
                    }
                    cursor?.close()
                }
                return path
            }else{//API19及之后的
                Log.e("getFilePath","AboveAPI19--->")
                //DocumentProvider
                if (DocumentsContract.isDocumentUri(context,uri)){
                    Log.e("getFilePath","isDocumentUri--->")
                    when(uri.authority){
                        //ExternalStorageProvider
                        "com.android.externalstorage.documents" -> {
                            Log.e("getFilePath","externalstorage--->")
                            val docId = DocumentsContract.getDocumentId(uri)
                            Log.e("docId",docId)
                            val split = docId.split(":")
                            val type = split[0]
                            if ("primary".equals(type,true)) {
                                path = Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                                return path
                            }else if("home".equals(type,true)){
                                path = Environment.getExternalStorageDirectory().toString() + "/" +"documents" + "/" + split[1]
                                return path
                            }else{
                                path = uri.path
                                Log.e("U盘",path.toString())
                                return path
                            }
                        }
                        //downloadsProvider
                        "com.android.providers.downloads.documents" -> {
                            Log.e("getFilePath","downloads--->")
                            val downId = DocumentsContract.getDocumentId(uri)
                            Log.e("downId",downId.toString())
                            if(downId != null && downId.startsWith("raw:")){
                                return downId.substring(startIndex = 4)
                            }
                            val split = downId.split(":")
//                            val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(downId[1]))//,ContentUris.parseId(uri)
                            val contentUriPreFixesToTry = arrayOf(
                                "content://downloads/public_downloads/"+split[0],
                                "content://downloads/my_downloads/"+split[0],
                                "content://downloads/all_downloads/"+split[0],

                            )

                            for (contentUriPrefix in contentUriPreFixesToTry){
//                                val contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), split[1].toLong())
                                val contentUri = Uri.parse("content://downloads/*")//(contentUriPrefix + split[1])
                                try {
                                    path = getDataColumn(context,contentUri,null,null)
                                    if(!TextUtils.isEmpty(path)){
                                        return path
                                    }
                                }catch (e:Exception){
                                    Log.e("Downloads",e.toString())
                                }
                            }

                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                                return uri.path
                            }
                            return path
                        }
                        //MediaProvider
                        "com.android.providers.media.documents" -> {
                            Log.e("getFilePath","media--->")
                            val docId:String = DocumentsContract.getDocumentId(uri)
                            val split = docId.split(":")
                            val type = split[0]
                            var contentUri:Uri? = null
                            when(type){
                                "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                                "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                                "document" -> {
                                    Log.e("tryDocumentPath", DocumentsContract.Path.CONTENTS_FILE_DESCRIPTOR.toString())
                                    path = Environment.getExternalStorageDirectory().toString() + "/" +"Documents" + "/" +
                                            getNameColumn(context, uri,null,null)
                                    return path

//                                    contentUri = Uri.parse("content://0@media/external/file/"+split[1])
//                                    path = contentUri?.let { getDataColumn(context,it,null,null) }

//                                    return path
                                }
                            }
                            val selection:String = "_id=?"
                            val selectionArgs = arrayOf(split[1])
                            path = contentUri?.let { getDataColumn(context,it,selection,selectionArgs) }
                            return path
                        }
                    }
                }else if ("content".equals(uri.scheme,true)){//MediaStore(and general)
                    //return the remote address
                    if ("com.google.android.apps.photos.content" == uri.authority){
                        return uri.lastPathSegment
                    }else if ("com.huawei.hidisk.fileprovider" == uri.authority){
                        val uriPath = uri.path
                        if (uriPath != null && uriPath.startsWith("/root")){
                            return uriPath.replace("/root".toRegex(),"")
                        }
                    }
                    path = getDataColumn(context,uri,null,null)
                    if (path == null){
                        return "/storage/emulated/0/" + uri.toString().split("/0/")[1]
                    }else{
                        return path
                    }
                }else if("file".equals(uri.scheme,true)){ //File
                    Log.e("urischeme",uri.scheme.toString())
                    return uri.path
                }
            }
        }
//        val message  =uri.scheme
//        Log.e("getFilePath","SchemeError: $message")
        return path
    }

    private fun getDataColumn(context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?): String? {
        var path:String? = null
        val column = "_data"
        val projection  = arrayOf(column)
        var cursor : Cursor? = null
        try {
            Log.e("getDataColumn:Uri", uri.toString())
            cursor = context.contentResolver.query(uri, projection, selection,selectionArgs, null)
            Log.e("getDataColumn", (cursor?.moveToFirst() ).toString())
            if (cursor != null && cursor.moveToFirst()){
                    val columnName  = cursor.columnNames
                    for(i in columnName){
                        Log.e("ColumnName", i)
                    }
                    val columnIndex:Int = cursor.getColumnIndexOrThrow(column)//返回指定名称的列名称
                    path = cursor.getString(columnIndex)//获取列的数据
            }
        }catch(e:Exception){
            Log.e("getDataColumn",e.toString())
            Log.e("getDataColumn","ERROR!")
            cursor?.close()
        }finally {
            cursor?.close()
        }
        return path
    }

    private fun getNameColumn(context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?): String? {
        var name:String? = null
        val column = "_display_name"
        val projection  = arrayOf(column)
        var cursor : Cursor? = null
        try {
            Log.e("getDataColumn:Uri", uri.toString())
            cursor = context.contentResolver.query(uri, null, selection,selectionArgs, null)
            Log.e("getDataColumn", (cursor?.moveToFirst() ).toString())
            if (cursor != null && cursor.moveToFirst()){
                    val columnName  = cursor.columnNames
                    for(i in columnName){
                        Log.e("ColumnName:$i", cursor.getString(cursor.getColumnIndexOrThrow(i)))
                    }
                val columnIndex:Int = cursor.getColumnIndexOrThrow(column)//返回指定名称的列名称
                name = cursor.getString(columnIndex)//获取列的数据
            }
        }catch(e:Exception){
            Log.e("getDataColumn",e.toString())
            Log.e("getDataColumn","ERROR!")
            cursor?.close()
        }finally {
            cursor?.close()
        }
        return name
    }

}