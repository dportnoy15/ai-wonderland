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
# 4. Go back into the directory containing this file and run it using:
#    - python generate_model_meshy.py {model description}

import sys, time, requests, torch
from py4j.java_gateway import JavaGateway, CallbackServerParameters

gateway = JavaGateway(
    callback_server_parameters=CallbackServerParameters())

def generate_model(api_key, model_description, style_prompt, art_style, negative_prompt):
    print(f"Generating a Meshy.ai model ...", flush=True)

    print(f"API Key: {api_key}", flush=True)
    print(f"Model Description: {model_description}", flush=True)
    print(f"Texture: {style_prompt}", flush=True)
    print(f"Art Style: {art_style}", flush=True)

    headers = {
        'Authorization': f"Bearer {api_key}"
    }

    payload = {
        "object_prompt": model_description,
        "style_prompt": style_prompt,
        "art_style": art_style,
        "negative_prompt": negative_prompt,
        "enable_pbr": False
    }

    response = requests.post(
        "https://api.meshy.ai/v1/text-to-3d",
        headers=headers,
        json=payload,
    )

    print(response.json(), flush=True)
    task_id = response.json()['result']
    print(f"Task ID: {task_id}", flush=True)

    model_created = False

    while model_created == False:
        # pause to give the texture time to generate
        time.sleep(10)

        response = requests.get(
            f"https://api.meshy.ai/v1/text-to-3d/{task_id}",
            headers=headers,
        )

        print(f"{response.json()['status']}: {response.json()['progress']}", flush=True)

        model_created = bool(response.json()['status'] == "SUCCEEDED")

        gateway.entry_point.setProgress(response.json()['status'], response.json()['progress'])

    r = requests.get(response.json()['model_url'], allow_redirects=True)

    gateway.entry_point.setObjectUrl(response.json()['model_url'])
    gateway.entry_point.setThumbnailUrl(response.json()['thumbnail_url'])

    print(f"Thumbnail: {response.json()['thumbnail_url']}", flush=True)

    open('gen-model/model.glb', 'wb').write(r.content)

deviceName = "cuda" if torch.cuda.is_available() else "cpu"

print(torch.version.cuda, flush=True)
print(deviceName, flush=True)

api_key = gateway.entry_point.getApiKey()
model_description = gateway.entry_point.getObjectDescription()
style_prompt = gateway.entry_point.getTextureDescription()
art_style = gateway.entry_point.getArtStyle()

negative_prompt = "ugly, low quality, melting"

# Provide a default values if the user doesn't enter anything for some of the fields

if model_description is None or len(model_description.strip()) == 0:
    model_description = "object"

if style_prompt is None or len(style_prompt.strip()) == 0:
    style_prompt = "realistic"

generate_model(api_key, model_description, style_prompt, art_style, negative_prompt)

gateway.close()

print("Finished entire python script", flush=True)
