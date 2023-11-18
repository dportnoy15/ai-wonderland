package modelimport;

public class AliceModel {
    private String name; // possibly temporary
    private String webUrl;
    private String thumbnailUrl;

    public AliceModel(String name, String webUrl, String thumbnailUrl) {
        this.name = name;
        this.webUrl = webUrl;
        this.thumbnailUrl = thumbnailUrl;
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
}
