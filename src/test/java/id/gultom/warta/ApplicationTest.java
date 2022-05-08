package id.gultom.warta;

import id.gultom.warta.dto.Warta;
import id.gultom.warta.service.DownloadService;
import id.gultom.warta.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.MessagingException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApplicationTest {

    final static String TEST_WARTA_PATH = "src/test/resources/WARTA_20220305.PDF";

    Application application;

    @Mock
    DownloadService downloadService;

    @Mock
    EmailService emailService;

    @BeforeEach
    public void setUp() throws URISyntaxException, IOException, InterruptedException {
        application = new Application(downloadService, emailService);
    }

    @Test
    public void testFindPdfPagesContainingQuery() {
        try {
            String query = "Eveline";
            List<String> pages = application.findPdfPagesContainingQuery(TEST_WARTA_PATH, query);
            System.out.println(String.join(", ", pages));
            assertTrue(!pages.isEmpty());
        } catch (IOException e) {
            assertTrue(false, "Download failed");
        }
    }

    @Test
    public void testFindPdfPagesNotContainingQuery() {
        try {
            String query = "Kamen Rider";
            List<String> pages = application.findPdfPagesContainingQuery(TEST_WARTA_PATH, query);
            assertTrue(pages.isEmpty());
        } catch (IOException e) {
            assertTrue(false, "Download failed");
        }
    }

    @Test
    public void testRun() {
        try {
            // mocks
            Path testWartaPath = Paths.get(TEST_WARTA_PATH);
            Warta testWarta = new Warta();
            testWarta.setPostDate(LocalDateTime.of(2022, 3, 5, 0, 0));
            testWarta.setFileName(testWartaPath.getFileName().toString());
            testWarta.setPath(testWartaPath.toString());
            when(downloadService.downloadLatestWartaPdf()).thenReturn(testWarta);

            // test
            application.runEmail("Eveline", "test@email.com");

            // verify
            ArgumentCaptor<String> attachmentCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> emailsCaptor = ArgumentCaptor.forClass(String.class);
            verify(emailService, times(1)).sendWithAttachment(
                    attachmentCaptor.capture(),
                    subjectCaptor.capture(),
                    bodyCaptor.capture(),
                    emailsCaptor.capture()
            );
            assertEquals(TEST_WARTA_PATH, attachmentCaptor.getValue());
            assertEquals("⛪ Warta GKI Cawang 05 March 2022", subjectCaptor.getValue());
            assertTrue(bodyCaptor.getValue().contains("<em>\"Eveline\"</em> found in Warta"));
            assertEquals("test@email.com", emailsCaptor.getValue());
        } catch (URISyntaxException e) {
            assertTrue(false, "PDF download failed");
        } catch (IOException e) {
            assertTrue(false, "PDF download failed");
        } catch (InterruptedException e) {
            assertTrue(false, "PDF download failed");
        } catch (MessagingException e) {
            assertTrue(false, "Email sending failed");
        }
    }

    @Test
    public void testRunNotFound() {
        try {
            // mocks
            Path testWartaPath = Paths.get(TEST_WARTA_PATH);
            Warta testWarta = new Warta();
            testWarta.setPostDate(LocalDateTime.of(2022, 3, 5, 0, 0));
            testWarta.setFileName(testWartaPath.getFileName().toString());
            testWarta.setPath(testWartaPath.toString());
            when(downloadService.downloadLatestWartaPdf()).thenReturn(testWarta);

            // test
            application.runEmail("Kamen Rider", "test@email.com");

            // verify
            ArgumentCaptor<String> attachmentCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> emailsCaptor = ArgumentCaptor.forClass(String.class);
            verify(emailService, times(1)).sendWithAttachment(
                    attachmentCaptor.capture(),
                    subjectCaptor.capture(),
                    bodyCaptor.capture(),
                    emailsCaptor.capture()
            );
            assertEquals(TEST_WARTA_PATH, attachmentCaptor.getValue());
            assertEquals("⛪ Warta GKI Cawang 05 March 2022", subjectCaptor.getValue());
            assertTrue(bodyCaptor.getValue().contains("<em>\"Kamen Rider\"</em> can not be found in Warta"));
            assertEquals("test@email.com", emailsCaptor.getValue());
        } catch (URISyntaxException e) {
            assertTrue(false, "PDF download failed");
        } catch (IOException e) {
            assertTrue(false, "PDF download failed");
        } catch (InterruptedException e) {
            assertTrue(false, "PDF download failed");
        } catch (MessagingException e) {
            assertTrue(false, "Email sending failed");
        }
    }
}
