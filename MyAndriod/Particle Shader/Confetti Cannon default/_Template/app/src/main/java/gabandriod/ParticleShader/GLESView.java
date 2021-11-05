package com.gabandroid.particleSystem;

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

//for random number generation
import java.util.Random;

public class GLESView extends GLSurfaceView implements GLSurfaceView.Renderer,OnGestureListener,OnDoubleTapListener
{

	private final Context context;
	private GestureDetector gestureDetector;

	private int vertexShaderObject;
	private int fragmentShaderObject;
	private int shaderProgramObject;

	private int[] vao = new int[1];
	private int[] vbo_verts = new int[1];
	private int[] vbo_colors = new int[1];
	private int[] vbo_velocities = new int[1];
	private int[] vbo_start_times = new int[1];
	
	private int mvpUniform;
	private int startTimeUniform;
	private int backgroundUniform;

	//matrix for perpective projection 
	private float perspectiveProjectionMatrix[] = new float[16];

	private int array_width;
	private int array_height;
	private static float ParticleTime = 0.0f;



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
		vertexShaderObject = GLES31.glCreateShader(GLES31.GL_VERTEX_SHADER);
		System.out.println("GAB: Created Vertex Shader");

		//vertex shader source code
		final String vertexShaderSourceCode = String.format
		(
			"#version 310 es"+
			"\n"+
			"uniform float Time;"+
			"uniform vec4 Background;"+
			"uniform mat4 u_mvp_matrix;"+
			"in vec4 MCVertex;"+
			"in vec4 MColor;"+
			"in vec3 Velocity;"+
			"in float StartTime;"+
			"out vec4 fColor;"+
			"void main(void)"+
			"{"+
			"vec4 vert;"+
			"float t = Time - StartTime;"+
			"if (t >= 0.0)"+
			"{"+
			"vert = MCVertex + vec4(Velocity * t , 0.0);"+
			"vert.y -= 4.9 * t * t;"+
			"fColor = MColor;"+
			"}"+
			"else"+
			"{"+
			"vert = MCVertex;"+
			"fColor = Background;"+
			"}"+
			"gl_Position = u_mvp_matrix * vert;"+
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
			"in vec4 fColor;"+
			"out vec4 FragColor;"+
			"void main(void)"+
			"{"+
			"FragColor = fColor;"+
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
		GLES31.glBindAttribLocation(shaderProgramObject,GLESMacros.GAB_ATTRIBUTE_VERTEX,"MCVertex");
		GLES31.glBindAttribLocation(shaderProgramObject,GLESMacros.GAB_ATTRIBUTE_COLOR,"MColor");
		GLES31.glBindAttribLocation(shaderProgramObject,GLESMacros.GAB_ATTRIBUTE_VELOCITY,"Velocity");
		GLES31.glBindAttribLocation(shaderProgramObject,GLESMacros.GAB_ATTRIBUTE_START_TIME,"StartTime");



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

		mvpUniform = GLES31.glGetUniformLocation(shaderProgramObject,"u_mvp_matrix");
		startTimeUniform = GLES31.glGetUniformLocation(shaderProgramObject,"Time");
		backgroundUniform = GLES31.glGetUniformLocation(shaderProgramObject,"Background");


		final float triangleVertices[] = new float[]
		{
			 0.0f,1.0f,0.0f,
			-1.0f,-1.0f,0.0f,
			 1.0f,-1.0f,0.0f
		};	

		createPoints(500,500);
		
		//enable depth testing


		GLES31.glEnable(GLES31.GL_DEPTH_TEST);
		GLES31.glDepthFunc(GLES31.GL_LEQUAL);
		GLES31.glEnable(GLES31.GL_CULL_FACE);


		final float bg[] = new float[] {0.0f,0.0f,0.0f,1.0f};
		GLES31.glUniform1fv(backgroundUniform,1,bg,0);

		//set background color
		GLES31.glClearColor(0.0f,0.0f,0.0f,1.0f);

		Matrix.setIdentityM(perspectiveProjectionMatrix,0);

	}


