# ai-wonderland
AI integration with the Alice project

SETUP INSTRUCTIONS

 - Install Java 17 (https://www.oracle.com/java/technologies/downloads/#jdk17-windows)
 - Install Maven (https://maven.apache.org/download.cgi)
 - Install Blender (https://www.blender.org/download/)
   - Make sure Blender is in your path (you should be able to launch blender by typing "blender in a terminal")
      - https://superuser.com/a/689336
 - Install python 3.9 (will not work with later versions) (https://www.python.org/downloads/release/python-3913/)
 - Either use Command Prompt as the terminal or download GIt for Windows (https://gitforwindows.org/)
 - Open a terminal and run the following commands (copy-paste and then press Enter)
   1. pip install torch==1.13.0+cu117 torchvision==0.14.0+cu117 torchaudio==0.13.0 --extra-index-url https://download.pytorch.org/whl/cu117
   2. pip install ipywidgets pyyaml ninja
   [comment]: <> (1. pip install torch==1.13.0+cu117 torchvision==0.14.0+cu117 torchaudio==0.13.0 --extra-index-url https://download.pytorch.org/whl/cu117)
   [comment]: <> (2. pip install ipywidgets pyyaml ninja transformers)
   [comment]: <> (3. Run the following commands to install pytorch3d)
   [comment]: <> (   - git clone https://github.com/facebookresearch/pytorch3d.git)
   [comment]: <> (   - cd pytorch3d)
   [comment]: <> (   - set DISTUTILS_USE_SDK=1)
   [comment]: <> (   - python setup.py install)
   [comment]: <> (4. Run the following commands to install shap-e)
   [comment]: <> (   - git clone https://github.com/openai/shap-e)
   [comment]: <> (   - cd shap-e
   [comment]: <> (   - pip install -e .)
 - Open a terminal, go to this folder, and run "mvn clean javafx:run" to run the program

NOTE: The PyTorch3D installation commands might need to be run in "x86 Native Tools Command Prompt for VS 2019", BUT try using the regular CLI first.
