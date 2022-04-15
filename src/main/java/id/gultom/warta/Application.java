package id.gultom.warta;

import id.gultom.warta.dto.Warta;
import id.gultom.warta.service.DownloadService;
import id.gultom.warta.service.EmailService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.http.HttpClient;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Application {

    private DownloadService downloadService;
    private EmailService emailService;

    public Application(DownloadService downloadService, EmailService emailService) {
        this.downloadService = downloadService;
        this.emailService = emailService;
    }

    public List<String> findPdfPagesContainingQuery(String filePath, String query) throws IOException {
        List<String> pageNumbers = new ArrayList<>();
        PDDocument doc = PDDocument.load(new File(filePath));
//        System.out.println(String.format("Num of pages: %d", doc.getNumberOfPages()));
        PDFTextStripper textStripper = new PDFTextStripper();
        for (int i = 0; i < doc.getNumberOfPages(); i++) {
            textStripper.setStartPage(i);
            textStripper.setEndPage(i);
            String text = textStripper.getText(doc).strip().trim().toLowerCase(Locale.ROOT);
            if (text.contains(query.toLowerCase(Locale.ROOT))) {
//                System.out.println("Found in page " + i);
                pageNumbers.add(String.valueOf(i));
            }
        }
        return pageNumbers;
    }

    private void reportException(Exception e) {
        try {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            emailService.send("Warta GKI Cawang Error", sw.toString(), "yohanes.gultom@gmail.com");
        } catch (MessagingException ex) {
            ex.printStackTrace();
        }
    }

    public void run(String query, String emails) {
        try {
            Warta warta = downloadService.downloadLatestWartaPdf();
            List<String> pages = findPdfPagesContainingQuery(warta.getPath(), query);
            String body;
            if (pages.isEmpty()) {
                body = String.format("\uD83D\uDFE1 <em>\"%s\"</em> can not be found in Warta.", query);
            } else {
                body = String.format("\uD83D\uDFE2 <em>\"%s\"</em> found in Warta on page(s): <strong>%s</strong>.", query, String.join(", ", pages));
            }
            String postDateStr = warta.getPostDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
            String subject = String.format("⛪ Warta GKI Cawang %s", postDateStr);
            body = String.join("",
                    "<p>Hello \uD83D\uDC4B</p>",
                    String.format("<p>%s Please check warta in attachment for more details.</p>", body),
                    "<p style=\"color:grey\">--<br>Warta GKI Cawang Search</p>"
            );
            emailService.sendWithAttachment(warta.getPath(), subject, body, emails);
        } catch (Exception e) {
            e.printStackTrace();
            reportException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        String query = args[0];
        String emails = args[1];
        HttpClient httpClient = HttpClient.newBuilder().build();
        DownloadService downloadService = new DownloadService(httpClient);
        EmailService emailService = new EmailService();
        Application application = new Application(downloadService, emailService);
        application.run(query, emails);
    }

}
