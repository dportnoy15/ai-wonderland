package modelimport;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FilterIO {
    private static final String path = "WordFilter.txt";
    public List<String> wordList;

    public FilterIO(){
        readFromFile();
    }

    private void readFromFile() {
        wordList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                wordList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
