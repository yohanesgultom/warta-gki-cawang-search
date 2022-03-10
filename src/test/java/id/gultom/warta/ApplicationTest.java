package id.gultom.warta;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApplicationTest {

    @Test
    @Disabled
    public void testDownloadLatestWartaPdf() {
        try {
            String path = Application.downloadLatestWartaPdf();
            assertTrue(new File(path).exists());
        } catch (Exception e) {
            assertTrue(false, "Download failed");
        }
    }

    @Test
    public void testFindPdfPagesContainingQuery() {
        try {
            String query = "Eveline Siregar";
            String pdfPath = "src/test/resources/WARTA_20220305.PDF";
            List<String> pages = Application.findPdfPagesContainingQuery(pdfPath, query);
            System.out.println(String.join(", ", pages));
            assertTrue(!pages.isEmpty());
        } catch (Exception e) {
            assertTrue(false, "Download failed");
        }
    }
}
