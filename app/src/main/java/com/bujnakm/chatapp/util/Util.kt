package com.bujnakm.chatapp.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

/**
 * A singleton providing decodeBase64ToBitmap functionality
 */
object Util {
    /**
     * Decodes a Base64 encoded string into a Bitmap image.
     * @param base64String The Base64 encoded string.
     * @return The decoded Bitmap image.
     */
    public fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val byteArray = Base64.decode(base64String, Base64.DEFAULT)
        val originalBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

        return cropToCircle(originalBitmap) // Crop image to a circle
    }
    private fun cropToCircle(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val canvas = android.graphics.Canvas(output)
        val paint = android.graphics.Paint()
        val rect = android.graphics.Rect(0, 0, size, size)

        paint.isAntiAlias = true
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

}