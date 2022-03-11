package id.gultom.warta;

import id.gultom.warta.dto.Warta;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Application {
    final static String LATEST_WARTA_API_URL = "http://gki-cawang.org/wp-json/wp/v2/posts?_fields=id,date,link,categories&orderby=date&order=desc&categories=7&per_page=1";
    final static String GOOGLE_DRIVE_DOWNLOAD_URL = "https://drive.google.com/u/0/uc?export=download";

    public static String doHttpGet(String url) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest req = HttpRequest.newBuilder().uri(new URI(url)).GET().build();
        HttpResponse<String> res = HttpClient.newBuilder().build().send(req, HttpResponse.BodyHandlers.ofString());
        return res.body();
    }

    public static Warta downloadLatestWartaPdf() throws URISyntaxException, IOException, InterruptedException {
        Warta warta = new Warta();
        JSONArray resJson = new JSONArray(doHttpGet(LATEST_WARTA_API_URL));
        JSONObject latestPostJson = resJson.getJSONObject(0);
        String postDate = latestPostJson.getString("date");
        warta.setPostDate(LocalDateTime.parse(postDate));
        String postUrl = latestPostJson.getString("link");
        System.out.println("✅ Latest warta post URL found: " + postUrl);
        String postHtml = doHttpGet(postUrl);
        Pattern pattern = Pattern.compile("https://drive.google.com/file/d/([^/]+)/");
        Matcher matcher = pattern.matcher(postHtml);
        if (!matcher.find()) {
            throw new RuntimeException("❌ Unable to find Google Drive link \uD83D\uDE41");
        } else {
            String fileId = matcher.group(1);
            String googleUrl = GOOGLE_DRIVE_DOWNLOAD_URL + "&id=" + fileId;
            System.out.println("Downloading " + googleUrl + "...");
            warta.setFileName("WARTA_" + warta.getPostDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".PDF");
            warta.setPath(warta.getFileName());
            File wartaFile = new File(warta.getPath());
            if (!wartaFile.exists()) {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(new URI(googleUrl)).GET().build();
                HttpResponse<Path> res = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.ALWAYS)
                        .build()
                        .send(req, HttpResponse.BodyHandlers.ofFile(Path.of(warta.getPath())));
                if (!res.body().toFile().exists()) {
                    throw new RuntimeException("❌ Warta PDF download failed \uD83D\uDE41");
                }
                System.out.println("✅ Latest warta PDF saved in " + warta.getPath());
            }
        }
        return warta;
    }

    public static List<String> findPdfPagesContainingQuery(String filePath, String query) throws IOException {
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

    public static void sendNotificationEmail(
            String pdfPath,
            String subject,
            String body,
            String emails) throws IOException, MessagingException {
        try (InputStream output = new FileInputStream("config.properties")) {
            Properties props = new Properties();
            props.load(output);
            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                        props.getProperty("mail.smtp.user"),
                        props.getProperty("mail.smtp.password")
                    );
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(props.getProperty("mail.smtp.user")));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emails));
            message.setSubject(subject);
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(body, "text/html; charset=UTF-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(new File(pdfPath));
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);
            System.out.println(String.format("Sending email to %s...", emails));
            Transport.send(message);
        }
    }

    public static void main(String[] args) {
        try {
            String query = args[0];
            String emails = args[1];
            Warta warta = downloadLatestWartaPdf();
            List<String> pages = findPdfPagesContainingQuery(warta.getPath(), query);
            String body;
            if (pages.isEmpty()) {
                body = String.format("\uD83D\uDFE1 <em>\"%s\"</em> can not be found in Warta.");
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
            sendNotificationEmail(warta.getPath(), subject, body, emails);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
