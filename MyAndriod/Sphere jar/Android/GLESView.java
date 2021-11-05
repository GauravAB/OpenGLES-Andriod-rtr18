package com.astromedicomp.winvm_gles_light_diffuse;

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

// A view for OpenGLES3 graphics which also receives touch events
public class GLESView extends GLSurfaceView implements GLSurfaceView.Renderer, OnGestureListener, OnDoubleTapListener
{
    private final Context context;
    
    private GestureDetector gestureDetector;
    
    private int vertexShaderObject;
    private int fragmentShaderObject;
    private int shaderProgramObject;

    private int[] vao_cube = new int[1];
    private int[] vbo_cube_position = new int[1];
    private int[] vbo_cube_normal = new int[1];
    
    private int  modelViewMatrixUniform, projectionMatrixUniform;
    private int  ldUniform, kdUniform, lightPositionUniform;
    
    private int doubleTapUniform;

    private float perspectiveProjectionMatrix[]=new float[16]; // 4x4 matrix
    
    private float angleCube= 0.0f;
    
    private int singleTap; // for animation
    private int doubleTap; // for lights
    
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
        // code
        doubleTap++;
        if(doubleTap > 1)
            doubleTap=0;
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
        // code
        singleTap++;
        if(singleTap > 1)
            singleTap=0;
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
         "in vec3 vNormal;"+
         "uniform mat4 u_model_view_matrix;"+
         "uniform mat4 u_projection_matrix;"+
         "uniform mediump int u_double_tap;"+
         "uniform vec3 u_Ld;"+
         "uniform vec3 u_Kd;"+
         "uniform vec4 u_light_position;"+
         "out vec3 diffuse_light;"+
         "void main(void)"+
         "{"+
         "if (u_double_tap == 1)"+
         "{"+
         "vec4 eyeCoordinates = u_model_view_matrix * vPosition;"+
         "vec3 tnorm = normalize(mat3(u_model_view_matrix) * vNormal);"+
         "vec3 s = normalize(vec3(u_light_position - eyeCoordinates));"+
         "diffuse_light = u_Ld * u_Kd * max(dot(s, tnorm), 0.0);"+
         "}"+
         "gl_Position = u_projection_matrix * u_model_view_matrix * vPosition;"+
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
         "in vec3 diffuse_light;"+
         "out vec4 FragColor;"+
         "uniform int u_double_tap;"+
         "void main(void)"+
         "{"+
         "vec4 color;"+
         "if (u_double_tap == 1)"+
         "{"+
         "color = vec4(diffuse_light,1.0);"+
         "}"+
         "else"+
         "{"+
         "color = vec4(1.0, 1.0, 1.0, 1.0);"+
         "}"+
         "FragColor = color;"+
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
        GLES32.glBindAttribLocation(shaderProgramObject,GLESMacros.VDG_ATTRIBUTE_NORMAL,"vNormal");

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

        // get uniform locations
        modelViewMatrixUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_model_view_matrix");
        projectionMatrixUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_projection_matrix");
        
        doubleTapUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_double_tap");
        
        ldUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_Ld");
        kdUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_Kd");
        lightPositionUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_light_position");;

        // *** vertices, colors, shader attribs, vbo, vao initializations ***
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
        
        float cubeNormals[]= new float[]
        {
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f
        };

        // CUBE
        // vao
        GLES32.glGenVertexArrays(1,vao_cube,0);
        GLES32.glBindVertexArray(vao_cube[0]);
        
        // position vbo
        GLES32.glGenBuffers(1,vbo_cube_position,0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vbo_cube_position[0]);
        
        ByteBuffer byteBuffer=ByteBuffer.allocateDirect(cubeVertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer verticesBuffer=byteBuffer.asFloatBuffer();
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
        
        // normal vbo
        GLES32.glGenBuffers(1,vbo_cube_normal,0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vbo_cube_normal[0]);
        
        byteBuffer=ByteBuffer.allocateDirect(cubeNormals.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        verticesBuffer=byteBuffer.asFloatBuffer();
        verticesBuffer.put(cubeNormals);
        verticesBuffer.position(0);
        
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,
                            cubeNormals.length * 4,
                            verticesBuffer,
                            GLES32.GL_STATIC_DRAW);
        
        GLES32.glVertexAttribPointer(GLESMacros.VDG_ATTRIBUTE_NORMAL,
                                     3,
                                     GLES32.GL_FLOAT,
                                     false,0,0);
        
        GLES32.glEnableVertexAttribArray(GLESMacros.VDG_ATTRIBUTE_NORMAL);
        
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
        
        // initialization
        singleTap=0;
        doubleTap=0;
        
        // set projectionMatrix to identitu matrix
        Matrix.setIdentityM(perspectiveProjectionMatrix,0);
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

        if(doubleTap==1)
        {
            GLES32.glUniform1i(doubleTapUniform, 1);
            
            // setting light properties
            GLES32.glUniform3f(ldUniform, 1.0f, 1.0f, 1.0f); // diffuse intensity of light
            GLES32.glUniform3f(kdUniform, 0.5f, 0.5f, 0.5f); // diffuse reflectivity of material
            float[] lightPosition = {0.0f, 0.0f, 2.0f, 1.0f};
            GLES32.glUniform4fv(lightPositionUniform, 1, lightPosition,0); // light position
        }
        else
        {
            GLES32.glUniform1i(doubleTapUniform, 0);
        }
        
        // OpenGL-ES drawing
        float modelMatrix[]=new float[16];
        float modelViewMatrix[]=new float[16];
        float rotationMatrix[]=new float[16];
        
        // CUBE CODE
        // set modelMatrix, modelViewMatrix, rotation matrices to identity matrix
        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(modelViewMatrix,0);
        Matrix.setIdentityM(rotationMatrix,0);

        // apply z axis translation to go deep into the screen by -5.0,
        // so that pyramid with same fullscreen co-ordinates, but due to above translation will look small
        Matrix.translateM(modelMatrix,0,0.0f,0.0f,-5.0f);
        
        Matrix.setRotateM(rotationMatrix,0,angleCube,1.0f,1.0f,1.0f); // ALL axes rotation by angleCube angle
        Matrix.multiplyMM(modelViewMatrix,0,modelMatrix,0,rotationMatrix,0);
        
        // pass above modelview matrix to the vertex shader in 'u_model_view_matrix' shader variable
        GLES32.glUniformMatrix4fv(modelViewMatrixUniform,1,false,modelViewMatrix,0);
        // pass projection matrix to the vertex shader in 'u_projection_matrix' shader variable
        GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,perspectiveProjectionMatrix,0);
        
        // bind vao
        GLES32.glBindVertexArray(vao_cube[0]);
        
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
        if(singleTap==1)
        {
            angleCube = angleCube - 0.75f;
        }
        
        // render/flush
        requestRender();
    }
    
    void uninitialize()
    {
        // code
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
        
        // destroy normal vbo
        if(vbo_cube_normal[0] != 0)
        {
            GLES32.glDeleteBuffers(1, vbo_cube_normal, 0);
            vbo_cube_normal[0]=0;
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
    }
}
