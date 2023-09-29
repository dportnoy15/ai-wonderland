import bpy

bpy.ops.object.delete(use_global=False)

bpy.ops.import_scene.gltf(filepath=r"C:\Users\yunqili\Documents\GitHub\ai-wonderland\model\Meshy_glb\model.glb")

bpy.ops.wm.collada_export(filepath=r"C:\Users\yunqili\Documents\GitHub\ai-wonderland\model\Collada\model.dae")
