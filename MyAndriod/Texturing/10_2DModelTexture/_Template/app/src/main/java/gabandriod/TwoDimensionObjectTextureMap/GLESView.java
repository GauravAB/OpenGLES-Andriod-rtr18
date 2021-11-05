package com.gabandroid.TwoDimensionalTexturemap;

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

//for texture mapping
import android.graphics.BitmapFactory;		//texture factory
import android.graphics.Bitmap;				//for png image
import android.opengl.GLUtils;				//for texImage2D()


public class GLESView extends GLSurfaceView implements GLSurfaceView.Renderer,OnGestureListener,OnDoubleTapListener
{

	private final Context context;
	private GestureDetector gestureDetector;

	private int vertexShaderObject;
	private int fragmentShaderObject;
	private int shaderProgramObject;

	//vertex array objects
	private int[] vao_square = new int[1];
	
	//pyramid vertex buffers
	private int[] vbo_square_pos = new int[1];
	private int[] vbo_square_texture = new int[1];

	
	//uniform variables to manipulate data
	private int mvpUniform;
	private int texture0_sampler_uniform;

	//texture variables
	private int[] texture_smiley = new int[1];

	//matrix for perpective projection 
	private float perspectiveProjectionMatrix[] = new float[16];

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
			"in vec4 vPosition;"+
			"in vec2 vTextureCoord;"+ 
			"out vec2 fTextureCoord;"+
			"uniform mat4 u_mvp_matrix;"+
			"void main(void)"+
			"{"+
			"gl_Position = u_mvp_matrix * vPosition;"+
			"fTextureCoord = vTextureCoord;"+
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
			"in vec2 fTextureCoord;"+
			"out vec4 FragColor;"+
			"uniform highp sampler2D u_texture0_sampler;"+
			"void main(void)"+
			"{"+
			"FragColor = texture(u_texture0_sampler,fTextureCoord);"+
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
		GLES31.glBindAttribLocation(shaderProgramObject,GLESMacros.GAB_ATTRIBUTE_TEXTURE0,"vTextureCoord");

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
		texture0_sampler_uniform = GLES31.glGetUniformLocation(shaderProgramObject,"u_texture0_sampler");

		//load textures
		texture_smiley[0] = loadGLTexture(R.raw.smiley);

		

		final float squareVertices[] = new float[]
		{
			1.0f,1.0f,0.0f,
			-1.0f,1.0f,0.0f,
			-1.0f,-1.0f,0.0f,
			1.0f,-1.0f,0.0f,
		};	


		final float squareTextures[] = new float []
		{
			0.0f,0.0f,
			1.0f,0.0f,
			1.0f,1.0f,
			0.0f,1.0f,
		};



		//---------------------------VAO SQUARE START-----------------------------------------------
		GLES31.glGenVertexArrays(1,vao_square,0);
		GLES31.glBindVertexArray(vao_square[0]);

