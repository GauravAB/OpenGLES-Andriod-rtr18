package com.gabandroidcubeWithLights;

import android.content.Context;	//for drawing context related
import android.opengl.GLSurfaceView;	//openGL surface view and all related
import javax.microedition.khronos.opengles.GL10;	//openGLES 1.0 needed as param type GL10
import javax.microedition.khronos.egl.EGLConfig;
import android.opengl.GLES31;
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

	private int[] vao_cube = new int[1];
	private int[] vbo_position = new int[1];
	private int[] vbo_normal = new int[1];
	
	private int modelViewUniform;
	private int projectionUniform;
	private int lightEnableUniform;
	private int lightdiffuseUniform;
	private int coefficientOfReflectionUniform;
	private int lightPositionUniform;
	//matrix for perpective projection 
	private float perspectiveProjectionMatrix[] = new float[16];
	private float cubeAngle = 0.0f;

	private boolean lighting = false;
	private boolean Animate = false;

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
		String glslVersion = gl.glGetString(GLES31.GL_SHADING_LANGUAGE_VERSION);
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
		if(Animate == true)
		{
				update();
		}
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
		if(lighting == false)
		{
			lighting = true;
		}
		else
		{
			lighting = false;
		}

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
		if(Animate == false)
		{
			Animate = true;
		}
		else
		{
			Animate = false;
		}
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
		vertexShaderObject = GLES31.glCreateShader(GLES31.GL_VERTEX_SHADER);
		System.out.println("GAB: Created Vertex Shader");

		//vertex shader source code
		final String vertexShaderSourceCode = String.format
		(
			"#version 310 es"+
			"\n"+
			"uniform mediump int u_light_enable;"+
			"in vec4 vPosition;"+
			"in vec3 vNormal;"+	
			"uniform mat4 u_mv_matrix;"+
			"uniform vec3 u_Ld;"+
			"uniform vec3 u_Kd;"+
			"uniform mat4 u_projection_matrix;"+
			"uniform vec4 u_light_position;"+
			"out vec3 diffuse_light;"+
			"void main(void)"+
			"{"+
			"if(u_light_enable == 1)"+
			"{"+
			"vec4 eyecoordinates = u_mv_matrix * vPosition;"+
			"vec3 tnorm = (mat3(u_mv_matrix) * vNormal);"+
			"vec3 s = normalize(vec3(u_light_position - eyecoordinates));"+
			"diffuse_light = u_Ld * u_Kd * max(dot(s,tnorm),0.0f);"+
			"}"+
			"gl_Position = u_projection_matrix *u_mv_matrix * vPosition;"+
			"}"
		);

		//provide source code to shader
		GLES31.glShaderSource(vertexShaderObject,vertexShaderSourceCode);

		System.out.println("GAB: vertex Shader Object created");

		System.out.println("GAB: compiling...");

		//compileShader & check for errors
		GLES31.glCompileShader(vertexShaderObject);

		int[] iShaderCompiledStatus = new int[1];
		int[] iInfoLogLength = new int[1];
		String szInfoLog = null;

		GLES31.glGetShaderiv(vertexShaderObject,GLES31.GL_COMPILE_STATUS,iShaderCompiledStatus,0);
		if(iShaderCompiledStatus[0] == GLES31.GL_FALSE)
		{
			GLES31.glGetShaderiv(vertexShaderObject,GLES31.GL_INFO_LOG_LENGTH,iInfoLogLength,0);
			if(iInfoLogLength[0] > 0)
			{
		
				szInfoLog = GLES31.glGetShaderInfoLog(vertexShaderObject);
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
		fragmentShaderObject = GLES31.glCreateShader(GLES31.GL_FRAGMENT_SHADER);

		//fragment shader source code
		final String fragmentShaderSourceCode = String.format
		(
			"#version 310 es"+
			"\n"+
			"precision highp float;"+
			"out vec4 FragColor;"+
			"uniform int u_light_enable;"+
			"in vec3 diffuse_light;"+
			"void main(void)"+
			"{"+
			"vec4 color;"+
			"if(u_light_enable == 1)"+
			"{"+
			"color = vec4(diffuse_light,0.0f);"+
			"}"+
			"else"+
			"{"+
			"color = vec4(1.0f,1.0f,1.0f,1.0f);"+
			"}"+
			"FragColor = color;"+
			"}"
		);

		
		GLES31.glShaderSource(fragmentShaderObject,fragmentShaderSourceCode);

		//compile shader and check for errors
		GLES31.glCompileShader(fragmentShaderObject);
		iShaderCompiledStatus[0] = 0;	//reinitialize
		iInfoLogLength[0] = 0;		//reinitialize
		szInfoLog = null;

		GLES31.glGetShaderiv(fragmentShaderObject,GLES31.GL_COMPILE_STATUS,iShaderCompiledStatus,0);

		if(iShaderCompiledStatus[0] == 0)
		{
			GLES31.glGetShaderiv(fragmentShaderObject,GLES31.GL_INFO_LOG_LENGTH,iInfoLogLength,0);
			if(iInfoLogLength[0] >0)
			{
				szInfoLog = GLES31.glGetShaderInfoLog(fragmentShaderObject);
				System.out.println("GAB: Fragment Shader Compilation log = "+szInfoLog);
				
				uninitialize();
				System.exit(0);
			}

		}
		else
		{

			System.out.println("GAB: Fragment Shader compile success");

		}

		shaderProgramObject = GLES31.glCreateProgram();

		GLES31.glAttachShader(shaderProgramObject,vertexShaderObject);
		GLES31.glAttachShader(shaderProgramObject,fragmentShaderObject);

		//comfirm data layout with shader 
		GLES31.glBindAttribLocation(shaderProgramObject,GLESMacros.GAB_ATTRIBUTE_VERTEX,"vPosition");
		GLES31.glBindAttribLocation(shaderProgramObject,GLESMacros.GAB_ATTRIBUTE_NORMAL,"vNormal");
		
		//link two shaders togather to shader Program object
		GLES31.glLinkProgram(shaderProgramObject);
		int[] iShaderProgramLinkStatus = new  int[1];
		iInfoLogLength[0] = 0;
		szInfoLog= null;

		GLES31.glGetProgramiv(shaderProgramObject,GLES31.GL_LINK_STATUS,iShaderProgramLinkStatus,0);
		if(iShaderProgramLinkStatus[0] == GLES31.GL_FALSE)
		{
			GLES31.glGetProgramiv(shaderProgramObject,GLES31.GL_INFO_LOG_LENGTH,iInfoLogLength,0);
			if(iInfoLogLength[0] > 0)
				{
					szInfoLog = GLES31.glGetProgramInfoLog(shaderProgramObject);
					System.out.println("GAB: shader program Link Log : = "+szInfoLog);
					uninitialize();
					System.exit(0);
				}
		}

		modelViewUniform = GLES31.glGetUniformLocation(shaderProgramObject,"u_mv_matrix");
		projectionUniform = GLES31.glGetUniformLocation(shaderProgramObject,"u_projection_matrix");
		lightdiffuseUniform = GLES31.glGetUniformLocation(shaderProgramObject,"u_Ld");
		coefficientOfReflectionUniform = GLES31.glGetUniformLocation(shaderProgramObject,"u_Kd");
		lightPositionUniform = GLES31.glGetUniformLocation(shaderProgramObject,"u_light_position");
		lightEnableUniform = GLES31.glGetUniformLocation(shaderProgramObject,"u_light_enable");

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


		final float cubeNormals[] = new float[]
		{
			0.0f,1.0f,0.0f,
			0.0f,1.0f,0.0f,
			0.0f,1.0f,0.0f,
			0.0f,1.0f,0.0f,
			
			0.0f,-1.0f,0.0f,
			0.0f,-1.0f,0.0f,
			0.0f,-1.0f,0.0f,
			0.0f,-1.0f,0.0f,
			
			0.0f,0.0f,1.0f,
			0.0f,0.0f,1.0f,
			0.0f,0.0f,1.0f,
			0.0f,0.0f,1.0f,

			0.0f,0.0f,-1.0f,
			0.0f,0.0f,-1.0f,
			0.0f,0.0f,-1.0f,
			0.0f,0.0f,-1.0f,
			
			-1.0f,0.0f,0.0f,
			-1.0f,0.0f,0.0f,
			-1.0f,0.0f,0.0f,
			-1.0f,0.0f,0.0f,

			1.0f,0.0f,0.0f,
			1.0f,0.0f,0.0f,
			1.0f,0.0f,0.0f,
			1.0f,0.0f,0.0f
		};

		GLES31.glGenVertexArrays(1,vao_cube,0);
		GLES31.glBindVertexArray(vao_cube[0]);


		GLES31.glGenBuffers(1,vbo_position,0);
		GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER,vbo_position[0]);
		//create c like buffer
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(cubeVertices.length*4);
		//set order of bytes (endianness and all)
		byteBuffer.order(ByteOrder.nativeOrder());
		//float type of buffer
		FloatBuffer verticesBuffer = byteBuffer.asFloatBuffer();
		//data inside buffer
		verticesBuffer.put(cubeVertices);
		//start from
		verticesBuffer.position(0);
		GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER,cubeVertices.length*4,verticesBuffer,GLES31.GL_STATIC_DRAW);
		GLES31.glVertexAttribPointer(GLESMacros.GAB_ATTRIBUTE_VERTEX,3,GLES31.GL_FLOAT,false,0,0);
		GLES31.glEnableVertexAttribArray(GLESMacros.GAB_ATTRIBUTE_VERTEX);
		GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER,0);

		GLES31.glGenBuffers(1,vbo_normal,0);
		GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER,vbo_normal[0]);
		//create c like buffer
	    byteBuffer = ByteBuffer.allocateDirect(cubeNormals.length*4);
		//set order of bytes (endianness and all)
		byteBuffer.order(ByteOrder.nativeOrder());
		//float type of buffer
		FloatBuffer normalsBuffer = byteBuffer.asFloatBuffer();
		//data inside buffer
		normalsBuffer.put(cubeNormals);
		//start from
		normalsBuffer.position(0);
		GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER,cubeNormals.length*4,normalsBuffer,GLES31.GL_STATIC_DRAW);
		GLES31.glVertexAttribPointer(GLESMacros.GAB_ATTRIBUTE_NORMAL,3,GLES31.GL_FLOAT,false,0,0);
		GLES31.glEnableVertexAttribArray(GLESMacros.GAB_ATTRIBUTE_NORMAL);
		GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER,0);


		GLES31.glBindVertexArray(0);



		//enable depth testing
		GLES31.glEnable(GLES31.GL_DEPTH_TEST);
		GLES31.glDepthFunc(GLES31.GL_LEQUAL);
		GLES31.glEnable(GLES31.GL_CULL_FACE);

		//set background color
		GLES31.glClearColor(0.0f,0.0f,0.0f,1.0f);

		Matrix.setIdentityM(perspectiveProjectionMatrix,0);

	}

	private void resize(int width , int height)
	{
		GLES31.glViewport(0,0,width,height);

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
		cubeAngle += 1.0f;
		if(cubeAngle >= 360.0f)
		{
			cubeAngle = 0.0f;
		}
	}

	private void display()
	{
		//code
		GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT | GLES31.GL_DEPTH_BUFFER_BIT);

		//use shader program
		GLES31.glUseProgram(shaderProgramObject);

		if(lighting)
		{
			GLES31.glUniform1i(lightEnableUniform,1);
			GLES31.glUniform3f(lightdiffuseUniform,1.0f,1.0f,1.0f);
			GLES31.glUniform3f(coefficientOfReflectionUniform,0.5f,0.5f,0.5f);

			float lightPosition[] = {0.0f,0.0f,2.0f,0.0f};
			GLES31.glUniform4fv(lightPositionUniform,1,lightPosition,0);
		}
		else
		{
			GLES31.glUniform1i(lightEnableUniform,0);
		}

		//opengl es drawing
		float modelViewMatrix[] = new float[16];
		float rotationMatrix[] = new float[16];

		Matrix.setIdentityM(modelViewMatrix,0);
		Matrix.setIdentityM(rotationMatrix,0);

		Matrix.translateM(modelViewMatrix,0,0.0f,0.0f,-5.0f);
		Matrix.rotateM(modelViewMatrix,0,cubeAngle,1.0f,1.0f,1.0f);

		GLES31.glUniformMatrix4fv(modelViewUniform,1,false,modelViewMatrix,0);
		GLES31.glUniformMatrix4fv(projectionUniform,1,false,perspectiveProjectionMatrix,0);

		//bind vao
		GLES31.glBindVertexArray(vao_cube[0]);

		GLES31.glDrawArrays(GLES31.GL_TRIANGLE_FAN,0,4);
		GLES31.glDrawArrays(GLES31.GL_TRIANGLE_FAN,4,4);
		GLES31.glDrawArrays(GLES31.GL_TRIANGLE_FAN,8,4);
		GLES31.glDrawArrays(GLES31.GL_TRIANGLE_FAN,12,4);
		GLES31.glDrawArrays(GLES31.GL_TRIANGLE_FAN,16,4);
		GLES31.glDrawArrays(GLES31.GL_TRIANGLE_FAN,20,4);

		GLES31.glBindVertexArray(0);

		GLES31.glUseProgram(0);

		requestRender();
	}

	private void uninitialize()
	{
		if(vao_cube[0] != 0)
		{
			GLES31.glDeleteVertexArrays(1,vao_cube,0);
			vao_cube[0] = 0;
		}

		if(vbo_normal[0] != 0)
		{
			GLES31.glDeleteBuffers(1,vbo_normal,0);
			vbo_normal[0] = 0;
		}

		if(vbo_position[0] != 0)
		{
			GLES31.glDeleteBuffers(1,vbo_position,0);
			vbo_position[0] = 0;
		}

		if(shaderProgramObject != 0)
		{
			if(vertexShaderObject != 0)
			{
				GLES31.glDetachShader(shaderProgramObject,vertexShaderObject);
				GLES31.glDeleteShader(vertexShaderObject);
				vertexShaderObject = 0;
			}
		}

		if(fragmentShaderObject != 0)
		{
			GLES31.glDetachShader(shaderProgramObject,fragmentShaderObject);
			GLES31.glDeleteShader(fragmentShaderObject);
			fragmentShaderObject = 0;
		}

		if(shaderProgramObject != 0)
		{
			GLES31.glDeleteProgram(shaderProgramObject);
			shaderProgramObject = 0;
		}
	}
}










