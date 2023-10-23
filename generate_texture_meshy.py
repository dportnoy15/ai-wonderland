# texture generation script

import sys, time, requests, torch

API_KEY = "msy_uuAfEaHJKIBEMNx8K0auCHz0ObeZVMdQXYaN"

def generate_texture(model_description, texture_style, negative_prompt):
    print(f"Generating a Meshy.ai texture ...", flush=True)

    '''
    headers = {
        'Authorization': f"Bearer {API_KEY}"
    }

    # TODO: Try different art styles
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

    print(response.json(), flush=True)
    task_id = response.json()['result']
    print(f"Task ID: {task_id}", flush=True)

    model_created = False

    while model_created == False:
        # pause to give the texture time to generate
        time.sleep(1)

        response = requests.get(
            f"https://api.meshy.ai/v1/text-to-3d/{task_id}",
            headers=headers,
        )

        print(f"{response.json()['status']}: {response.json()['progress']}", flush=True)

        model_created = bool(response.json()['status'] == "SUCCEEDED")
    '''

deviceName = "cuda" if torch.cuda.is_available() else "cpu"

print(torch.version.cuda, flush=True)
print(deviceName, flush=True)

#model_description = "a japanese battle mech"
model_description = ' '.join(sys.argv[1:])

#texture_style = "high-poly cartoony"
#texture_style = "high fantasy, cartoony, magic"
#texture_style = "medieval small house, ancient, best quality, 4k, trending on artstation"
#texture_style = "fantasy, cartoony, game assets"
#texture_style = "red fangs, Samurai outfit that fused with japanese batik style"
texture_style = "modern, clean, metallic"

#negative_prompt = "low quality, low resolution, ugly"
negative_prompt = "ugly, low quality, melting"

generate_texture(model_description, texture_style, negative_prompt)

print("Finished entire python script", flush=True)