	private void createPoints(int width , int height)
	{
		float i,j;
		Random rand = new Random();


		GLES31.glGenVertexArrays(1,vao,0);
		GLES31.glBindVertexArray(vao[0]);

		//create c like buffer
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(width*height*3*4);
		//set order of bytes (endianness and all)
		byteBuffer.order(ByteOrder.nativeOrder());
		//float type of buffer
		FloatBuffer vertsBuffer = byteBuffer.asFloatBuffer();

		//create c like buffer
		byteBuffer = ByteBuffer.allocateDirect(width*height*3*4);
		//set order of bytes (endianness and all)
		byteBuffer.order(ByteOrder.nativeOrder());
		//float type of buffer
		FloatBuffer colorsBuffer = byteBuffer.asFloatBuffer();
		
		//create c like buffer
	    byteBuffer = ByteBuffer.allocateDirect(width*height*3*4);
		//set order of bytes (endianness and all)
		byteBuffer.order(ByteOrder.nativeOrder());
		//float type of buffer
		FloatBuffer velocitiesBuffer = byteBuffer.asFloatBuffer();
		
			//create c like buffer
		byteBuffer = ByteBuffer.allocateDirect(width*height*4);
		//set order of bytes (endianness and all)
		byteBuffer.order(ByteOrder.nativeOrder());
		//float type of buffer
		FloatBuffer startTimeBuffer = byteBuffer.asFloatBuffer();
		

		for(i = 0.5f / width - 0.5f; i < 0.5f ; i = i + 1.0f / width )
		{
			for(j= 0.5f / height - 0.5f ; j < 0.5f ; j = j + 1.0f /height )
			{
				vertsBuffer.put(i-0.5f);
				vertsBuffer.put(-1.0f);
				vertsBuffer.put(j);

				colorsBuffer.put( (rand.nextFloat()) * 0.5f + 0.5f);
				colorsBuffer.put( (rand.nextFloat()) * 0.5f + 0.5f);
				colorsBuffer.put( (rand.nextFloat()) * 0.5f + 0.5f);
			
				velocitiesBuffer.put( (rand.nextFloat()) + 3.0f );
				velocitiesBuffer.put( (rand.nextFloat()) * 10.0f);
				velocitiesBuffer.put( (rand.nextFloat()) + 3.0f);
			
				startTimeBuffer.put( (rand.nextFloat()) * 10.0f);
			}
		}

	
		//start from
		vertsBuffer.position(0);		
		//start from
		colorsBuffer.position(0);
		//start from
		velocitiesBuffer.position(0);
		//start from
		startTimeBuffer.position(0);

		array_height = height;
		array_width = width;
	

		GLES31.glGenBuffers(1,vbo_verts,0);
		GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER,vbo_verts[0]);
		GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER,width*height*3,vertsBuffer,GLES31.GL_STATIC_DRAW);
		GLES31.glVertexAttribPointer(GLESMacros.GAB_ATTRIBUTE_VERTEX,3,GLES31.GL_FLOAT,false,0,0);
		GLES31.glEnableVertexAttribArray(GLESMacros.GAB_ATTRIBUTE_VERTEX);
		GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER,0);
		
		GLES31.glGenBuffers(1,vbo_colors,0);
		GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER,vbo_colors[0]);
		GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER,width*height*3,colorsBuffer,GLES31.GL_STATIC_DRAW);
		GLES31.glVertexAttribPointer(GLESMacros.GAB_ATTRIBUTE_COLOR,3,GLES31.GL_FLOAT,false,0,0);
		GLES31.glEnableVertexAttribArray(GLESMacros.GAB_ATTRIBUTE_COLOR);
		GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER,0);
		
		GLES31.glGenBuffers(1,vbo_velocities,0);
		GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER,vbo_velocities[0]);
		GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER,width*height*3,velocitiesBuffer,GLES31.GL_STATIC_DRAW);
		GLES31.glVertexAttribPointer(GLESMacros.GAB_ATTRIBUTE_VELOCITY,3,GLES31.GL_FLOAT,false,0,0);
		GLES31.glEnableVertexAttribArray(GLESMacros.GAB_ATTRIBUTE_VELOCITY);
		GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER,0);
		
		GLES31.glGenBuffers(1,vbo_start_times,0);
		GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER,vbo_start_times[0]);
		GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER,width*height,startTimeBuffer,GLES31.GL_STATIC_DRAW);
		GLES31.glVertexAttribPointer(GLESMacros.GAB_ATTRIBUTE_START_TIME,3,GLES31.GL_FLOAT,false,0,0);
		GLES31.glEnableVertexAttribArray(GLESMacros.GAB_ATTRIBUTE_START_TIME);
		GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER,0);
	
		GLES31.glBindVertexArray(0);
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

	private void display()
	{
		//code
		GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT | GLES31.GL_DEPTH_BUFFER_BIT);

		//use shader program
		GLES31.glUseProgram(shaderProgramObject);

		//opengl es drawing
		float modelViewMatrix[] = new float[16];
		float modelViewProjectionMatrix[] = new float[16];


		Matrix.setIdentityM(modelViewMatrix,0);
		Matrix.setIdentityM(modelViewProjectionMatrix,0);
		Matrix.translateM(modelViewMatrix,0,0.0f,0.0f,-3.0f);
		Matrix.multiplyMM(modelViewProjectionMatrix,0,perspectiveProjectionMatrix,0,modelViewMatrix,0);

		GLES31.glUniformMatrix4fv(mvpUniform,1,false,modelViewProjectionMatrix,0);

		ParticleTime += 0.001f;
		GLES31.glUniform1f(startTimeUniform,ParticleTime);

		//bind vao
		GLES31.glBindVertexArray(vao[0]);

		GLES31.glDrawArrays(GLES31.GL_POINTS,0,array_width*array_height);

		GLES31.glBindVertexArray(0);
		GLES31.glUseProgram(0);

		requestRender();
	}

	private void uninitialize()
	{
		if(vao[0] != 0)
		{
			GLES31.glDeleteVertexArrays(1,vao,0);
			vao[0] = 0;
		}

		if(vbo_verts[0] != 0)
		{
			GLES31.glDeleteBuffers(1,vbo_verts,0);
			vbo_verts[0] = 0;
		}



		if(vbo_colors[0] != 0)
		{
			GLES31.glDeleteBuffers(1,vbo_colors,0);
			vbo_colors[0] = 0;
		}

		if(vbo_velocities[0] != 0)
		{
			GLES31.glDeleteBuffers(1,vbo_velocities,0);
			vbo_velocities[0] = 0;
		}

		if(vbo_start_times[0] != 0)
		{
			GLES31.glDeleteBuffers(1,vbo_start_times,0);
			vbo_start_times	[0] = 0;
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










