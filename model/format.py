import bpy, sys

argv = sys.argv

bpy.ops.object.delete(use_global=False)

model_path = "gen-model\model.glb"

if "--" in argv:
    model_path = argv[argv.index("--") + 1]

bpy.ops.import_scene.gltf(filepath=model_path)

bpy.ops.wm.collada_export(filepath=r"gen-model\model.dae")
