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

import sys, torch, trimesh
from diffusers import ShapEPipeline
from diffusers.utils import export_to_ply

print("Generating a 3D model...")

deviceName = "cuda" if torch.cuda.is_available() else "cpu"

print(torch.version.cuda)
print(deviceName)

prompt = ' '.join(sys.argv[1:])

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

print("Finished entire python script")