# texture generation script

import sys, time, requests, torch

API_KEY = "msy_XwyMLovQW6UIw75vK21B38hhyujKvIzE6Gtq"

def generate_texture(model_url, model_description, texture_style, negative_prompt):
    print(f"Generating a Meshy.ai texture ...", flush=True)

    print(f"Model Description: {model_description}", flush=True)
    print(f"Texture Style: {texture_style}", flush=True)

    headers = {
        'Authorization': f"Bearer {API_KEY}"
    }

    payload = {
        "object_prompt": model_description,
        "style_prompt": texture_style,
        "art_style": "generic",
        "negative_prompt": negative_prompt
    }

    response = requests.post(
        "https://api.meshy.ai/v1/text-to-3d",
        headers=headers,
        json=payload,
    )

    #response.raise_for_status()

    print(response.json(), flush=True)
    task_id = response.json()['result']
    print(f"Task ID: {task_id}", flush=True)

    texture_created = False

    while texture_created == False:
        # pause to give the texture time to generate
        time.sleep(10)

        response = requests.get(
            f"https://api.meshy.ai/v1/text-to-3d/{task_id}",
            headers=headers,
        )

        print(f"{response.json()['status']}: {response.json()['progress']}", flush=True)

        texture_created = bool(response.json()['status'] == "SUCCEEDED")

    r = requests.get(response.json()['model_url'], allow_redirects=True)

    open('gen-model/model.glb', 'wb').write(r.content)

# This regenerates the whole model. Temporary workaround until we figure out how to correctly use the text-to-texture api
def generate_texture_temp(model_url, model_description, texture_style, negative_prompt):
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

    r = requests.get(response.json()['model_url'], allow_redirects=True)

    open('gen-model/model.glb', 'wb').write(r.content)

deviceName = "cuda" if torch.cuda.is_available() else "cpu"

print(torch.version.cuda, flush=True)
print(deviceName, flush=True)

modelDescFile = open('model-desc.txt', 'r')
lines = modelDescFile.readlines()
modelDescFile.close()

model_url = lines[0]
model_description = lines[1]

model_url = model_url[:-1]
model_description = model_description[:-1]

print(model_url, flush=True)
print(model_description, flush=True)

#model_url = 'https://assets.meshy.ai/email%7C6538765000df208a7ec2f3fc/tasks/018b7247-e810-7ce5-b8ee-e2e87b5e75d2/output/model.glb?Expires=1698688697&Signature=dSPOYzJN89WlIw9u483wWkNuANSethanQGlL~PMT6z8-m2zScHU9QAQpRaSL-04I1lbCGAHoWAgLUt2xvQdmz6R6bFwvLWISj4h9HLntBLNUqYaB~JWE~xxqbB2mBZ4h103DAo9KyzfLAzmE6lJwkZXUJnzPx1aMnBegCer8Uq~XrsQi72B5eM1nHp-AhLOBrIKPeaRTYHoDa7rds4o03cbCBBXDyVy0RNtUDuCVMBucFNOPl-tmRkHo-Y~hmw6PNq5jMVxACni5djxO-T5wBVO0HqT7OO9UviR~0fzn6plVOXqHCvJ5~Cz9QuZR~blWK~Ru74RVL~qHbJzAbjKkPA__&Key-Pair-Id=KL5I0C8H7HX83'
#model_description = 'a kettle'

texture_style = ' '.join(sys.argv[1:])

#negative_prompt = "low quality, low resolution, ugly"
negative_prompt = "ugly, low quality, melting"

generate_texture_temp(model_url, model_description, texture_style, negative_prompt)

print("Finished entire python script", flush=True)