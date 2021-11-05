package com.astromedicomp.winvm_gles_3d_texture;

import android.content.Context; // for drawing context related
import android.opengl.GLSurfaceView; // for OpenGL Surface View and all related
import javax.microedition.khronos.opengles.GL10; // for OpenGLES 1.0 needed as param type GL10
import javax.microedition.khronos.egl.EGLConfig; // for EGLConfig needed as param type EGLConfig
import android.opengl.GLES32; // for OpenGLES 3.2
import android.view.MotionEvent; // for "MotionEvent"
import android.view.GestureDetector; // for GestureDetector
import android.view.GestureDetector.OnGestureListener; // OnGestureListener
import android.view.GestureDetector.OnDoubleTapListener; // for OnDoubleTapListener

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.Matrix; // for Matrix math

import android.graphics.BitmapFactory; // texture factory
import android.graphics.Bitmap; // for PNG image
import android.opengl.GLUtils; // for texImage2D()

// A view for OpenGLES3 graphics which also receives touch events
public class GLESView extends GLSurfaceView implements GLSurfaceView.Renderer, OnGestureListener, OnDoubleTapListener
{
    private final Context context;
    
    private GestureDetector gestureDetector;
    
    private int vertexShaderObject;
    private int fragmentShaderObject;
    private int shaderProgramObject;
    
    private int[] vao_pyramid = new int[1];
    private int[] vbo_pyramid_position = new int[1];
    private int[] vbo_pyramid_texture = new int[1];
    
    private int[] vao_cube = new int[1];
    private int[] vbo_cube_position = new int[1];
    private int[] vbo_cube_texture = new int[1];
    
    private int mvpUniform;
    private int texture0_sampler_uniform;

    private int[] texture_kundali = new int[1];
    private int[] texture_stone = new int[1];
    
    private float anglePyramid = 0.0f;
    private float angleCube= 0.0f;
    
    private float perspectiveProjectionMatrix[]=new float[16]; // 4x4 matrix
    
    public GLESView(Context drawingContext)
    {
        super(drawingContext);
        
        context=drawingContext;

        // accordingly set EGLContext to current supported version of OpenGL-ES
        setEGLContextClientVersion(3);

        // set Renderer for drawing on the GLSurfaceView
        setRenderer(this);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        
        gestureDetector = new GestureDetector(context, this, null, false);
        gestureDetector.setOnDoubleTapListener(this);
    }
    
    // overriden method of GLSurfaceView.Renderer ( Init Code )
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        // get OpenGL-ES version
        String glesVersion = gl.glGetString(GL10.GL_VERSION);
        System.out.println("VDG: OpenGL-ES Version = "+glesVersion);
        // get GLSL version
        String glslVersion=gl.glGetString(GLES32.GL_SHADING_LANGUAGE_VERSION);
        System.out.println("VDG: GLSL Version = "+glslVersion);

