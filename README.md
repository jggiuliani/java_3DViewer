# java_3DViewer
Lightweight Java window application that allows the user to inspect a single triangulated .obj file.

4x4 float arrays represent 4x4 matrices.
1x4 float arrays represent column vectors with x,y,z,w components.

The Mesh class stores an array of vertices and an array of their respective indices comparable to element and index buffers in OpenGL. Each vertex is a float[][] whose outer array represents vertex attributes. Each inner array is a 1x4 column vector containing the values of its respective attribute. As the program is written, each vertex only contains one float[] because position is the only vertex attribute used. With the Mesh class and current vertex structure, it is possible to expand the functionality of the viewer to include multiple Mesh objects as well as vertex UV coordinates, normals, and texturing. The Mesh class keeps a 3x3 rotation matrix inside a 4x4 float array that is updated every time the Mesh's rotation is changed. The Mesh class also stores origin, scale, and rotation vectors.

The Ops class contains static utility functions to operate mathematically on floats and float arrays while interpreting them as 4x4 matrices and vectors where applicable. It also contains a fastInverSqrt method based off the Quake III algorithm, however I did not test to see if there is a positive improvement in speed when using it in Java. Most functions change the state of the 'a' parameter and do not return a new object so that the developer has better control over memory. Public methods are named so that only those with "get" in their header are not void. The 'a' parameter of every public method that does not have "get" in its header is altered by the method.

During projection, every 3 consecutive indices in a Mesh's index buffer represents a face since it is assumed that the .obj mesh will be triangulated.

The triangle drawing process is somewhat inefficient as it purely uses barycentric weights as a means of detecting whether a pixel is inside the curreent triangle. This means barycentric weights are calculated for each pixel inside the bounding box of each triangle facing the camera. As the previous statement implies, however, the projection and rasterization process is somewhat optimized because backfaces are always culled, i.e. no projection or barycentric calculations are performed for triangles not facing the camera. A depth buffer is used and assigned values by calculating the depth of a given pixel by applying the barycentric weights of the current triangle to the z value of their respective vertex.

The user can drag the mouse to rotate the mesh object along its local z and x axes.
The files include model.obj which is just a cube. The user can replace the .obj file used in Main's main method by changing the path specified when calling m.setMesh("...");

The viewer does not have an FPS cap or any delta time functionality. 

Unlike Blender, Z is forward.

Currently there is no way to translate or rotate the camera.

