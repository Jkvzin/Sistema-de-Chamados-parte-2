package com.example.trabalho2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.InputStream;

/**
 * Utilitario para carregar imagens com rotacao EXIF aplicada.
 * Corrige o problema de fotos tiradas em pe (retrato) aparecerem deitadas.
 */
public class ImageUtils {

    /**
     * Carrega um bitmap com subsampling e rotacao EXIF aplicada.
     * Suporta content:// URI (MediaStore) e caminhos de arquivo absolutos.
     *
     * @param context   Contexto para acessar ContentResolver
     * @param path      URI (content://) ou caminho de arquivo
     * @param reqWidth  Largura maxima desejada (para subsampling)
     * @param reqHeight Altura maxima desejada (para subsampling)
     * @return Bitmap rotacionado e redimensionado, ou null em caso de erro
     */
    public static Bitmap loadRotatedBitmap(Context context, String path, int reqWidth, int reqHeight) {
        if (path == null || path.isEmpty()) return null;

        try {
            // Passo 1: ler apenas as dimensoes (bounds only)
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            decodeBounds(context, path, options);

            // Passo 2: calcular inSampleSize
            options.inSampleSize = calcularInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;

            // Passo 3: decodificar o bitmap
            Bitmap bitmap = decodeBitmap(context, path, options);
            if (bitmap == null) return null;

            // Passo 4: ler orientacao EXIF e rotacionar
            int orientation = getExifOrientation(context, path);
            Bitmap rotated = rotateBitmap(bitmap, orientation);

            // Libera o bitmap original se foi rotacionado
            if (rotated != bitmap) {
                bitmap.recycle();
            }

            return rotated;
        } catch (Exception e) {
            return null;
        }
    }

    private static void decodeBounds(Context context, String path, BitmapFactory.Options options) throws Exception {
        if (path.startsWith("content://")) {
            Uri uri = Uri.parse(path);
            try (InputStream is = context.getContentResolver().openInputStream(uri)) {
                if (is != null) {
                    BitmapFactory.decodeStream(is, null, options);
                }
            }
        } else {
            File file = new File(path);
            if (file.exists()) {
                BitmapFactory.decodeFile(path, options);
            }
        }
    }

    private static Bitmap decodeBitmap(Context context, String path, BitmapFactory.Options options) throws Exception {
        if (path.startsWith("content://")) {
            Uri uri = Uri.parse(path);
            try (InputStream is = context.getContentResolver().openInputStream(uri)) {
                if (is != null) {
                    return BitmapFactory.decodeStream(is, null, options);
                }
            }
        } else {
            File file = new File(path);
            if (file.exists()) {
                return BitmapFactory.decodeFile(path, options);
            }
        }
        return null;
    }

    private static int getExifOrientation(Context context, String path) {
        try {
            ExifInterface exif;
            if (path.startsWith("content://")) {
                Uri uri = Uri.parse(path);
                try (InputStream is = context.getContentResolver().openInputStream(uri)) {
                    if (is != null) {
                        exif = new ExifInterface(is);
                    } else {
                        return ExifInterface.ORIENTATION_NORMAL;
                    }
                }
            } else {
                exif = new ExifInterface(path);
            }
            return exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
        } catch (Exception e) {
            return ExifInterface.ORIENTATION_NORMAL;
        }
    }

    /**
     * Aplica a rotacao/flip EXIF ao bitmap.
     */
    private static Bitmap rotateBitmap(Bitmap source, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.postScale(1, -1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.postRotate(270);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.postRotate(90);
                matrix.postScale(-1, 1);
                break;
            default:
                return source; // ORIENTATION_NORMAL (1) ou ORIENTATION_UNDEFINED (0)
        }

        Bitmap rotated = Bitmap.createBitmap(
                source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        return rotated;
    }

    /**
     * Calcula o fator de subsampling para redimensionar a imagem.
     */
    private static int calcularInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int altura = options.outHeight;
        int largura = options.outWidth;
        int inSampleSize = 1;
        if (altura > reqHeight || largura > reqWidth) {
            int metadeAltura = altura / 2;
            int metadeLargura = largura / 2;
            while ((metadeAltura / inSampleSize) >= reqHeight
                    && (metadeLargura / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
