from collada import *
from collada import source

mesh = Collada()

#effect = material.Effect("effect0", [], "phong", diffuse=(1,0,0), specular=(0,1,0))
#mat = material.Material("material0", "mymaterial", effect)
image = material.CImage("cottage_diffuse","texture_trial.png")
surface = material.Surface("cottage_diffuse-surface", image)
sampler2D = material.Sampler2D("cottage_diffuse-sampler", surface)
map = material.Map(sampler2D, "UVSET0")

effect = material.Effect("cottage-effect",[surface, sampler2D], "phong", diffuse=map, specular=(0,1,0))
mat = material.Material("cottage-material", "cottage-material", effect)

mesh.effects.append(effect)
mesh.materials.append(mat)
mesh.images.append(image)

import numpy
# put every floats of vertex in this vert_float (mesh positions)
vert_floats = [-1,1,1,1,1,1,-1,-1,1,1,
              -1,1,-1,1,-1,1,1,-1,-1,-1,-1,1,-1,-1]
# put every floats of normals in this normal_float (mesh normals)
normal_floats = [0,0,1, 0,1,0, 0,-1,0, -1,0,0, 1,0,0, 0,0,-1]
vert_src = source.FloatSource("cubeverts-array", numpy.array(vert_floats), ('X', 'Y', 'Z'))
normal_src = source.FloatSource("cubenormals-array", numpy.array(normal_floats), ('X', 'Y', 'Z'))

geom = geometry.Geometry(mesh, "geometry0", "mycube", [vert_src, normal_src])

input_list = source.InputList()
input_list.addInput(0, 'VERTEX', "#cubeverts-array")
input_list.addInput(1, 'NORMAL', "#cubenormals-array")

indices = numpy.array([0,0,2,0,3,0, 0,0,3,0,1,0, 0,1,1,1,5,1,
                       0,1,5,1,4,1, 6,2,7,2,3,2, 6,2,3,2,2,2,
                       0,3,4,3,6,3, 0,3,6,3,2,3, 3,4,7,4,5,4,
                       3,4,5,4,1,4, 5,5,7,5,6,5, 5,5,6,5,4,5])

triset = geom.createTriangleSet(indices, input_list, "cottage-material")
geom.primitives.append(triset)
mesh.geometries.append(geom)

matnode = scene.MaterialNode("cottage-material", mat, inputs=[])
geomnode = scene.GeometryNode(geom, [matnode])
node = scene.Node("node0", children=[geomnode])

myscene = scene.Scene("myscene", [node])
mesh.scenes.append(myscene)
mesh.scene = myscene

mesh.write('test2.dae')