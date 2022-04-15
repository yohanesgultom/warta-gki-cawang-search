package id.gultom.warta.service;

import id.gultom.warta.dto.Warta;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadService {

    final static String LATEST_WARTA_API_URL = "http://gki-cawang.org/wp-json/wp/v2/posts?_fields=id,date,link,categories&orderby=date&order=desc&categories=7&per_page=1";
    final static String GOOGLE_DRIVE_DOWNLOAD_URL = "https://drive.google.com/u/0/uc?export=download";

    private HttpClient httpClient;

    public DownloadService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String doHttpGet(String url) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest req = HttpRequest.newBuilder().uri(new URI(url)).GET().build();
        HttpResponse<String> res = this.httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        return res.body();
    }

    public Warta downloadLatestWartaPdf() throws URISyntaxException, IOException, InterruptedException {
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

}
