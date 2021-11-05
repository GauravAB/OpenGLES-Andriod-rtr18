package com.gabandroid.ThreeDimensionalAnimation;

import android.content.Context;	//for drawing context related
import android.opengl.GLSurfaceView;	//openGL surface view and all related
import javax.microedition.khronos.opengles.GL10;	//openGLES 1.0 needed as param type GL10
import javax.microedition.khronos.egl.EGLConfig;
import android.opengl.GLES32;
import android.view.MotionEvent;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.OnDoubleTapListener;

//for vbo
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

//for math variables and calculations
import android.opengl.Matrix;


public class GLESView extends GLSurfaceView implements GLSurfaceView.Renderer,OnGestureListener,OnDoubleTapListener
{

	private final Context context;
	private GestureDetector gestureDetector;

	private int vertexShaderObject;
	private int fragmentShaderObject;
	private int shaderProgramObject;

	private int[] vao_pyramid = new int[1];
	private int[] vao_cube = new int[1];
	
	private int[] vbo_pyramid_pos = new int[1];
	private int[] vbo_pyramid_color = new int[1];

	private int[] vbo_cube_pos = new int[1];
	private int[] vbo_cube_color = new int[1];
	private int mvpUniform;

	//matrix for perpective projection 
	private float perspectiveProjectionMatrix[] = new float[16];

	private float anglePyramid;
	private float angleCube;


	public GLESView(Context drawingContext)
	{
		super(drawingContext);
		context = drawingContext;

		setEGLContextClientVersion(3);

		//set renderer for drawing on EGL Surface view
		setRenderer(this);

		//set render mode to draw on surface when there is change in data
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		gestureDetector = new GestureDetector(context,this,null,false);
		gestureDetector.setOnDoubleTapListener(this);
	}

	//overriden method of GLSurfaceView.Renderer(Init Code)
	@Override
	public void onSurfaceCreated(GL10 gl , EGLConfig config)
	{
		//get opengl es version
		String glesVersion = gl.glGetString(GL10.GL_VERSION);
		System.out.println("GAB: OpenGL-ES Version = " + glesVersion);
		//get gles version
		String glslVersion = gl.glGetString(GLES32.GL_SHADING_LANGUAGE_VERSION);
		System.out.println("GAB: GLSL Version = " + glslVersion);

		initialize(gl);
	}

	//overriden method of GLSurfaceView.Renderer(change size Code)
	@Override
	public void onSurfaceChanged(GL10 unused , int width , int height)
	{
		resize(width,height);
	}

	//overriden method of GLSurfaceView.Renderer(rendering code)
	@Override
	public void onDrawFrame(GL10 unused)
	{
		update();
		display();
	}

	//Handling on touch event is most important 
	//Because it triggers all gesture and tap events

	@Override
	public boolean onTouchEvent(MotionEvent e)
	{
		//code
		int eventaction = e.getAction();
		if(!gestureDetector.onTouchEvent(e))
		{
			super.onTouchEvent(e);
		}

		return(true);
	}

	//abstract method fr double tap listener
	@Override 
	public boolean onDoubleTap(MotionEvent e)
	{
		return (true);
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e)
	{
		return (true);
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e)
	{
		return (true);
	}

	@Override
	public boolean onDown(MotionEvent e)
	{
		return(true);
	}

	@Override
	public boolean onFling(MotionEvent e1,MotionEvent e2,float velocityX,float velocityY)
	{
		return (true);
	}

	@Override
	public void onLongPress(MotionEvent e)
	{

	}

	@Override
	public boolean onScroll(MotionEvent e1 , MotionEvent e2 , float distanceX , float distanceY)
	{
		uninitialize();
		System.exit(0);
		return(true);
	}

	@Override
	public void onShowPress(MotionEvent e)
	{

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e)
	{
		return (true);
	}

	private void initialize(GL10 gl)
	{
		//Vertex Shader
		System.out.println("GAB: Inside Initialize");


		//create shader
		vertexShaderObject = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);
		System.out.println("GAB: Created Vertex Shader");

		//vertex shader source code
		final String vertexShaderSourceCode = String.format
		(
			"#version 310 es"+
			"\n"+
			"in vec4 vPosition;"+
			"in vec4 vColor;"+
			"out vec4 out_color;"+
			"uniform mat4 u_mvp_matrix;"+
			"void main(void)"+
			"{"+
			"gl_Position = u_mvp_matrix * vPosition;"+
			"out_color = vColor;"+
			"}"
		);

