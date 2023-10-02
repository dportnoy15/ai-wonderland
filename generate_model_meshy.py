# 3D model generation script

# Setup instructions:
#
# 1. Install python 3.9 (I don't think this will work with later Python versions)
#
# Open a shell and run the following commands:
#
# 2. pip install torch==1.13.0+cu117 torchvision==0.14.0+cu117 torchaudio==0.13.0 --extra-index-url https://download.pytorch.org/whl/cu117
#
# 3. pip install ipywidgets pyyaml ninja
#
# 4. Run the following commands in "x86 Native Tools Command Prompt for VS 2019" to install pytorch3d
#    - git clone https://github.com/facebookresearch/pytorch3d.git
#    - cd pytorch3d
#    - python setup.py install
#
# 5. Run the following commands to install shap-e
#    - git clone https://github.com/openai/shap-e
#    - cd shap-e
#    - pip install -e .
#
# 6. Go back into the directory containing this file and run it using:
#    - python generate-model.py {model description}

import sys, time, requests, torch

API_KEY = "msy_wDUftZGYBMefP7Aaz2UiBpl5ysVFWGgtu7rN"

def generate_model(model_description, texture_style):
    print("Generating Meshy model;")

    headers = {
        'Authorization': f"Bearer {API_KEY}"
    }

    payload = {
        "object_prompt": model_description,
        "style_prompt": texture_style,
        "enable_pbr": True,
        "art_style": "generic",
        "negative_prompt": "low quality, low resolution, low poly, ugly"
    }

    response = requests.post(
        "https://api.meshy.ai/v1/text-to-3d",
        headers=headers,
        json=payload,
    )

    print(response.json())
    task_id = response.json()['result']
    print(f"Task ID: {task_id}")

    model_created = False

    while model_created == False:
        # pause to give the texture time to generate
        time.sleep(1)

        response = requests.get(
            f"https://api.meshy.ai/v1/text-to-3d/{task_id}",
            headers=headers,
        )

        #print(f"Error: {response.raise_for_status()}")
        print(f"{response.json()['status']}: {response.json()['progress']}")

        model_created = bool(response.json()['status'] == "SUCCEEDED")

    r = requests.get(response.json()['model_url'], allow_redirects=True)
    open('gen-model/model.glb', 'wb').write(r.content)

print("Generating a 3D model...")

deviceName = "cuda" if torch.cuda.is_available() else "cpu"

print(torch.version.cuda)
print(deviceName)

prompt = ' '.join(sys.argv[1:])

model_description = prompt
texture_style = "high-poly cartoony"
#texture_style = "high fantasy, cartoony, magic"
#model_description = "a japanese battle mech"
#texture_style = "red fangs, Samurai outfit that fused with japanese batik style"

generate_model(model_description, texture_style)