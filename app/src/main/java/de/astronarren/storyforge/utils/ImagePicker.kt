package de.astronarren.storyforge.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

@Composable
fun rememberImagePicker(
    onImageSelected: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val savedPath = saveImageToInternalStorage(context, selectedUri)
            savedPath?.let { path ->
                onImageSelected(path)
            }
        }
    }
    
    return {
        imagePickerLauncher.launch("image/*")
    }
}

@Composable
fun rememberImagePickerWithCrop(
    onImageSelected: (String) -> Unit,
    onImageSelectedForCrop: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val tempPath = saveImageToInternalStorage(context, selectedUri, isTemp = true)
            tempPath?.let { path ->
                onImageSelectedForCrop(path)
            }
        }
    }
    
    return {
        imagePickerLauncher.launch("image/*")
    }
}

@Composable
fun rememberCharacterPortraitPicker(
    onImageSelected: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val savedPath = saveCharacterPortraitToInternalStorage(context, selectedUri)
            savedPath?.let { path ->
                onImageSelected(path)
            }
        }
    }
    
    return {
        imagePickerLauncher.launch("image/*")
    }
}

private fun saveImageToInternalStorage(context: Context, uri: Uri, isTemp: Boolean = false): String? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        
        // Create a unique filename
        val prefix = if (isTemp) "temp_cover" else "book_cover"
        val filename = "${prefix}_${UUID.randomUUID()}.jpg"
        val folderName = if (isTemp) "temp_images" else "book_covers"
        val file = File(context.filesDir, folderName)
        if (!file.exists()) {
            file.mkdirs()
        }
        
        val imageFile = File(file, filename)
        val outputStream = FileOutputStream(imageFile)
        
        // Compress and save the bitmap
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        outputStream.close()
        
        imageFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun saveCharacterPortraitToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        
        // Create a unique filename for character portrait
        val filename = "character_portrait_${UUID.randomUUID()}.jpg"
        val folderName = "character_portraits"
        val file = File(context.filesDir, folderName)
        if (!file.exists()) {
            file.mkdirs()
        }
        
        val imageFile = File(file, filename)
        val outputStream = FileOutputStream(imageFile)
        
        // Compress and save the bitmap
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        outputStream.close()
        
        imageFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun cropAndSaveImage(context: Context, imagePath: String, cropRect: Rect): String? {
    return try {
        val originalBitmap = BitmapFactory.decodeFile(imagePath)
        
        // Calculate crop dimensions
        val cropX = max(0, cropRect.left.toInt())
        val cropY = max(0, cropRect.top.toInt())
        val cropWidth = min(originalBitmap.width - cropX, cropRect.width.toInt())
        val cropHeight = min(originalBitmap.height - cropY, cropRect.height.toInt())
        
        // Create cropped bitmap
        val croppedBitmap = Bitmap.createBitmap(
            originalBitmap,
            cropX,
            cropY,
            cropWidth,
            cropHeight
        )
        
        // Save cropped image
        val filename = "book_cover_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, "book_covers")
        if (!file.exists()) {
            file.mkdirs()
        }
        
        val imageFile = File(file, filename)
        val outputStream = FileOutputStream(imageFile)
        
        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        outputStream.close()
        
        // Clean up bitmaps
        originalBitmap.recycle()
        croppedBitmap.recycle()
        
        // Delete temporary file
        deleteImageFile(imagePath)
          imageFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun deleteImageFile(imagePath: String?) {
    if (imagePath != null) {
        try {
            val file = File(imagePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