		//provide source code to shader
		GLES32.glShaderSource(vertexShaderObject,vertexShaderSourceCode);

		System.out.println("GAB: vertex Shader Object created");

		System.out.println("GAB: compiling...");

		//compileShader & check for errors
		GLES32.glCompileShader(vertexShaderObject);

		int[] iShaderCompiledStatus = new int[1];
		int[] iInfoLogLength = new int[1];
		String szInfoLog = null;

		GLES32.glGetShaderiv(vertexShaderObject,GLES32.GL_COMPILE_STATUS,iShaderCompiledStatus,0);
		if(iShaderCompiledStatus[0] == GLES32.GL_FALSE)
		{
			GLES32.glGetShaderiv(vertexShaderObject,GLES32.GL_INFO_LOG_LENGTH,iInfoLogLength,0);
			if(iInfoLogLength[0] > 0)
			{
		
				szInfoLog = GLES32.glGetShaderInfoLog(vertexShaderObject);
				System.out.println("GAB: Vertex Shader Compilation Log ="+szInfoLog);
				uninitialize();
				System.exit(0);
			}
		}
		else
		{

			System.out.println("GAB: VertexShader compile success");

		}

		//Fragment Shader
		//create shader
		fragmentShaderObject = GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER);

		//fragment shader source code
		final String fragmentShaderSourceCode = String.format
		(
			"#version 310 es"+
			"\n"+
			"precision highp float;"+
			"in vec4 out_color;"+
			"out vec4 FragColor;"+
			"void main(void)"+
			"{"+
			"FragColor = out_color;"+
			"}"
		);

		
		GLES32.glShaderSource(fragmentShaderObject,fragmentShaderSourceCode);

		//compile shader and check for errors
		GLES32.glCompileShader(fragmentShaderObject);
		iShaderCompiledStatus[0] = 0;	//reinitialize
		iInfoLogLength[0] = 0;		//reinitialize
		szInfoLog = null;

		GLES32.glGetShaderiv(fragmentShaderObject,GLES32.GL_COMPILE_STATUS,iShaderCompiledStatus,0);

		if(iShaderCompiledStatus[0] == 0)
		{
			GLES32.glGetShaderiv(fragmentShaderObject,GLES32.GL_INFO_LOG_LENGTH,iInfoLogLength,0);
			if(iInfoLogLength[0] >0)
			{
				szInfoLog = GLES32.glGetShaderInfoLog(fragmentShaderObject);
				System.out.println("GAB: Fragment Shader Compilation log = "+szInfoLog);
				
				uninitialize();
				System.exit(0);
			}

		}
		else
		{

			System.out.println("GAB: Fragment Shader compile success");

		}

		shaderProgramObject = GLES32.glCreateProgram();

		GLES32.glAttachShader(shaderProgramObject,vertexShaderObject);
		GLES32.glAttachShader(shaderProgramObject,fragmentShaderObject);

		//comfirm data layout with shader 
		GLES32.glBindAttribLocation(shaderProgramObject,GLESMacros.GAB_ATTRIBUTE_VERTEX,"vPosition");
		GLES32.glBindAttribLocation(shaderProgramObject,GLESMacros.GAB_ATTRIBUTE_COLOR,"vColor");

		//link two shaders togather to shader Program object
		GLES32.glLinkProgram(shaderProgramObject);
		int[] iShaderProgramLinkStatus = new  int[1];
		iInfoLogLength[0] = 0;
		szInfoLog= null;

		GLES32.glGetProgramiv(shaderProgramObject,GLES32.GL_LINK_STATUS,iShaderProgramLinkStatus,0);
		if(iShaderProgramLinkStatus[0] == GLES32.GL_FALSE)
		{
			GLES32.glGetProgramiv(shaderProgramObject,GLES32.GL_INFO_LOG_LENGTH,iInfoLogLength,0);
			if(iInfoLogLength[0] > 0)
				{
					szInfoLog = GLES32.glGetProgramInfoLog(shaderProgramObject);
					System.out.println("GAB: shader program Link Log : = "+szInfoLog);
					uninitialize();
					System.exit(0);
				}
		}

		mvpUniform = GLES32.glGetUniformLocation(shaderProgramObject,"u_mvp_matrix");

		final float pyramidVertices[] = new float[]
		{
				0.0f,0.5f,0.0f,
			   -0.5f,-0.5f,0.5f,
				0.5f,-0.5f,0.5f,
				//right side
				0.0f,0.5f,0.0f,
				0.5f,-0.5f,0.5f,
				0.5f,-0.5f,-0.5f,
				//back side
				0.0f,0.5f,0.0f,
				0.5f,-0.5f,-0.5f,
			   -0.5f,-0.5f,-0.5f,
				//left side
				0.0f,0.5f,0.0f,
			   -0.5f,-0.5f,-0.5f,
			   -0.5f,-0.5f,0.5f
		};

		final float pyramidColors[] = new float[]
		{

		    1.0f,0.0f,0.0f,
			0.0f,1.0f,0.0f,
			0.0f,0.0f,1.0f,
			//right face
			1.0f,0.0f,0.0f,
			0.0f,0.0f,1.0f,
			0.0f,1.0f,0.0f,
			//back face
			1.0f,0.0f,0.0f,
			0.0f,1.0f,0.0f,
			0.0f,0.0f,1.0f,
			//left face
			1.0f,0.0f,0.0f,
			0.0f,0.0f,1.0f,
			0.0f,1.0f,0.0f

		};


		final float cubeVertices[] = new float[]
		{
				1.0f,1.0f,-1.0f,
				-1.0f,1.0f,-1.0f,
				-1.0f,1.0f,1.0f,
				1.0f,1.0f,1.0f,

				1.0f,-1.0f,1.0f,
				-1.0f,-1.0f,1.0f,
				-1.0f,-1.0f,-1.0f,
				1.0f,-1.0f,-1.0f,

				1.0f,1.0f,1.0f,
				-1.0f,1.0f,1.0f,
				-1.0f,-1.0f,1.0f,
				1.0f,-1.0f,1.0f,

				1.0f,-1.0f,-1.0f,
				-1.0f,-1.0f,-1.0f,
				-1.0f,1.0f,-1.0f,
				1.0f,1.0f,-1.0f,

				-1.0f,1.0f,1.0f,
				-1.0f,1.0f,-1.0f,
				-1.0f,-1.0f,-1.0f,
				-1.0f,-1.0f,1.0f,

				1.0f,1.0f,-1.0f,
				1.0f,1.0f,1.0f,
				1.0f,-1.0f,1.0f,
				1.0f,-1.0f,-1.0f,
		};	

		final float cubeColors[] = new float []
		{
				0.0f,1.0f,0.0f,
				0.0f,1.0f,0.0f,
				0.0f,1.0f,0.0f,
				0.0f,1.0f,0.0f,

				1.0f,0.5f,0.0f,
				1.0f,0.5f,0.0f,
				1.0f,0.5f,0.0f,
				1.0f,0.5f,0.0f,

				1.0f,0.0f,0.0f,
				1.0f,0.0f,0.0f,
				1.0f,0.0f,0.0f,
				1.0f,0.0f,0.0f,

				1.0f,1.0f,0.0f,
				1.0f,1.0f,0.0f,
				1.0f,1.0f,0.0f,
				1.0f,1.0f,0.0f,

				0.0f,0.0f,1.0f,
				0.0f,0.0f,1.0f,
				0.0f,0.0f,1.0f,
				0.0f,0.0f,1.0f,

				1.0f,0.0f,1.0f,
				1.0f,0.0f,1.0f,
				1.0f,0.0f,1.0f,
				1.0f,0.0f,1.0f,
		};

		for (int i = 0; i < 72; i++)
		{
			if (cubeVertices[i] > 0.0f)
			{
				cubeVertices[i] = cubeVertices[i]-0.25f;
			}
			else if (cubeVertices[i] < 0.0f)
			{
				cubeVertices[i] = cubeVertices[i] + 0.25f;
			}
			else
			{
				cubeVertices[i] = cubeVertices[i];
			}
		}


		//---------------------------VAO TRIANGLE START-----------------------------------------------
		GLES32.glGenVertexArrays(1,vao_pyramid,0);
		GLES32.glBindVertexArray(vao_pyramid[0]);

		//position buffer-----------------------------------------------------------------------------
		GLES32.glGenBuffers(1,vbo_pyramid_pos,0);
		GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vbo_pyramid_pos[0]);
		//create c like buffer
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(pyramidVertices.length*4);
		//set order of bytes (endianness and all)
		byteBuffer.order(ByteOrder.nativeOrder());
		//float type of buffer
		FloatBuffer verticesBufferPyramid = byteBuffer.asFloatBuffer();
		//data inside buffer
		verticesBufferPyramid.put(pyramidVertices);
		//start from
		verticesBufferPyramid.position(0);
		GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,pyramidVertices.length*4,verticesBufferPyramid,GLES32.GL_STATIC_DRAW);
		GLES32.glVertexAttribPointer(GLESMacros.GAB_ATTRIBUTE_VERTEX,3,GLES32.GL_FLOAT,false,0,0);
		GLES32.glEnableVertexAttribArray(GLESMacros.GAB_ATTRIBUTE_VERTEX);
		GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);
		//position buffer end------------------------------------------------------------------------------

		GLES32.glGenBuffers(1,vbo_pyramid_color,0);
		GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vbo_pyramid_color[0]);

		byteBuffer = ByteBuffer.allocateDirect(pyramidColors.length*4);
		byteBuffer.order(ByteOrder.nativeOrder());
		FloatBuffer colorBufferPyramid = byteBuffer.asFloatBuffer();
		colorBufferPyramid.put(pyramidColors);
		colorBufferPyramid.position(0);

		GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,pyramidColors.length*4,colorBufferPyramid,GLES32.GL_STATIC_DRAW);
		GLES32.glVertexAttribPointer(GLESMacros.GAB_ATTRIBUTE_COLOR,3,GLES32.GL_FLOAT,false,0,0);
		GLES32.glEnableVertexAttribArray(GLESMacros.GAB_ATTRIBUTE_COLOR);
		GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);

		GLES32.glBindVertexArray(0);

		//---------------------------VAO TRIANGLE END----------------------------------------------------

		//---------------------------VAO SQUARE START----------------------------------------------------

		GLES32.glGenVertexArrays(1,vao_cube,0);
		GLES32.glBindVertexArray(vao_cube[0]);

		GLES32.glGenBuffers(1,vbo_cube_pos,0);
		GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vbo_cube_pos[0]);

		byteBuffer = ByteBuffer.allocateDirect(cubeVertices.length*4);
		byteBuffer.order(ByteOrder.nativeOrder());
		FloatBuffer verticesBufferCube =  byteBuffer.asFloatBuffer();

		verticesBufferCube.put(cubeVertices);
		verticesBufferCube.position(0);

		GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,cubeVertices.length*4,verticesBufferCube,GLES32.GL_STATIC_DRAW);
		GLES32.glVertexAttribPointer(GLESMacros.GAB_ATTRIBUTE_VERTEX,3,GLES32.GL_FLOAT,false,0,0);
		GLES32.glEnableVertexAttribArray(GLESMacros.GAB_ATTRIBUTE_VERTEX);
		GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);

		GLES32.glGenBuffers(1,vbo_cube_color,0);
		GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vbo_cube_color[0]);

		byteBuffer = ByteBuffer.allocateDirect(cubeColors.length*4);
		byteBuffer.order(ByteOrder.nativeOrder());
		FloatBuffer colorBufferCube = byteBuffer.asFloatBuffer();
		colorBufferCube.put(cubeColors);
		colorBufferCube.position(0);

		GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,cubeColors.length*4,colorBufferCube,GLES32.GL_STATIC_DRAW);
		GLES32.glVertexAttribPointer(GLESMacros.GAB_ATTRIBUTE_COLOR,3,GLES32.GL_FLOAT,false,0,0);
		GLES32.glEnableVertexAttribArray(GLESMacros.GAB_ATTRIBUTE_COLOR);
		GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);

		

		GLES32.glBindVertexArray(0);
		//---------------------------VAO SQAURE END------------------------------------------------------



		//enable depth testing
		GLES32.glEnable(GLES32.GL_DEPTH_TEST);
		GLES32.glDepthFunc(GLES32.GL_LEQUAL);

		//set background color
		GLES32.glClearColor(0.0f,0.0f,0.0f,1.0f);

		Matrix.setIdentityM(perspectiveProjectionMatrix,0);

	}

	private void resize(int width , int height)
	{
		GLES32.glViewport(0,0,width,height);

		if(width >= height)
		{
			Matrix.perspectiveM(perspectiveProjectionMatrix,0,45.0f,(float)width/(float)height,0.1f,100.0f);
		}
		else
		{
			Matrix.perspectiveM(perspectiveProjectionMatrix,0,45.0f,(float)height/(float)width,0.1f,100.0f);
		}
	}

	private void update()
	{
		anglePyramid += 1.0f;
		if(anglePyramid >= 360.0f)
		{
			anglePyramid -= 360.0f;
		}

		angleCube += 1.0f;
		if(angleCube >= 360.0f)
		{
			angleCube -= 360.0f;
		}
	}

	private void display()
	{
		//code
		GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);

		//use shader program
		GLES32.glUseProgram(shaderProgramObject);

		//--------------------------------------------------------------------------------------------------
		//triangle
		float modelViewMatrix[] = new float[16];
		float modelViewProjectionMatrix[] = new float[16];


		Matrix.setIdentityM(modelViewMatrix,0);
		Matrix.setIdentityM(modelViewProjectionMatrix,0);
		Matrix.translateM(modelViewMatrix,0,-0.9f,0.0f,-3.0f);
		Matrix.rotateM(modelViewMatrix,0,anglePyramid,0.0f,1.0f,0.0f);
		Matrix.multiplyMM(modelViewProjectionMatrix,0,perspectiveProjectionMatrix,0,modelViewMatrix,0);

		GLES32.glUniformMatrix4fv(mvpUniform,1,false,modelViewProjectionMatrix,0);
		//bind vao
		GLES32.glBindVertexArray(vao_pyramid[0]);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLES,0,12);
		GLES32.glBindVertexArray(0);
		//-----------------------------------------------------------------------------------------------------
		//square
		Matrix.setIdentityM(modelViewMatrix,0);
		Matrix.setIdentityM(modelViewProjectionMatrix,0);
		Matrix.translateM(modelViewMatrix,0,1.7f,0.0f,-6.0f);
		Matrix.rotateM(modelViewMatrix,0,angleCube,1.0f,1.0f,1.0f);
		Matrix.multiplyMM(modelViewProjectionMatrix,0,perspectiveProjectionMatrix,0,modelViewMatrix,0);

		GLES32.glUniformMatrix4fv(mvpUniform,1,false,modelViewProjectionMatrix,0);
		//bind vao of square
		GLES32.glBindVertexArray(vao_cube[0]);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,0,4);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,4,4);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,8,4);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,12,4);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,16,4);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN,20,4);
		
		GLES32.glBindVertexArray(0);

		GLES32.glUseProgram(0);

		requestRender();
	}

	private void uninitialize()
	{
		if(vao_pyramid[0] != 0)
		{
			GLES32.glDeleteVertexArrays(1,vao_pyramid,0);
			vao_pyramid[0] = 0;
		}

		if(vbo_pyramid_pos[0] != 0)
		{
			GLES32.glDeleteBuffers(1,vbo_pyramid_pos,0);
			vbo_pyramid_pos[0] = 0;
		}

		if(vbo_pyramid_color[0] != 0)
		{
			GLES32.glDeleteBuffers(1,vbo_pyramid_color,0);
			vbo_pyramid_color[0] = 0;
		}

		if(vao_cube[0] != 0)
		{
			GLES32.glDeleteVertexArrays(1,vao_cube,0);
			vao_cube[0] = 0;
		}

		if(vbo_cube_pos[0] != 0)
		{
			GLES32.glDeleteBuffers(1,vbo_cube_pos,0);
			vbo_cube_pos[0] = 0;
		}
		
		if(vbo_cube_color[0] != 0)
		{
			GLES32.glDeleteBuffers(1,vbo_cube_color,0);
			vbo_cube_color[0] = 0;
		}


		if(shaderProgramObject != 0)
		{
			if(vertexShaderObject != 0)
			{
				GLES32.glDetachShader(shaderProgramObject,vertexShaderObject);
				GLES32.glDeleteShader(vertexShaderObject);
				vertexShaderObject = 0;
			}
		}

		if(fragmentShaderObject != 0)
		{
			GLES32.glDetachShader(shaderProgramObject,fragmentShaderObject);
			GLES32.glDeleteShader(fragmentShaderObject);
			fragmentShaderObject = 0;
		}

		if(shaderProgramObject != 0)
		{
			GLES32.glDeleteProgram(shaderProgramObject);
			shaderProgramObject = 0;
		}
	}
}










