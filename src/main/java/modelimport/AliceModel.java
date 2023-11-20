package modelimport;

public class AliceModel {
    private String name; // possibly temporary
    private String webUrl;
    private String thumbnailUrl;
    private String localPath;

    public AliceModel(String name, String webUrl, String thumbnailUrl, String localPath) {
        this.name = name;
        this.webUrl = webUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.localPath = localPath;
    }

    public AliceModel(String name, String webUrl, String thumbnailUrl) {
        this(name, webUrl, thumbnailUrl, null);
    }

    public String getName() {
        return name;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
}
