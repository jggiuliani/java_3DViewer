public class Ops
{
    public static void timesEquals(float[] a, float[][] mat)
    {
        float[] input = a.clone();

        for (int k = 0; k < 4; k++)
            a[k] = input[0] * mat[k][0] + input[1]*mat[k][1] + input[2]*mat[k][2] + input[3]*mat[k][3];
    }

    public static void plusEquals(float[] a, float[] b)
    {
        // w unaffected
        a[0] += b[0];
        a[1] += b[1];
        a[2] += b[2];
    }

    public static void minusEquals(float[] a, float[] b)
    {
        // w unaffected
        a[0] -= b[0];
        a[1] -= b[1];
        a[2] -= b[2];
    }

    public static void timesEquals(float[] a, float[] b)
    {
        // w unaffected
        a[0] *= b[0];
        a[1] *= b[1];
        a[2] *= b[2];
    }

    public static float[] getCrossProduct(float[] a, float[] b)
    {
        float[] ret = new float[4];
        ret[0] = a[1] * b[2] - a[2] * b[1];
        ret[1] = a[2] * b[0] - a[0] * b[2];
        ret[2] = a[0] * b[1] - a[1] * b[0];
        ret[3] = 1;
        return ret;
        
    }
    
    public static float[][] getProjectionMatrix(float w, float h, float fov, float n_clip, float f_clip)
    {
        return new float[][]
                  {
                    {1 / ((w/h) * (float)Math.tan(fov/2)), 0, 0, 0},
                    {0, 1 / ((float)Math.tan(fov/2)), 0, 0},
                    {0, 0, -(f_clip+n_clip)/(f_clip-n_clip), -2*f_clip*n_clip / (f_clip - n_clip)},
                    {0, 0, -1, 0}
                  };
    }   

    public static float getDotProduct(float[] a, float[] b)
    {
        // w not used
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    public static void normalize(float[] a)
    {
        // w unaffected
        float d = fastInverseSqrt(a[0]*a[0] + a[1]*a[1] + a[2]*a[2]);
        a[0] *= d;
        a[1] *= d;
        a[2] *= d;
    }

    private static float fastInverseSqrt(float a)
    {
        float xhalf = 0.5f * a;
        int i = Float.floatToIntBits(a);
        i = 0x5f3759df - (i >> 1);
        a = Float.intBitsToFloat(i);
        a *= (1.5f - xhalf * a * a);
        return a;
    }

    public static float[] getBarycentricWeights(float[][] tri, float[] screen_coord)
    {
        float[] ret = new float[4];

        float[] v0 = tri[1].clone(); Ops.minusEquals(v0, tri[0]);
        float[] v1 = tri[2].clone(); Ops.minusEquals(v1, tri[0]);
        float[] v2 = screen_coord.clone(); Ops.minusEquals(v2, tri[0]);
        
        float d00 = Ops.getDotProduct(v0, v0);
        float d01 = Ops.getDotProduct(v0, v1);
        float d11 = Ops.getDotProduct(v1, v1);
        float d20 = Ops.getDotProduct(v2, v0);
        float d21 = Ops.getDotProduct(v2, v1);
        
        float invDenom = 1f / (d00 * d11 - d01 * d01);
        
        // x,y,z correspond to u,v,w respectively

        float v = (d11 * d20 - d01 * d21) * invDenom;
        float w = (d00 * d21 - d01 * d20) * invDenom;

        ret[0] = 1 - v - w;
        ret[1] = v;
        ret[2] = w;

        return ret;
    }
}
