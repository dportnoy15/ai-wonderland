import bpy, sys

argv = sys.argv

bpy.ops.object.delete(use_global=False)

# this script gets called for Meshy models without any CLI parameters, and assumes the path and filetype
filetype = ".glb"
model_path = "gen-model\model.glb"

# for models uploaded by the user, these values need to be passed in as CLI parameters
if "--" in argv:
    filetype = argv[argv.index("--") + 1]
    model_path = argv[argv.index("--") + 2]

if filetype in {".glb", ".gltf"}:
    print("Importing GLTF...")
    bpy.ops.import_scene.gltf(filepath=model_path) # works for glb and gltf
elif filetype == ".obj":
    print("Importing OBJ...")
    bpy.ops.import_scene.obj(filepath=model_path)
elif filetype == ".stl":
    print("Importing STL...")
    bpy.ops.import_mesh.stl(filepath=model_path)
elif filetype == ".fbx":
    print("Importing FBX...")
    bpy.ops.import_scene.fbx(filepath=model_path)

bpy.ops.wm.collada_export(filepath=r"gen-model\model.dae")
