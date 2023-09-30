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

import sys, torch, trimesh, requests, json, time
from diffusers import ShapEPipeline
from diffusers.utils import export_to_ply

API_KEY = "msy_wDUftZGYBMefP7Aaz2UiBpl5ysVFWGgtu7rN"
TEXTURE_STYLE = "high fantasy, cartoony, magic"

def generate_model(model_description, texture_style):
    print("Generating Meshy model;")

    headers = {
        'Authorization': f"Bearer {API_KEY}"
    }

    payload = {
        "object_prompt": "a japanese mech with lasers",
        "style_prompt": "red fangs, Samurai outfit that fused with japanese batik style",
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

def generate_texture(model_description, texture_style):
    print("Generating texture for mode;")

    headers = {
        'Authorization': f"Bearer {API_KEY}"
    }

    payload = {
        "model_url": "https://cdn.meshy.ai/model/example_model_2.glb",
        "object_prompt": "a clown mask",
        "style_prompt": "puffy purple fangs, Samurai outfit that fused with japanese batik style",
        "enable_original_uv": True,
        "enable_pbr": True,
        "negative_prompt": "low quality, low resolution, low poly, ugly"
    }

    response = requests.post(
        "https://api.meshy.ai/v1/text-to-texture",
        headers=headers,
        json=payload,
    )

    task_id = response.json()['result']
    #task_id = '018ae282-9c69-71e1-a599-352217923ae5'

    #print(f"Stats Code: {response.status_code}")
    print(f"Task ID: {task_id}")

    texture_created=False

    while texture_created == False:
        # pause to give the texture time to generate
        time.sleep(1)

        response = requests.get(
            f"https://api.meshy.ai/v1/text-to-texture/{task_id}",
            headers=headers,
        )

        #print(f"Error: {response.raise_for_status()}")
        print(f"{response.json()['status']}: {response.json()['progress']}")

        texture_created = bool(response.json()['status'] == "SUCCEEDED")

    r = requests.get(response.json()['model_url'], allow_redirects=True)
    open('gen-model/model.glb', 'wb').write(r.content)

    # no need to download textures since they are already part of the GLB file
    '''
    r = requests.get(response.json()['texture_urls'][0]['base_color'], allow_redirects=True)
    open('gen-model/texture_0.png', 'wb').write(r.content)

    r = requests.get(response.json()['texture_urls'][0]['metallic'], allow_redirects=True)
    open('gen-model/texture_0_metallic.png', 'wb').write(r.content)

    r = requests.get(response.json()['texture_urls'][0]['normal'], allow_redirects=True)
    open('gen-model/texture_0_normal.png', 'wb').write(r.content)

    r = requests.get(response.json()['texture_urls'][0]['roughness'], allow_redirects=True)
    open('gen-model/texture_0_roughness.png', 'wb').write(r.content)
    '''

    print("DOWNLOADED THE TEXTURED MODEL")

print("Generating a 3D model...")

deviceName = "cuda" if torch.cuda.is_available() else "cpu"

print(torch.version.cuda)
print(deviceName)

prompt = ' '.join(sys.argv[1:])

'''
device = torch.device(deviceName)

pipe = ShapEPipeline.from_pretrained("openai/shap-e", torch_dtype=torch.float16, variant="fp16")
pipe = pipe.to(device)

guidance_scale = 15.0

images = pipe(
    prompt,
    guidance_scale=guidance_scale,
    num_inference_steps=64,
    frame_size=256,
    output_type="mesh"
).images

model_name = "3d_model"
model_path = export_to_ply(images[0], model_name + ".ply")

print(f"saved file: {model_path}/{model_name}.ply")

mesh = trimesh.load(model_name + ".ply")
mesh.export(model_name + ".glb", file_type="glb")
'''

print("Finished entire python script")

generate_model(prompt, TEXTURE_STYLE)