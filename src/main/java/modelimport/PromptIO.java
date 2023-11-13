package modelimport;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PromptIO {

    // Retrieve this list from the class to read the suggested prompt data
    public List<ObjectData> objectDataList;

    public PromptIO(){
        objectDataList = loadObjectData("Description.json");
    }

    public List<ObjectData> loadObjectData(String filePath) {
        List<ObjectData> objectDataList = new ArrayList<>();

        try {
            String jsonString = Files.readString(Path.of(filePath));
            JSONObject jsonObject = new JSONObject(jsonString);

            JSONArray descriptionPairArray = jsonObject.getJSONArray("DescriptionPair");

            for (int i = 0; i < descriptionPairArray.length(); i++) {
                JSONObject element = descriptionPairArray.getJSONObject(i);
                String obj = element.getString("obj");
                String style = element.getString("style");
                String negative = element.optString("negative", ""); // Handle optional field
                objectDataList.add(new ObjectData(obj, style, negative));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return objectDataList;
    }

    public class ObjectData {
        private final String obj;
        private final String style;
        private final String negative;

        public ObjectData(String obj, String style, String negative) {
            this.obj = obj;
            this.style = style;
            this.negative = negative;
        }

        public String getObjectDescription(){
            return obj;
        }

        public String getTextureDescription(){
            return style;
        }

        public String getNegativePrompt(){
            return negative;
        }
    }
}
