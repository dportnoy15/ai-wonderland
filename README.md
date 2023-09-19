# ai-wonderland
AI integration with the Alice project

SETUP INSTRUCTIONS

 - Install Java 17
 - Install Maven
 - Follow the instructions at the top of the generate-model.py file to properly install the python dependencies
 - [Only do this step once] Open a terminal and run "python generate-model.py test"
   - You could instead just run the java program right away, which will also run generate-model.py
   - However, the first time you try to use the SHAP_E model, it will download about 4 GB of data, which will take a long time
   - Running the python script directly will give you decent progress bars to see how much time is left
   - If this script were run from Java, you would see no progress indicators, and it would look like the program was frozen
 - Open a terminal, go to this folder, and run "mvn clean javafx:run" to run the program

