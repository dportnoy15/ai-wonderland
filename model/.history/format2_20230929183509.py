import aspose.threed as a3d

scene = a3d.Scene.from_file("Meshy_glb/model.glb")
scene.save("Collada/model.dae")

#need to run "pip install aspose-3d"
#can't be imported into Alice