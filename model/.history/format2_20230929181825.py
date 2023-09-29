import aspose.threed as a3d

scene = a3d.Scene.from_file("Meshy_glb/model.glb")
scene.save("Collada/model.dae")