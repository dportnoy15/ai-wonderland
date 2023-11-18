# texture generation script

import sys, time, requests, torch
from py4j.java_gateway import JavaGateway, CallbackServerParameters

gateway = JavaGateway(
    callback_server_parameters=CallbackServerParameters())

# This regenerates the whole model. Temporary workaround until we figure out how to correctly use the text-to-texture api
def generate_texture(api_key, model_url, model_description, style_prompt, art_style, negative_prompt):
    print(f"Generating a Meshy.ai texture ...", flush=True)

    print(f"Model URL: {model_url}", flush=True)
    print(f"Model Description: {model_description}", flush=True)
    print(f"Texture Style: {style_prompt}", flush=True)

    headers = {
        'Authorization': f"Bearer {api_key}"
    }

    payload = {
        "model_url": model_url,
        "object_prompt": model_description,
        "style_prompt": style_prompt,
        "art_style": art_style,
        "negative_prompt": negative_prompt,
        "enable_pbr": False,
        "enable_original_uv": True
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

    print(f"Thumbnail: {response.json()['thumbnail_url']}", flush=True)

    open('gen-model/model.glb', 'wb').write(r.content)

deviceName = "cuda" if torch.cuda.is_available() else "cpu"

print(torch.version.cuda, flush=True)
print(deviceName, flush=True)

api_key = gateway.entry_point.getApiKey()
model_url = gateway.entry_point.getObjectUrl()
model_description = gateway.entry_point.getObjectDescription()
style_prompt = gateway.entry_point.getTextureDescription()
art_style = gateway.entry_point.getArtStyle()

negative_prompt = "ugly, low quality, melting"

generate_texture(api_key, model_url, model_description, style_prompt, art_style, negative_prompt)

gateway.close()

print("Finished entire python script", flush=True)
