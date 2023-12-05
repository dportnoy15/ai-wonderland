set JAVA_HOME=C:\Program Files\Java\jdk-17
set AI_WONDERLAND_HOME=C:\Users\dportnoy\Dev\ai-wonderland

echo %JAVA_HOME%

cd %AI_WONDERLAND_HOME%

call mvn clean javafx:run >> %AI_WONDERLAND_HOME%\alice-integration\batLog.txt 2>&1