        initialize(gl);
    }
 
    // overriden method of GLSurfaceView.Renderer ( Chnge Size Code )
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height)
    {
        resize(width, height);
    }

    // overriden method of GLSurfaceView.Renderer ( Rendering Code )
    @Override
    public void onDrawFrame(GL10 unused)
    {
        display();
    }
    
    // Handling 'onTouchEvent' Is The Most IMPORTANT,
    // Because It Triggers All Gesture And Tap Events
    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        // code
        int eventaction = e.getAction();
        if(!gestureDetector.onTouchEvent(e))
            super.onTouchEvent(e);
        return(true);
    }
    
    // abstract method from OnDoubleTapListener so must be implemented
    @Override
    public boolean onDoubleTap(MotionEvent e)
    {
        return(true);
    }
    
    // abstract method from OnDoubleTapListener so must be implemented
    @Override
    public boolean onDoubleTapEvent(MotionEvent e)
    {
        // Do Not Write Any code Here Because Already Written 'onDoubleTap'
        return(true);
    }
    
    // abstract method from OnDoubleTapListener so must be implemented
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e)
    {
        return(true);
    }
    
    // abstract method from OnGestureListener so must be implemented
    @Override
    public boolean onDown(MotionEvent e)
    {
        // Do Not Write Any code Here Because Already Written 'onSingleTapConfirmed'
        return(true);
    }
    
    // abstract method from OnGestureListener so must be implemented
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        return(true);
    }
    
    // abstract method from OnGestureListener so must be implemented
    @Override
    public void onLongPress(MotionEvent e)
    {
    }
    
    // abstract method from OnGestureListener so must be implemented
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
        uninitialize();
        System.exit(0);
        return(true);
    }
    
    // abstract method from OnGestureListener so must be implemented
    @Override
    public void onShowPress(MotionEvent e)
    {
    }
    
    // abstract method from OnGestureListener so must be implemented
    @Override
    public boolean onSingleTapUp(MotionEvent e)
    {
        return(true);
    }

    private void initialize(GL10 gl)
    {
        // ***********************************************
        // Vertex Shader
        // ***********************************************
        // create shader
        vertexShaderObject=GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);
        
        // vertex shader source code
        final String vertexShaderSourceCode =String.format
        (
         "#version 320 es"+
         "\n"+
         "in vec4 vPosition;"+
         "in vec2 vTexture0_Coord;"+
         "out vec2 out_texture0_coord;"+
         "uniform mat4 u_mvp_matrix;"+
         "void main(void)"+
         "{"+
         "gl_Position = u_mvp_matrix * vPosition;"+
         "out_texture0_coord = vTexture0_Coord;"+
         "}"
        );
        
        // provide source code to shader
        GLES32.glShaderSource(vertexShaderObject,vertexShaderSourceCode);
        
        // compile shader & check for errors
        GLES32.glCompileShader(vertexShaderObject);
        int[] iShaderCompiledStatus = new int[1];
        int[] iInfoLogLength = new int[1];
        String szInfoLog=null;
        GLES32.glGetShaderiv(vertexShaderObject, GLES32.GL_COMPILE_STATUS, iShaderCompiledStatus, 0);
        if (iShaderCompiledStatus[0] == GLES32.GL_FALSE)
        {
            GLES32.glGetShaderiv(vertexShaderObject, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
            if (iInfoLogLength[0] > 0)
            {
                szInfoLog = GLES32.glGetShaderInfoLog(vertexShaderObject);
                System.out.println("VDG: Vertex Shader Compilation Log = "+szInfoLog);
                uninitialize();
                System.exit(0);
           }
        }

        // ***********************************************
        // Fragment Shader
        // ***********************************************
        // create shader
        fragmentShaderObject=GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER);
        
        // fragment shader source code
        final String fragmentShaderSourceCode =String.format
        (
         "#version 320 es"+
         "\n"+
         "precision highp float;"+
         "in vec2 out_texture0_coord;"+
         "uniform highp sampler2D u_texture0_sampler;"+
         "out vec4 FragColor;"+
         "void main(void)"+
         "{"+
         "FragColor = texture(u_texture0_sampler, out_texture0_coord);"+
         "}"
        );
        
        // provide source code to shader
        GLES32.glShaderSource(fragmentShaderObject,fragmentShaderSourceCode);
        
        // compile shader and check for errors
        GLES32.glCompileShader(fragmentShaderObject);
        iShaderCompiledStatus[0] = 0; // re-initialize
        iInfoLogLength[0] = 0; // re-initialize
        szInfoLog=null; // re-initialize
        GLES32.glGetShaderiv(fragmentShaderObject, GLES32.GL_COMPILE_STATUS, iShaderCompiledStatus, 0);
        if (iShaderCompiledStatus[0] == GLES32.GL_FALSE)
        {
            GLES32.glGetShaderiv(fragmentShaderObject, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
            if (iInfoLogLength[0] > 0)
            {
                szInfoLog = GLES32.glGetShaderInfoLog(fragmentShaderObject);
                System.out.println("VDG: Fragment Shader Compilation Log = "+szInfoLog);
                uninitialize();
                System.exit(0);
            }
        }
        
        // create shader program
        shaderProgramObject=GLES32.glCreateProgram();
        
        // attach vertex shader to shader program
        GLES32.glAttachShader(shaderProgramObject,vertexShaderObject);
        
        // attach fragment shader to shader program
        GLES32.glAttachShader(shaderProgramObject,fragmentShaderObject);
        
        // pre-link binding of shader program object with vertex shader attributes
        GLES32.glBindAttribLocation(shaderProgramObject,GLESMacros.VDG_ATTRIBUTE_VERTEX,"vPosition");
        GLES32.glBindAttribLocation(shaderProgramObject,GLESMacros.VDG_ATTRIBUTE_TEXTURE0,"vTexture0_Coord");

        // link the two shaders together to shader program object
        GLES32.glLinkProgram(shaderProgramObject);
        int[] iShaderProgramLinkStatus = new int[1];
        iInfoLogLength[0] = 0; // re-initialize
        szInfoLog=null; // re-initialize
        GLES32.glGetProgramiv(shaderProgramObject, GLES32.GL_LINK_STATUS, iShaderProgramLinkStatus, 0);
        if (iShaderProgramLinkStatus[0] == GLES32.GL_FALSE)
        {
            GLES32.glGetProgramiv(shaderProgramObject, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
            if (iInfoLogLength[0] > 0)
            {
                szInfoLog = GLES32.glGetProgramInfoLog(shaderProgramObject);
                System.out.println("VDG: Shader Program Link Log = "+szInfoLog);
                uninitialize();
                System.exit(0);
            }
        }

        // get MVP uniform location
        mvpUniform=GLES32.glGetUniformLocation(shaderProgramObject,"u_mvp_matrix");
        
        // get texture sampler uniform location
        texture0_sampler_uniform=GLES32.glGetUniformLocation(shaderProgramObject,"u_texture0_sampler");
        
        // load textures
        texture_stone[0]=loadGLTexture(R.raw.stone);
        texture_kundali[0]=loadGLTexture(R.raw.vijay_kundali_horz_inverted);

        // *** vertices, colors, shader attribs, vbo, vao initializations ***
        // pyramid vertices
        float pyramidVertices[]= new float[]
        {
            0, 1, 0,    // front-top
            -1, -1, 1,  // front-left
            1, -1, 1,   // front-right
            
            0, 1, 0,    // right-top
            1, -1, 1,   // right-left
            1, -1, -1,  // right-right
            
            0, 1, 0,    // back-top
            1, -1, -1,  // back-left
            -1, -1, -1, // back-right
            
            0, 1, 0,    // left-top
            -1, -1, -1, // left-left
            -1, -1, 1   // left-right
        };
        
        float pyramidTexcoords[]= new float[]
        {
            0.5f, 1.0f, // front-top
            0.0f, 0.0f, // front-left
            1.0f, 0.0f, // front-right
            
            0.5f, 1.0f, // right-top
            1.0f, 0.0f, // right-left
            0.0f, 0.0f, // right-right
            
            0.5f, 1.0f, // back-top
            1.0f, 0.0f, // back-left
            0.0f, 0.0f, // back-right
            
            0.5f, 1.0f, // left-top
            0.0f, 0.0f, // left-left
            1.0f, 0.0f, // left-right
        };
        
        float cubeVertices[]= new float[]
        {
            // top surface
            1.0f, 1.0f,-1.0f,  // top-right of top
            -1.0f, 1.0f,-1.0f, // top-left of top
            -1.0f, 1.0f, 1.0f, // bottom-left of top
            1.0f, 1.0f, 1.0f,  // bottom-right of top
            
            // bottom surface
            1.0f,-1.0f, 1.0f,  // top-right of bottom
            -1.0f,-1.0f, 1.0f, // top-left of bottom
            -1.0f,-1.0f,-1.0f, // bottom-left of bottom
            1.0f,-1.0f,-1.0f,  // bottom-right of bottom
            
            // front surface
            1.0f, 1.0f, 1.0f,  // top-right of front
            -1.0f, 1.0f, 1.0f, // top-left of front
            -1.0f,-1.0f, 1.0f, // bottom-left of front
            1.0f,-1.0f, 1.0f,  // bottom-right of front
            
            // back surface
            1.0f,-1.0f,-1.0f,  // top-right of back
            -1.0f,-1.0f,-1.0f, // top-left of back
            -1.0f, 1.0f,-1.0f, // bottom-left of back
            1.0f, 1.0f,-1.0f,  // bottom-right of back
            
            // left surface
            -1.0f, 1.0f, 1.0f, // top-right of left
            -1.0f, 1.0f,-1.0f, // top-left of left
            -1.0f,-1.0f,-1.0f, // bottom-left of left
            -1.0f,-1.0f, 1.0f, // bottom-right of left
            
            // right surface
            1.0f, 1.0f,-1.0f,  // top-right of right
            1.0f, 1.0f, 1.0f,  // top-left of right
            1.0f,-1.0f, 1.0f,  // bottom-left of right
            1.0f,-1.0f,-1.0f,  // bottom-right of right
        };
        
        // If above -1.0f Or +1.0f Values Make Cube Much Larger Than Pyramid,
        // then follow the code in following loop which will convertt all 1s And -1s to -0.75 or +0.75
        for(int i=0;i<72;i++)
        {
            if(cubeVertices[i]<0.0f)
                cubeVertices[i]=cubeVertices[i]+0.25f;
            else if(cubeVertices[i]>0.0f)
                cubeVertices[i]=cubeVertices[i]-0.25f;
            else
                cubeVertices[i]=cubeVertices[i]; // no change
        }
        
        float cubeTexcoords[]= new float[]
        {
            0.0f,0.0f,
            1.0f,0.0f,
            1.0f,1.0f,
            0.0f,1.0f,
            
            0.0f,0.0f,
            1.0f,0.0f,
            1.0f,1.0f,
            0.0f,1.0f,
            
            0.0f,0.0f,
            1.0f,0.0f,
            1.0f,1.0f,
            0.0f,1.0f,
            
            0.0f,0.0f,
            1.0f,0.0f,
            1.0f,1.0f,
            0.0f,1.0f,
            
            0.0f,0.0f,
            1.0f,0.0f,
            1.0f,1.0f,
            0.0f,1.0f,
            
            0.0f,0.0f,
            1.0f,0.0f,
            1.0f,1.0f,
            0.0f,1.0f,
        };

        // PYRAMID
        // vao
        GLES32.glGenVertexArrays(1,vao_pyramid,0);
        GLES32.glBindVertexArray(vao_pyramid[0]);

        // position vbo
        GLES32.glGenBuffers(1,vbo_pyramid_position,0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vbo_pyramid_position[0]);
        
        ByteBuffer byteBuffer=ByteBuffer.allocateDirect(pyramidVertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer verticesBuffer=byteBuffer.asFloatBuffer();
        verticesBuffer.put(pyramidVertices);
        verticesBuffer.position(0);
        
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,
                            pyramidVertices.length * 4,
                            verticesBuffer,
                            GLES32.GL_STATIC_DRAW);
        
        GLES32.glVertexAttribPointer(GLESMacros.VDG_ATTRIBUTE_VERTEX,
                                     3,
                                     GLES32.GL_FLOAT,
                                     false,0,0);
        
        GLES32.glEnableVertexAttribArray(GLESMacros.VDG_ATTRIBUTE_VERTEX);
        
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);
        
        // texture vbo
        GLES32.glGenBuffers(1,vbo_pyramid_texture,0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vbo_pyramid_texture[0]);
        
        byteBuffer=ByteBuffer.allocateDirect(pyramidTexcoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        verticesBuffer=byteBuffer.asFloatBuffer();
        verticesBuffer.put(pyramidTexcoords);
        verticesBuffer.position(0);
        
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,
                            pyramidTexcoords.length * 4,
                            verticesBuffer,
                            GLES32.GL_STATIC_DRAW);
        
        GLES32.glVertexAttribPointer(GLESMacros.VDG_ATTRIBUTE_TEXTURE0,
                                     2,
                                     GLES32.GL_FLOAT,
                                     false,0,0);
        
        GLES32.glEnableVertexAttribArray(GLESMacros.VDG_ATTRIBUTE_TEXTURE0);
        
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);

        GLES32.glBindVertexArray(0);
        
        // CUBE
        // vao
        GLES32.glGenVertexArrays(1,vao_cube,0);
        GLES32.glBindVertexArray(vao_cube[0]);
        
        // position vbo
        GLES32.glGenBuffers(1,vbo_cube_position,0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vbo_cube_position[0]);
        
        byteBuffer=ByteBuffer.allocateDirect(cubeVertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        verticesBuffer=byteBuffer.asFloatBuffer();
        verticesBuffer.put(cubeVertices);
        verticesBuffer.position(0);
        
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,
                            cubeVertices.length * 4,
                            verticesBuffer,
                            GLES32.GL_STATIC_DRAW);
        
        GLES32.glVertexAttribPointer(GLESMacros.VDG_ATTRIBUTE_VERTEX,
                                     3,
                                     GLES32.GL_FLOAT,
                                     false,0,0);
        
        GLES32.glEnableVertexAttribArray(GLESMacros.VDG_ATTRIBUTE_VERTEX);
        
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);
        
        // texture vbo
        GLES32.glGenBuffers(1,vbo_cube_texture,0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vbo_cube_texture[0]);
        
        byteBuffer=ByteBuffer.allocateDirect(cubeTexcoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        verticesBuffer=byteBuffer.asFloatBuffer();
        verticesBuffer.put(cubeTexcoords);
        verticesBuffer.position(0);
        
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,
                            cubeTexcoords.length * 4,
                            verticesBuffer,
                            GLES32.GL_STATIC_DRAW);
        
        GLES32.glVertexAttribPointer(GLESMacros.VDG_ATTRIBUTE_TEXTURE0,
                                     2,
                                     GLES32.GL_FLOAT,
                                     false,0,0);
        
        GLES32.glEnableVertexAttribArray(GLESMacros.VDG_ATTRIBUTE_TEXTURE0);
        
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);
        
        GLES32.glBindVertexArray(0);

        // enable depth testing
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);
        // depth test to do
        GLES32.glDepthFunc(GLES32.GL_LEQUAL);
        // We will always cull back faces for better performance
        GLES32.glEnable(GLES32.GL_CULL_FACE);
        
        // Set the background color
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // black
        
        // set projectionMatrix to identitu matrix
        Matrix.setIdentityM(perspectiveProjectionMatrix,0);
    }
    
    private int loadGLTexture(int imageFileResourceID)
    {
        // code
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        
        // read in the resource
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imageFileResourceID, options);
        
        int[] texture=new int[1];
        
        // create a texture object to apply to model
        GLES32.glGenTextures(1, texture, 0);
        
        // indicate that pixel rows are tightly packed (defaults to stride of 4 which is kind of only good for RGBA or FLOAT data types)
        GLES32.glPixelStorei(GLES32.GL_UNPACK_ALIGNMENT,1);
        
        // bind with the texture
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D,texture[0]);
        
        // set up filter and wrap modes for this texture object
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D,GLES32.GL_TEXTURE_MAG_FILTER,GLES32.GL_LINEAR);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D,GLES32.GL_TEXTURE_MIN_FILTER,GLES32.GL_LINEAR_MIPMAP_LINEAR);
        
        // load the bitmap into the bound texture
        GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, bitmap, 0);
        
        // generate mipmap
        GLES32.glGenerateMipmap(GLES32.GL_TEXTURE_2D);
        
        return(texture[0]);
    }
    
    private void resize(int width, int height)
    {
        // code
        GLES32.glViewport(0, 0, width, height);
        
        // calculate the projection matrix
        Matrix.perspectiveM(perspectiveProjectionMatrix,0,45.0f,(float)width/(float)height,0.1f,100.0f); // typecasting is IMP
    }
    
    public void display()
    {
        // code
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);
        
        // use shader program
        GLES32.glUseProgram(shaderProgramObject);

        // OpenGL-ES drawing
        float modelViewMatrix[]=new float[16];
        float rotationMatrix[]=new float[16];
        float modelViewProjectionMatrix[]=new float[16];
        
        // PYRAMID CODE
        // set modelview & modelviewprojection matrices to identity
        Matrix.setIdentityM(modelViewMatrix,0);
        Matrix.setIdentityM(modelViewProjectionMatrix,0);

        // apply z axis translation to go deep into the screen by -5.0,
        // so that pyramid with same fullscreen co-ordinates, but due to above translation will look small
        Matrix.translateM(modelViewMatrix,0,-1.5f,0.0f,-5.0f);
        
        Matrix.setRotateM(rotationMatrix,0,anglePyramid,0.0f,1.0f,0.0f); // Y axis rotation by anglePyramid angle
        Matrix.multiplyMM(modelViewMatrix,0,modelViewMatrix,0,rotationMatrix,0);
        
        // multiply the modelview and projection matrix to get modelviewprojection matrix
        Matrix.multiplyMM(modelViewProjectionMatrix,0,perspectiveProjectionMatrix,0,modelViewMatrix,0);
        
        // pass above modelviewprojection matrix to the vertex shader in 'u_mvp_matrix' shader variable
        // whose position value we already calculated in initWithFrame() by using glGetUniformLocation()
        GLES32.glUniformMatrix4fv(mvpUniform,1,false,modelViewProjectionMatrix,0);
        
        // bind vao
        GLES32.glBindVertexArray(vao_pyramid[0]);
        
        // bind with pyramid texture
        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D,texture_stone[0]);
         // 0th sampler enable ( as we have only 1 texture sampler in fragment shader )
        GLES32.glUniform1i(texture0_sampler_uniform, 0);

        // draw, either by glDrawTriangles() or glDrawArrays() or glDrawElements()
        GLES32.glDrawArrays(GLES32.GL_TRIANGLES,0,12); // 3 (each with its x,y,z ) vertices for 4 faces of pyramid
        
        // unbind vao
        GLES32.glBindVertexArray(0);
        
        // CUBE CODE
        // set modelViewMatrix to identitu matrix
        Matrix.setIdentityM(modelViewMatrix,0);
        Matrix.setIdentityM(modelViewProjectionMatrix,0);

        // apply z axis translation to go deep into the screen by -5.0,
        // so that pyramid with same fullscreen co-ordinates, but due to above translation will look small
        Matrix.translateM(modelViewMatrix,0,1.5f,0.0f,-5.0f);
        
        Matrix.setRotateM(rotationMatrix,0,angleCube,1.0f,1.0f,1.0f); // ALL axes rotation by angleCube angle
        Matrix.multiplyMM(modelViewMatrix,0,modelViewMatrix,0,rotationMatrix,0);
        
        // multiply the modelview and projection matrix to get modelviewprojection matrix
        Matrix.multiplyMM(modelViewProjectionMatrix,0,perspectiveProjectionMatrix,0,modelViewMatrix,0);
        
        // pass above modelviewprojection matrix to the vertex shader in 'u_mvp_matrix' shader variable
        // whose position value we already calculated in initWithFrame() by using glGetUniformLocation()
        GLES32.glUniformMatrix4fv(mvpUniform,1,false,modelViewProjectionMatrix,0);
        
        // bind vao
        GLES32.glBindVertexArray(vao_cube[0]);
        
        // bind with cube texture
        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D,texture_kundali[0]);
        // 0th sampler enable ( as we have only 1 texture sampler in fragment shader )
        GLES32.glUniform1i(texture0_sampler_uniform, 0);

        // draw, either by glDrawTriangles() or glDrawArrays() or glDrawElements()
        // *** draw, either by glDrawTriangles() or glDrawArrays() or glDrawElements()
        // actually 2 triangles make 1 square, so there should be 6 vertices,
        // but as 2 tringles while making square meet each other at diagonal,
        // 2 of 6 vertices are common to both triangles, and hence 6-2=4
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,0,4);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,4,4);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,8,4);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,12,4);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,16,4);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,20,4);
        
        // unbind vao
        GLES32.glBindVertexArray(0);
        
        // un-use shader program
        GLES32.glUseProgram(0);

        // keep rotating
        anglePyramid = anglePyramid + 0.75f;
        angleCube = angleCube - 0.75f;
        
        // render/flush
        requestRender();
    }
    
    void uninitialize()
    {
        // code
        // PYRAMID
        // destroy vao
        if(vao_pyramid[0] != 0)
        {
            GLES32.glDeleteVertexArrays(1, vao_pyramid, 0);
            vao_pyramid[0]=0;
        }
        
        // destroy position vbo
        if(vbo_pyramid_position[0] != 0)
        {
            GLES32.glDeleteBuffers(1, vbo_pyramid_position, 0);
            vbo_pyramid_position[0]=0;
        }
        
        // destroy texture vbo
        if(vbo_pyramid_texture[0] != 0)
        {
            GLES32.glDeleteBuffers(1, vbo_pyramid_texture, 0);
            vbo_pyramid_texture[0]=0;
        }

        // CUBE
        // destroy vao
        if(vao_cube[0] != 0)
        {
            GLES32.glDeleteVertexArrays(1, vao_cube, 0);
            vao_cube[0]=0;
        }
        
        // destroy position vbo
        if(vbo_cube_position[0] != 0)
        {
            GLES32.glDeleteBuffers(1, vbo_cube_position, 0);
            vbo_cube_position[0]=0;
        }
        
        // destroy texture vbo
        if(vbo_cube_texture[0] != 0)
        {
            GLES32.glDeleteBuffers(1, vbo_cube_texture, 0);
            vbo_cube_texture[0]=0;
        }

        if(shaderProgramObject != 0)
        {
            if(vertexShaderObject != 0)
            {
                // detach vertex shader from shader program object
                GLES32.glDetachShader(shaderProgramObject, vertexShaderObject);
                // delete vertex shader object
                GLES32.glDeleteShader(vertexShaderObject);
                vertexShaderObject = 0;
            }
            
            if(fragmentShaderObject != 0)
            {
                // detach fragment  shader from shader program object
                GLES32.glDetachShader(shaderProgramObject, fragmentShaderObject);
                // delete fragment shader object
                GLES32.glDeleteShader(fragmentShaderObject);
                fragmentShaderObject = 0;
            }
        }

        // delete shader program object
        if(shaderProgramObject != 0)
        {
            GLES32.glDeleteProgram(shaderProgramObject);
            shaderProgramObject = 0;
        }
        
        // delete texture objects
        if (texture_stone[0] != 0)
        {
            GLES32.glDeleteTextures(1, texture_stone, 0);
            texture_stone[0] = 0;
        }
        if (texture_kundali[0] != 0)
        {
            GLES32.glDeleteTextures(1, texture_kundali, 0);
            texture_kundali[0] = 0;
        }
    }
}
