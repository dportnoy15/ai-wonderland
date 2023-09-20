from collada import *

mesh = Collada('house.dae')

mesh.effects.pop()
mesh.materials.pop()

image = material.CImage("cottage_diffuse","texture_trial.png")
surface = material.Surface("cottage_diffuse-surface", image)
sampler2D = material.Sampler2D("cottage_diffuse-sampler", surface)
map = material.Map(sampler2D, "UVSET0")

effect = material.Effect("cottage-effect",[surface, sampler2D], "phong", diffuse=map, specular=(0,1,0))
mat = material.Material("cottage-material", "cottage-material", effect)

mesh.effects.append(effect)
mesh.materials.append(mat)
mesh.images.append(image)

mesh.write('house_texture.dae')