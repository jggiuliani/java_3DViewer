import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.MouseInputListener;

public class Main extends JPanel implements ComponentListener, MouseInputListener
{
    final static int MAX_VERT_ATTRIBS = 1;
    final Dimension DEFAULT_SIZE = new Dimension(800, 600);

    BufferedImage img = new BufferedImage((int)DEFAULT_SIZE.getWidth(), (int)DEFAULT_SIZE.getHeight(), 1);
    int[] imgData;
    float[] depthBuffer;
    float[][] projMat;
    float fov = (float)Math.PI/2, minClip = 0.1f, maxClip = 1000;

    Graphics context;

    Mesh mesh;

    // Store variables outside render loop to skip allocation on each iteration
    float[][] tri = new float[3][4];
    float[] line1, line2;

    Point lastMousePt;

    public static void main(String[] args)
    {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(3); // EXIT_ON_CLOSE
        frame.setVisible(true);
        Main m = new Main(frame.getGraphics());
        frame.add(m);
        frame.pack();
        m.init();

        /** OBJ ONLY **/
        m.setMesh("model.obj");
        m.mesh.origin[2] = 3;
    }

    public Main(Graphics context)
    {
        this.context = context;
        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        setPreferredSize(DEFAULT_SIZE);
    }

    public void init()
    {
        configureRaster(); // Set image to panel size and adjust data and depth buffer arrays
        paintComponent(context);

        new Thread(()->
        {
            while(true)
            {
                repaint();
            }
            
        }).start();
    }

    public void configureRaster()
    {
        img = new BufferedImage(Math.max(1, getWidth()), Math.max(1, getHeight()), 1); // 1 = TYPE_INT_RGB
        img.setAccelerationPriority(1);

        imgData = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
        depthBuffer = new float[imgData.length];
        projMat = Ops.getProjectionMatrix(getWidth(), getHeight(), fov, minClip, maxClip);
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        setBackground(Color.BLACK);

        int length = imgData.length;
        for (int i = 0; i < length; i++)
        {
            imgData[i] = 0; // Reset the image to black
            depthBuffer[i] = -Float.MAX_VALUE; // reset the depth buffer
        }

        // Store on the stack for faster computation
        int width = getWidth();
        int height = getHeight();

        int min_x, min_y, max_x, max_y;

        if (mesh != null)
        {
            int[] inds = mesh.indices;
            int num_tris = inds.length;

            for (int i = 0; i < num_tris; i+=3) // tri in verts
            {
                tri[0] = mesh.vertices[ mesh.indices[i]   ][0].clone();
                tri[1] = mesh.vertices[ mesh.indices[i+1] ][0].clone();
                tri[2] = mesh.vertices[ mesh.indices[i+2] ][0].clone();

                for (float[] v : tri) // v in tri
                {
                    Ops.timesEquals(v, mesh.scale);
                    Ops.timesEquals(v, mesh.rotMat);
                    Ops.plusEquals(v, mesh.origin);
                }

                float[] normal = new float[4];
                line1  = tri[1].clone();
                line2 = tri[2].clone();

                Ops.minusEquals(line1, tri[0]);
                Ops.minusEquals(line2, tri[0]);
                normal = Ops.getCrossProduct(line1, line2);
                Ops.normalize(normal);

                float[] cameraRay = tri[0].clone();
                Ops.minusEquals(cameraRay, new float[]{0,0,0,1});

                // If the triangle is facing the camera
                if(Ops.getDotProduct(normal, cameraRay) < 0)
                {
                    for (float[] v : tri)
                    {
                        Ops.timesEquals(v, projMat); // Project
                        float id = 1/v[3];

                        // Perspective divide and convert to screen space
                        v[0] = (v[0]*id + 1) * width*0.5f;
                        v[1] = (v[1]*id + 1) * height*0.5f;
                    }

                    // Set bounding box for triangle
                    min_x = (int) Math.min(width, Math.max(0, Math.min(tri[0][0], Math.min(tri[1][0], tri[2][0]))));
                    max_x = (int) Math.min(width, Math.max(0, Math.max(tri[0][0], Math.max(tri[1][0], tri[2][0]))));
                    min_y = (int) Math.min(height, Math.max(0, Math.min(tri[0][1], Math.min(tri[1][1], tri[2][1]))));
                    max_y = (int) Math.min(height, Math.max(0, Math.max(tri[0][1], Math.max(tri[1][1], tri[2][1]))));

                    for (int x = min_x; x < max_x; x++)
                        for (int y = min_y; y < max_y; y++)
                        {
                            int ind = x + y * width;
                            float[] weight = Ops.getBarycentricWeights(tri, new float[]{x,y,0,1}); // weight now has barycentric weights
                            
                            // if the pixel is inside the triangle
                            if (weight[0] >= 0 && weight[1] >= 0 && weight[2] >= 0)
                            {
                                // depth value of current pixel obtained through applying barycentric weights to tri's vertices
                                float depth = weight[0]*tri[0][2] + weight[1]*tri[1][2] + weight[2]*tri[2][2];
                                
                                // if its closer to the camera than anything previously drawn at the given pixel
                                if (depth > depthBuffer[ind])
                                {
                                    depthBuffer[ind] = depth;

                                    imgData[ind] = new Color((int)(weight[0]*255),(int)(weight[1]*255),(int)(weight[2]*255)).getRGB();
                                }
                            }
                        }   
                }
        }
    }

        g.drawImage(img,0,0,null);
        
    }

    public void setMesh(String path)
    {
        mesh = new Mesh(path);
    }

    public void componentResized(ComponentEvent e)
    {
        configureRaster();
    }

    public void mousePressed(MouseEvent e)
    { 
        lastMousePt = e.getPoint();
    }

    public void mouseReleased(MouseEvent e)
    {
        lastMousePt = e.getPoint();
    }

    public void mouseDragged(MouseEvent e)
    {
        mesh.rotate(0, (float)((e.getX()-lastMousePt.x)/(Math.PI*24)),
                    (float)(-(e.getY()-lastMousePt.y)/(Math.PI*24)));

        lastMousePt = e.getPoint();
    }

    public void componentMoved(ComponentEvent e){}

    public void componentShown(ComponentEvent e){}

    public void componentHidden(ComponentEvent e){}

    public void mouseClicked(MouseEvent e){}

    public void mouseEntered(MouseEvent e){}

    public void mouseExited(MouseEvent e){}

    public void mouseMoved(MouseEvent e){}

}
