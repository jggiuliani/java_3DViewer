import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;

public class Mesh
{
    float[] origin = new float[4];
    {
        origin[3] = 1;
    }
    float[] rotation = new float[4];
    float[][] rotMat = new float[4][4]; 
    {   
        // init to identity so that theta = 0 on all axes
        for (int k = 0; k < 4; k++)
            rotMat[k][k] = 1; 
    }
    float[] scale = {1,1,1,1};

    float[][][] vertices;
    int[] indices;

    public Mesh(String path)
    {
        ArrayList<float[][]> verts = new ArrayList<float[][]>();
        ArrayList<Integer> inds = new ArrayList<Integer>();

        try(BufferedReader br = new BufferedReader(new FileReader(path)))
        {
            for(String line; (line = br.readLine()) != null;)
            {
                if (line.length() > 0)
                {
                    float[][] arr = new float[Main.MAX_VERT_ATTRIBS][4];
                    arr[0][3] = 1;
    
                    if(line.charAt(0) == 'v')
                    {
                        String[] vals = line.split(" ");
                        arr[0][0] = Float.parseFloat(vals[1]);
                        arr[0][1] = Float.parseFloat(vals[2]);
                        arr[0][2] = Float.parseFloat(vals[3]);
                        verts.add(arr);
                    }
    
                    else if(line.charAt(0) == 'f')
                    {
                        String[] vals = line.split(" ");
                        for (int k = 1; k < 4; k++)
                            inds.add(Integer.parseInt(vals[k])-1);
                        
                    }
                }
            }
        }

        catch (Exception e)
        {
            e.printStackTrace();
        };

        vertices = new float[verts.size()][Main.MAX_VERT_ATTRIBS][4];
        vertices = verts.toArray(vertices);
        
        indices = new int[inds.size()];
        for (int i = 0; i < indices.length; i++)
        {
            indices[i] = inds.get(i).intValue();
        }
    }

    public void rotate(float x, float y, float z)
    {
        rotation[0] += x;
        rotation[1] += y;
        rotation[2] += z;

        float sina = (float) Math.sin(rotation[0]);
        float sinb = (float) Math.sin(rotation[1]);
        float sing = (float) Math.sin(rotation[2]);
        float cosa = (float) Math.cos(rotation[0]);
        float cosb = (float) Math.cos(rotation[1]);
        float cosg = (float) Math.cos(rotation[2]);

        rotMat = new float[][]
                  {{cosa * cosb, cosa * sinb * sing - sina * cosg, cosa * sinb * cosg + sina * sing, 0},
                   {sina * cosb, sina * sinb * sing + cosa * cosg, sina * sinb * cosg - cosa * sing, 0},
                   {-sinb, cosb * sing, cosb * cosg, 0},
                   {0, 0, 0, 1}};

        
    }



}