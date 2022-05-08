package id.gultom.warta.service;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;

public class WaService {

    final static String WAPI_URL = "https://wapi.teknoedu.id";

    private HttpClient httpClient;

    private Properties config;

    public WaService(HttpClient httpClient) throws IOException {
        this.httpClient = httpClient;
        try (InputStream output = new FileInputStream("config.properties")) {
            config = new Properties();
            config.load(output);
        }
    }

    public String doHttpPost(String url, String body, Map<String, String> headers) throws IOException, InterruptedException, URISyntaxException {
        System.out.printf("url: %s\n", url);
//        System.out.printf("body: %s\n", body);
        System.out.printf("headers: %s\n", headers);
        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(new URI(url))
                .POST(HttpRequest.BodyPublishers.ofString(body, Charset.forName("UTF8")));
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            reqBuilder.header(entry.getKey(), entry.getValue());
        }
        HttpRequest req = reqBuilder.build();
        HttpResponse<String> res = this.httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        return res.body();
    }

    public void send(String body, String numbers) throws IOException, URISyntaxException, InterruptedException {
        for (String number : numbers.split(",")) {
            String payload = String.format("{\"number\":\"%s\",\"message\":\"%s\"}", number.strip(), body);
            String res = doHttpPost(WAPI_URL + "/send", payload, Map.of(
                    "Content-Type", "application/json",
                    "Authorization", config.getProperty("wa.auth.header")
            ));
            System.out.println(res);
            Thread.sleep(1000);
        }
    }

    public void sendWithAttachment(
            String attachmentPath,
            String body,
            String numbers) throws IOException, URISyntaxException, InterruptedException {
        File file = new File(attachmentPath);
        String mimeType = "application/octet-stream";
        Path path = file.toPath();
        try {
            mimeType = Files.probeContentType(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String encodedFile = Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(file));
        for (String number : numbers.split(",")) {
            System.out.printf("number: %s, message: %s, filename: %s mime: %s\n",  number.strip(), body, path.getFileName().toString(), mimeType);
            String payload = String.format(
                    "{\"number\":\"%s\",\"message\":\"%s\",\"attachments\":[{\"filename\":\"%s\",\"mime\":\"%s\",\"content\":\"%s\"}]}",
                    number.strip(), body, path.getFileName().toString(), mimeType, encodedFile);
//            String payload = String.format(
//                    "{\"number\":\"%s\",\"message\":\"%s\"}",
//                    number.strip(), body);
            String res = doHttpPost(WAPI_URL + "/send", payload, Map.of(
                    "Content-Type", "application/json",
                    "Authorization", config.getProperty("wa.auth.header")
            ));
            System.out.println(res);
            Thread.sleep(1000);
        }
    }
}
