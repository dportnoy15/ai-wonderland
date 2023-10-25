# texture generation script

import sys, time, requests, torch

API_KEY = "msy_pS6Mne8d4CZawova3JDUIuxrya9mTSR8NK0G"

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

deviceName = "cuda" if torch.cuda.is_available() else "cpu"

print(torch.version.cuda, flush=True)
print(deviceName, flush=True)

modelDescFile = open('model-desc.txt', 'r')
lines = modelDescFile.readlines()
modelDescFile.close()

model_url = lines[0]
model_description = lines[1]

model_url = 'https://assets.meshy.ai/email%7C652989eb2f2bbf73ed6c2901/tasks/018b6464-6406-7470-b7e9-c6770ce41d0f/output/model.glb?Expires=1698456274&Signature=BKsrlaXFnf4we2VDMV5Cso6bxLp7IYxGMk52Qho7TXz7sdlkq6BbzDOWcgr3TIrGFU0I9i-IFlf69IC9sSsia2~~BB4aYlzfY9qOp~p8WmQv5w9S-E~zvi1vfL6xibf-GWy6dflC7VWLHtStpDVN0cltwpBVl~Rw9mWPdHcw5e47Pj73BTGKXaCWcG6q8CkBnnMT~OkX1EETon9V7HbrWI--F-n7LEhUb3OzSiQT-13m-7TanTfI8MTc6xzdUzVRSXhG5VG7cFB8YWsGzwUbsYFHEnhnlK-w3HF6tCicVUOGrUwbVvPICvIspyBQ2J5kwxBkRhYWxrKuQ7xaVtdj7Q__&Key-Pair-Id=KL5I0C8H7HX83'
model_description = 'tactical battle submarine'

texture_style = ' '.join(sys.argv[1:])

#negative_prompt = "low quality, low resolution, ugly"
negative_prompt = "ugly, low quality, melting"

generate_texture_temp(model_url, model_description, texture_style, negative_prompt)

print("Finished entire python script", flush=True)