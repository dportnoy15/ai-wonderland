import bpy

bpy.ops.object.delete(use_global=False)

bpy.ops.import_scene.gltf(filepath=r"gen-model\model.glb")

bpy.ops.wm.collada_export(filepath=r"gen-model\model.dae")
