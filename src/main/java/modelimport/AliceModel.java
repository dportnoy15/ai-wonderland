package modelimport;

public class AliceModel {
    private String name; // possibly temporary
    private String webUrl;
    private String thumbnailUrl;
    private String localPath; // The Alice import wizard requires a local model, so this is the local path the model is written to for that

    private boolean fromMeshy;

    public static AliceModel createFromMeshy(String name, String webUrl, String thumbnailUrl) {
        return new AliceModel(name, webUrl, thumbnailUrl, null, true);
    }

    public static AliceModel createFromLocalFile(String name, String webUrl) {
        return new AliceModel(name, webUrl, null, null, false);
    }

    private AliceModel(String name, String webUrl, String thumbnailUrl, String localPath, boolean fromMeshy) {
        this.name = name;
        this.webUrl = webUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.localPath = localPath;

        this.fromMeshy = fromMeshy;
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

    public boolean isMeshyModel() {
        return fromMeshy;
    }

    public boolean isLocalModel() {
        return !fromMeshy;
    }
}
