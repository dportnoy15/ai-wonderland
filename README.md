# ai-wonderland
AI integration with the Alice project

SETUP INSTRUCTIONS

 - Install Java 17 (https://www.oracle.com/java/technologies/downloads/#jdk17-windows)
 - Install Maven (https://maven.apache.org/download.cgi)
 - Install python 3.9 (will not work with later versions) (https://www.python.org/downloads/release/python-3913/)
 - Either use Command Prompt as the terminal or download GIt for Windows (https://gitforwindows.org/)
 - Open a terminal and run the following commands
   1. pip install torch==1.13.0+cu117 torchvision==0.14.0+cu117 torchaudio==0.13.0 --extra-index-url https://download.pytorch.org/whl/cu117
   2. pip install ipywidgets pyyaml ninja transformers
   3. Run the following commands to install pytorch3d (I think I had to use "x86 Native Tools Command Prompt for VS 2019" by try the regular command prompt first)
      - git clone https://github.com/facebookresearch/pytorch3d.git
      - cd pytorch3d
      - python setup.py install
   4. Run the following commands to install shap-e
      - git clone https://github.com/openai/shap-e
      - cd shap-e
      - pip install -e .
 - Open a terminal, go to this folder, and run "mvn clean javafx:run" to run the program

