package br.com.luandiniz.tournamentmanager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import br.com.luandiniz.tournamentmanager.model.Duelista;

public class PDFGenerator {
    private final Context context;

    public PDFGenerator(Context context) {
        this.context = context;
    }

    public File generateRankPDF(List<Duelista> duelistas, String title) throws IOException {
        // Configurações do PDF
        String fileName = "Rank_" + System.currentTimeMillis() + ".pdf";
        File file = new File(context.getExternalFilesDir(null), fileName);

        PdfDocument document = new PdfDocument();

        // Tamanho da página (A4 em pixels a 72dpi)
        int pageWidth = 595;
        int pageHeight = 842;

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);

        // Margens
        int margin = 36;
        int y = margin;

        // Criar primeira página
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // 1. Cabeçalho do PDF
        paint.setTextSize(22);
        paint.setFakeBoldText(true);
        canvas.drawText(title, margin, y, paint);
        y += 30;

        paint.setTextSize(18);
        canvas.drawText("Rank", margin, y, paint);
        y += 30;

        // 2. Cabeçalho da tabela
        paint.setTextSize(12);
        paint.setFakeBoldText(true);
        canvas.drawText("Posição", margin, y, paint);
        canvas.drawText("Duelistas", margin + 100, y, paint);
        canvas.drawText("V", margin + 300, y, paint);
        canvas.drawText("D", margin + 350, y, paint);
        canvas.drawText("E", margin + 400, y, paint);
        canvas.drawText("P", margin + 450, y, paint);
        canvas.drawText("Pts", margin + 500, y, paint);
        y += 20;

        // Linha divisória
        paint.setStrokeWidth(1);
        canvas.drawLine(margin, y, pageWidth - margin, y, paint);
        y += 30;

        // 3. Conteúdo da tabela
        paint.setFakeBoldText(false);
        for (int i = 0; i < duelistas.size(); i++) {
            Duelista d = duelistas.get(i);

            // Verificar se precisa de nova página
            if (y > pageHeight - margin - 30) {
                document.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, document.getPages().size() + 1).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = margin;
            }

            canvas.drawText((i + 1) + "°", margin, y, paint);
            canvas.drawText(d.getNome(), margin + 100, y, paint);
            canvas.drawText(String.valueOf(d.getVitorias()), margin + 300, y, paint);
            canvas.drawText(String.valueOf(d.getDerrotas()), margin + 350, y, paint);
            canvas.drawText(String.valueOf(d.getEmpates()), margin + 400, y, paint);
            canvas.drawText(String.valueOf(d.getParticipacao()), margin + 450, y, paint);
            canvas.drawText(String.valueOf(d.getPontos()), margin + 500, y, paint);

            y += 30;
        }

        document.finishPage(page);

        // Salvar o documento
        FileOutputStream fos = new FileOutputStream(file);
        document.writeTo(fos);
        document.close();
        fos.close();

        return file;
    }
}
