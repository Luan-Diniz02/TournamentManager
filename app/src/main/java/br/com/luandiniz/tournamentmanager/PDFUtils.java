package br.com.luandiniz.tournamentmanager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;

public class PDFUtils {
    public static void abrirPDF(Context context, File file) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    file
            );

            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            PackageManager pm = context.getPackageManager();
            if (intent.resolveActivity(pm) != null) {
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Nenhum visualizador de PDF instalado", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Erro ao abrir PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static void compartilharPDF(Context context, File file) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            Uri uri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    file
            );

            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(shareIntent, "Compartilhar PDF via"));
        } catch (Exception e) {
            Toast.makeText(context, "Erro ao compartilhar PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