		//position buffer-----------------------------------------------------------------------------
		GLES31.glGenBuffers(1,vbo_square_pos,0);
		GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER,vbo_square_pos[0]);
		//create c like buffer
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(squareVertices.length*4);
		//set order of bytes (endianness and all)
		byteBuffer.order(ByteOrder.nativeOrder());
		//float type of buffer
		FloatBuffer verticesBufferSquare = byteBuffer.asFloatBuffer();
		//data inside buffer
		verticesBufferSquare.put(squareVertices);
		//start from
		verticesBufferSquare.position(0);
		
		GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER,squareVertices.length*4,verticesBufferSquare,GLES31.GL_STATIC_DRAW);
		GLES31.glVertexAttribPointer(GLESMacros.GAB_ATTRIBUTE_VERTEX,3,GLES31.GL_FLOAT,false,0,0);
		GLES31.glEnableVertexAttribArray(GLESMacros.GAB_ATTRIBUTE_VERTEX);
		GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER,0);

		//position buffer end------------------------------------------------------------------------------

		GLES31.glGenBuffers(1,vbo_square_texture,0);
		GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER,vbo_square_texture[0]);

		byteBuffer = ByteBuffer.allocateDirect(squareTextures.length*4);
		byteBuffer.order(ByteOrder.nativeOrder());
		FloatBuffer textureBufferSquare = byteBuffer.asFloatBuffer();
		textureBufferSquare.put(squareTextures);
		textureBufferSquare.position(0);

		GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER,squareTextures.length*4,textureBufferSquare,GLES31.GL_STATIC_DRAW);
		GLES31.glVertexAttribPointer(GLESMacros.GAB_ATTRIBUTE_TEXTURE0,2,GLES31.GL_FLOAT,false,0,0);
		GLES31.glEnableVertexAttribArray(GLESMacros.GAB_ATTRIBUTE_TEXTURE0);
		GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER,0);

		GLES31.glBindVertexArray(0);

		//---------------------------VAO square END----------------------------------------------------

		//enable depth testing
		GLES31.glEnable(GLES31.GL_DEPTH_TEST);
		GLES31.glDepthFunc(GLES31.GL_LEQUAL);

		//set background color
		GLES31.glClearColor(1.0f,1.0f,1.0f,1.0f);

		Matrix.setIdentityM(perspectiveProjectionMatrix,0);

	}

	private int loadGLTexture(int imageFileResourceID)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		
		//keep default scale , no explicit scaling
		options.inScaled = false;	//turning explicit scaling off

		//read in the resource
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),imageFileResourceID,options);
		int[] texture  = new int[1];

		//create texture object to apply to model
		GLES31.glGenTextures(1,texture,0);
		GLES31.glPixelStorei(GLES31.GL_UNPACK_ALIGNMENT,1);

		//bind with the texture
		GLES31.glBindTexture(GLES31.GL_TEXTURE_2D,texture[0]);

		//setup filter and wrap modes for texture
		GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D,GLES31.GL_TEXTURE_MAG_FILTER,GLES31.GL_LINEAR);
		GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D,GLES31.GL_TEXTURE_MIN_FILTER,GLES31.GL_LINEAR_MIPMAP_LINEAR);
		
		//load the bitmap into the bound texture
		GLUtils.texImage2D(GLES31.GL_TEXTURE_2D,0,bitmap,0);

		//generate mipmap
		GLES31.glGenerateMipmap(GLES31.GL_TEXTURE_2D);

		return(texture[0]);

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

		//--------------------------------------------------------------------------------------------------
		//triangle
		float modelViewMatrix[] = new float[16];
		float modelViewProjectionMatrix[] = new float[16];


		Matrix.setIdentityM(modelViewMatrix,0);
		Matrix.setIdentityM(modelViewProjectionMatrix,0);
		Matrix.translateM(modelViewMatrix,0,0.0f,0.0f,-3.0f);	
		Matrix.multiplyMM(modelViewProjectionMatrix,0,perspectiveProjectionMatrix,0,modelViewMatrix,0);

		GLES31.glUniformMatrix4fv(mvpUniform,1,false,modelViewProjectionMatrix,0);

		//bind vao
		GLES31.glBindVertexArray(vao_square[0]);
		//activate and bind texture
		GLES31.glActiveTexture(GLES31.GL_TEXTURE0);
		GLES31.glBindTexture(GLES31.GL_TEXTURE_2D,texture_smiley[0]);
		GLES31.glUniform1i(texture0_sampler_uniform,0);

		GLES31.glDrawArrays(GLES31.GL_TRIANGLE_FAN,0,4);
		GLES31.glBindVertexArray(0);
		//-----------------------------------------------------------------------------------------------------
		
		GLES31.glUseProgram(0);

		requestRender();
	}

	private void uninitialize()
	{
		if(vao_square[0] != 0)
		{
			GLES31.glDeleteVertexArrays(1,vao_square,0);
			vao_square[0] = 0;
		}

		if(vbo_square_pos[0] != 0)
		{
			GLES31.glDeleteBuffers(1,vbo_square_pos,0);
			vbo_square_pos[0] = 0;
		}

		if(vbo_square_texture[0] != 0)
		{
			GLES31.glDeleteBuffers(1,vbo_square_texture,0);
			vbo_square_texture[0] = 0;
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










