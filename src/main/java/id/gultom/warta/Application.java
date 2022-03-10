package id.gultom.warta;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
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

    public static String downloadLatestWartaPdf() throws URISyntaxException, IOException, InterruptedException {
        String pdfPath = null;
        JSONArray resJson = new JSONArray(doHttpGet(LATEST_WARTA_API_URL));
        JSONObject latestPostJson = resJson.getJSONObject(0);
        String postDate = latestPostJson.getString("date");
        LocalDateTime postDateTime = LocalDateTime.parse(postDate);
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
            pdfPath = "WARTA_" + postDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".PDF";
            File wartaFile = new File(pdfPath);
            if (!wartaFile.exists()) {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(new URI(googleUrl)).GET().build();
                HttpResponse<Path> res = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.ALWAYS)
                        .build()
                        .send(req, HttpResponse.BodyHandlers.ofFile(Path.of(pdfPath)));
                if (!res.body().toFile().exists()) {
                    throw new RuntimeException("❌ Warta PDF download failed \uD83D\uDE41");
                }
                System.out.println("✅ Latest warta PDF saved in " + pdfPath);
            }
        }
        return pdfPath;
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

    public static void main(String[] args) {
        try {
            String query = args[0];
            String filePath = downloadLatestWartaPdf();
            List<String> pages = findPdfPagesContainingQuery(filePath, query);
            if (pages.isEmpty()) {
                System.out.println(String.format("✅ %s can not be found"));
            } else {
                System.out.println(String.format("✅ %s found in page(s): %s", query, String.join(", ", pages)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
