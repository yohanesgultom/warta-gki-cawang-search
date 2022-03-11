package id.gultom.warta.dto;

import java.time.LocalDateTime;

public class Warta {
    LocalDateTime postDate;
    String fileName;
    String path;

    public Warta() {
    }

    public Warta(LocalDateTime postDate, String fileName, String path) {
        this.postDate = postDate;
        this.fileName = fileName;
        this.path = path;
    }
    
    public LocalDateTime getPostDate() {
        return postDate;
    }
    
    public void setPostDate(LocalDateTime postDate) {
        this.postDate = postDate;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
}
