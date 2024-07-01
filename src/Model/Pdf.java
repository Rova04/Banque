package Model;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class Pdf {

    public static void main(String[] args) {
        // Nom du fichier PDF à générer
        String filename = "example.pdf";

        // Contenu du PDF
        String entete = "Nom du banque \n Date: 22/10/24 \n AVIS DE VIREMENT N°005";
        String suivant = "Contenu du PDF généré avec iText.";
        String next = "Contenu du PDF généré avec iText.";
        String ambany = "Contenu du PDF généré avec iText.";

        // Générer le PDF
        generatePDF(filename, entete, suivant, next, ambany);
    }

    public static void generatePDF(String filename, String entete, String bodyleft, String bodyright, String bottom) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            Paragraph pEntete = new Paragraph(entete);
            pEntete.setAlignment(Element.ALIGN_CENTER);
            Paragraph pbodyleft = new Paragraph(bodyleft);
            Paragraph pA = new Paragraph("A");
            pA.setAlignment(Element.ALIGN_CENTER);
            Paragraph pbodyright = new Paragraph(bodyright);
            pbodyright.setAlignment(Element.ALIGN_RIGHT);
            Paragraph pbottom = new Paragraph(bottom);

            document.add(pEntete);
            document.add(pbodyleft);
            document.add(pA);
            document.add(pbodyright);
            document.add(pbottom);

            document.close();
            System.out.println("PDF généré avec succès !");
        } catch (FileNotFoundException | DocumentException e) {
            e.printStackTrace();
        }
    }
}
