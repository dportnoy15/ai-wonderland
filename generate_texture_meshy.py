# texture generation script

import sys, time, requests, torch
from py4j.java_gateway import JavaGateway, CallbackServerParameters

# use this key next: msy_8oC9G40mJu5RlEAdpMMP1fn8xZksOn4CEOKv
API_KEY = "msy_lDwYqkiAL9Ogpl2gIK46OJDrA2EO2G161USP"

gateway = JavaGateway(
    callback_server_parameters=CallbackServerParameters())

# This regenerates the whole model. Temporary workaround until we figure out how to correctly use the text-to-texture api
def generate_texture(model_url, model_description, texture_style, negative_prompt):
    print(f"Generating a Meshy.ai texture ...", flush=True)

    print(f"Model Description: {model_description}", flush=True)
    print(f"Texture Style: {texture_style}", flush=True)

    headers = {
        'Authorization': f"Bearer {API_KEY}"
    }

    payload = {
        "model_url": model_url,
        "object_prompt": model_description,
        "style_prompt": texture_style,
        "enable_original_uv": True,
        "enable_pbr": False,
        "negative_prompt": "low quality, low resolution, low poly, ugly"
    }

    response = requests.post(
        "https://api.meshy.ai/v1/text-to-texture",
        headers=headers,
        json=payload,
    )
    response.raise_for_status()

    print(response.json(), flush=True)
    task_id = response.json()['result']
    print(f"Task ID: {task_id}", flush=True)

    texture_created = False

    while texture_created == False:
        # pause to give the texture time to generate
        time.sleep(10)

        response = requests.get(
            f"https://api.meshy.ai/v1/text-to-texture/{task_id}",
            headers=headers,
        )

        print(f"{response.json()['status']}: {response.json()['progress']}", flush=True)

        texture_created = bool(response.json()['status'] == "SUCCEEDED")

        gateway.entry_point.setProgress(response.json()['status'], response.json()['progress'])

    r = requests.get(response.json()['model_url'], allow_redirects=True)

    open('gen-model/model.glb', 'wb').write(r.content)

deviceName = "cuda" if torch.cuda.is_available() else "cpu"

print(torch.version.cuda, flush=True)
print(deviceName, flush=True)

model_url = gateway.entry_point.getObjectUrl()
model_description = gateway.entry_point.getObjectDescription()

print(model_url, flush=True)
print(model_description, flush=True)

texture_style = ' '.join(sys.argv[1:])

#negative_prompt = "low quality, low resolution, ugly"
negative_prompt = "ugly, low quality, melting"

generate_texture(model_url, model_description, texture_style, negative_prompt)

print("Finished entire python script", flush=True)
