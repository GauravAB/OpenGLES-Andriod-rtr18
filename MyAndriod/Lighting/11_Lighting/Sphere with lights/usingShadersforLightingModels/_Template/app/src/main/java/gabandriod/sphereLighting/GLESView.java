package com.gabandroid.sphereLighting;

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
//for sphere
import java.nio.ShortBuffer;
//for math variables and calculations
import android.opengl.Matrix;


public class GLESView extends GLSurfaceView implements GLSurfaceView.Renderer,OnGestureListener,OnDoubleTapListener
{

	private final Context context;
	private GestureDetector gestureDetector;

	private int vertexShaderObject;
	private int fragmentShaderObject;
	private int shaderProgramObject;

	private int[] model_matrix_uniform = new int[1];
	private int[] view_matrix_uniform = new int[1];
	private int[] projection_matrix_uniform = new int[1];

	private int[] vao_sphere = new int[1];
	private int[] vbo_sphere_position = new int[1];
	private int[] vbo_sphere_normal = new int[1];
	private int[] vbo_sphere_element = new int[1];

	private int numVertices;
	private int numElements;



	//light variables
	private int phong_uniform;

	private int[] La_uniform = new int[1];
	private int[] Ld_uniform = new int[1];
	private int[] Ls_uniform = new int[1];
	private int[] light_position_uniform = new int[1];

	private int[] Ka_uniform = new int[1];
	private int[] Kd_uniform = new int[1];
	private int[] Ks_uniform = new int[1];
	private int[] material_shininess_uniform = new int[1];

	private boolean lighting;
	private int[] light_enable_uniform = new int[1];
	private int light_model_toggle = 1;

	private float light_ambient[] = new float[]  {0.0f,0.0f,0.0f,1.0f};
	private float light_diffuse[] = new float[]  {1.0f,1.0f,1.0f,1.0f};
	private float light_specular[] = new float[] {1.0f,1.0f,1.0f,1.0f};
	private float light_position[] = new float[] {100.0f,100.0f,100.0f,1.0f};

	private float material_ambient[] = new float[] {0.0f,0.0f,0.0f,1.0f};
	private float material_diffuse[] = new float[] {1.0f,1.0f,1.0f,1.0f};
	private float material_specular[] = new float[]{1.0f,1.0f,1.0f,1.0f};
	private float material_shininess = 50.0f;


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
		if(lighting == false)
		{
			lighting = true;
		}
		else
		{
			lighting = false;

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
		if(light_model_toggle == 1)
		{
			light_model_toggle = 0;
		}
		else
		{
			light_model_toggle = 1;
		}
		return (true);
	}

	private void initialize(GL10 gl)
	{
		//Vertex Shader
		System.out.println("GAB: Inside Initialize");

		//create shader
		vertexShaderObject = GLES31.glCreateShader(GLES31.GL_VERTEX_SHADER);
		System.out.println("GAB: Created Vertex Shader light model");

		//vertex shader source code
	    final String vertexShaderSourceCode = String.format
		(
			"#version 310 es"+
			"\n"+
			"in vec4 vPosition;"+
			"in vec3 vNormal;"+
			"out vec3 light_direction;"+
			"out vec3 transformed_normals;"+
			"out vec3 viewer_vector;"+
			"uniform mat4 u_model_matrix;"+
			"uniform mat4 u_view_matrix;"+
			"uniform mat4 u_projection_matrix;"+
			"uniform vec4 u_Lp;"+
			"uniform mediump int u_phong;"+
			"uniform  mediump int u_lighting_enabled;"+
			"uniform vec3 u_La;"+
			"uniform vec3 u_Ld;"+
			"uniform vec3 u_Ls;"+
			"uniform vec3 u_Ka;"+
			"uniform vec3 u_Kd;"+
			"uniform vec3 u_Ks;"+
			"uniform float u_material_shininess;"+
			"out vec3 phong_ads_color;"+
			"void main(void)"+
			"{"+
			"if (u_lighting_enabled == 1)"+
			"{"+
			"if(u_phong == 1)"+
			"{"+
			"vec4 eye_coordinates = u_view_matrix * u_model_matrix * vPosition;"+
			"transformed_normals = mat3(u_view_matrix * u_model_matrix) * vNormal;"+
			"light_direction = vec3(u_Lp) - eye_coordinates.xyz;"+
			"viewer_vector = -eye_coordinates.xyz;"+
			"}"+
			"else"+
			"{"+
			"vec4 eye_coordinates = u_view_matrix * u_model_matrix * vPosition;"+
			"vec3 normalized_transformed_normals = normalize(mat3(u_view_matrix * u_model_matrix) * vNormal);"+
			"vec3 normalized_light_direction = normalize(vec3(u_Lp) - eye_coordinates.xyz);"+
			"float tn_dot_ld = max(dot(normalized_transformed_normals,normalized_light_direction),0.0f);"+
			"vec3 ambient = u_La * u_Ka;"+
			"vec3 diffuse = u_Ld * u_Kd * tn_dot_ld;"+
			"vec3 reflection_vector = reflect(-normalized_light_direction,normalized_transformed_normals);"+
			"vec3 normalized_viewer_vector = normalize(-eye_coordinates.xyz);"+
			"vec3 specular = u_Ls * u_Ks * pow(max(dot(reflection_vector,normalized_viewer_vector),0.0),u_material_shininess);"+
			"phong_ads_color = ambient + diffuse + specular;"+
			"}"+
			"}"+
			"gl_Position = u_projection_matrix * u_view_matrix * u_model_matrix * vPosition;"+
			"}"
		);

		//provide source code to shader
		GLES31.glShaderSource(vertexShaderObject,vertexShaderSourceCode);

		System.out.println("GAB: vertex Shader Object for phong lighting created");

		System.out.println("GAB: compiling...");

		//compileShader & check for errors
		GLES31.glCompileShader(vertexShaderObject);

		//reinitialize
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
			"in vec3 light_direction;"+
			"in vec3 transformed_normals;"+
			"in vec3 viewer_vector;"+
			"in vec3 phong_ads_color;"+
			"out vec4 FragColor;"+
			"uniform int u_lighting_enabled;"+
			"uniform int u_phong;"+
			"uniform vec3 u_La;"+
			"uniform vec3 u_Ld;"+
			"uniform vec3 u_Ls;"+
			"uniform vec3 u_Ka;"+
			"uniform vec3 u_Kd;"+
			"uniform vec3 u_Ks;"+
			"uniform float u_material_shininess;"+
			"void main(void)"+
			"{"+
			"vec3 phong_ads_color_local;"+
			"if(u_lighting_enabled == 1)"+
			"{"+
			"if(u_phong == 1)"+
			"{"+
			"vec3 normalized_transformed_normals = normalize(transformed_normals);"+
			"vec3 normalized_light_direction = normalize(light_direction);"+
			"float tn_dot_ld = max(dot(normalized_transformed_normals,normalized_light_direction),0.0f);"+
			"vec3 ambient = u_La * u_Ka;"+
			"vec3 diffuse = u_Ld * u_Kd * tn_dot_ld;"+
			"vec3 reflection_vector = reflect(-normalized_light_direction,normalized_transformed_normals);"+
			"vec3 normalized_viewer_vector = normalize(viewer_vector);"+
			"vec3 specular = u_Ls * u_Ks * pow(max(dot(reflection_vector,normalized_viewer_vector),0.0),u_material_shininess);"+
			"phong_ads_color_local = ambient + diffuse + specular;"+
			"}"+
			"else"+
			"{"+
			"phong_ads_color_local = phong_ads_color;"+
			"}"+
			"}"+
			"else"+
			"{"+
			"phong_ads_color_local = vec3(1.0f,1.0f,1.0f);"+
			"}"+
			"FragColor = vec4(phong_ads_color_local,0.0f);"+
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
		int[] iShaderProgramLinkStatus = new int[1];
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

		model_matrix_uniform[0]= GLES31.glGetUniformLocation(shaderProgramObject,"u_model_matrix");
		view_matrix_uniform[0] = GLES31.glGetUniformLocation(shaderProgramObject,"u_view_matrix");
		projection_matrix_uniform[0] = GLES31.glGetUniformLocation(shaderProgramObject,"u_projection_matrix");

		La_uniform[0] = GLES31.glGetUniformLocation(shaderProgramObject,"u_La");
		Ls_uniform[0] = GLES31.glGetUniformLocation(shaderProgramObject,"u_Ls");
		Ld_uniform[0] = GLES31.glGetUniformLocation(shaderProgramObject,"u_Ld");
		light_position_uniform[0] = GLES31.glGetUniformLocation(shaderProgramObject,"u_Lp");

		Ka_uniform[0] = GLES31.glGetUniformLocation(shaderProgramObject,"u_Ka");
		Kd_uniform[0] = GLES31.glGetUniformLocation(shaderProgramObject,"u_Kd");
		Ks_uniform[0] = GLES31.glGetUniformLocation(shaderProgramObject,"u_Ks");
		material_shininess_uniform[0] = GLES31.glGetUniformLocation(shaderProgramObject,"u_material_shininess");

		light_enable_uniform[0] = GLES31.glGetUniformLocation(shaderProgramObject,"u_lighting_enabled");

		phong_uniform = GLES31.glGetUniformLocation(shaderProgramObject,"u_phong");



		Sphere sphere = new Sphere();
		float sphere_vertices[] = new float[1146];
		float sphere_normals[] = new float[1146];
		float sphere_textures[] = new float[764];
		short sphere_elements[] = new short[2280];

		sphere.getSphereVertexData(sphere_vertices,sphere_normals,sphere_textures,sphere_elements);

		numVertices = sphere.getNumberOfSphereVertices();
		numElements = sphere.getNumberOfSphereElements();


		GLES31.glGenVertexArrays(1,vao_sphere,0);
		GLES31.glBindVertexArray(vao_sphere[0]);

		GLES31.glGenBuffers(1,vbo_sphere_position,0);
		GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER,vbo_sphere_position[0]);
		//create c like buffer
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(sphere_vertices.length*4);
		//set order of bytes (endianness and all)
		byteBuffer.order(ByteOrder.nativeOrder());
		//float type of buffer
		FloatBuffer verticesBuffer = byteBuffer.asFloatBuffer();
		//data inside buffer
		verticesBuffer.put(sphere_vertices);
		//start from
		verticesBuffer.position(0);
		GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER,sphere_vertices.length*4,verticesBuffer,GLES31.GL_STATIC_DRAW);
		GLES31.glVertexAttribPointer(GLESMacros.GAB_ATTRIBUTE_VERTEX,3,GLES31.GL_FLOAT,false,0,0);
		GLES31.glEnableVertexAttribArray(GLESMacros.GAB_ATTRIBUTE_VERTEX);
		GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER,0);

		GLES31.glGenBuffers(1,vbo_sphere_normal,0);
		GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER,vbo_sphere_normal[0]);
        //create c like buffer
	    byteBuffer = ByteBuffer.allocateDirect(sphere_normals.length*4);
		//set order of bytes (endianness and all)
		byteBuffer.order(ByteOrder.nativeOrder());
		//float type of buffer
		FloatBuffer normalsBuffer = byteBuffer.asFloatBuffer();
		//data inside buffer
		normalsBuffer.put(sphere_normals);
		//start from
		normalsBuffer.position(0);

		GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER,sphere_normals.length*4,normalsBuffer,GLES31.GL_STATIC_DRAW);
		GLES31.glVertexAttribPointer(GLESMacros.GAB_ATTRIBUTE_NORMAL,3,GLES31.GL_FLOAT,false,0,0);
		GLES31.glEnableVertexAttribArray(GLESMacros.GAB_ATTRIBUTE_NORMAL);
		GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER,0);

		byteBuffer = ByteBuffer.allocateDirect(sphere_elements.length*4);
		//set order of bytes (endianness and all)
		byteBuffer.order(ByteOrder.nativeOrder());
		//float type of buffer
		ShortBuffer elementsBuffer = byteBuffer.asShortBuffer();
		//data inside buffer
		elementsBuffer.put(sphere_elements);
		//start from
		elementsBuffer.position(0);
		
		//ELEMENTS VBO
		GLES31.glGenBuffers(1,vbo_sphere_element,0);
		GLES31.glBindBuffer(GLES31.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);

		GLES31.glBufferData(GLES31.GL_ELEMENT_ARRAY_BUFFER,sphere_elements.length*2,elementsBuffer,GLES31.GL_STATIC_DRAW);
		GLES31.glBindBuffer(GLES31.GL_ELEMENT_ARRAY_BUFFER,0);

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

	private void display()
	{
		//code
		GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT | GLES31.GL_DEPTH_BUFFER_BIT);

		GLES31.glUseProgram(shaderProgramObject);
		
		//opengl es drawing
		float modelMatrix[] = new float[16];
		float ViewMatrix[] = new float[16];
		Matrix.setIdentityM(modelMatrix,0);
		Matrix.setIdentityM(ViewMatrix,0);	
		
		
		Matrix.translateM(modelMatrix,0,0.0f,0.0f,-3.0f);
		Matrix.scaleM(modelMatrix,0,2.0f,2.0f,2.0f);
	
		GLES31.glUniformMatrix4fv(model_matrix_uniform[0],1,false,modelMatrix,0);
		GLES31.glUniformMatrix4fv(view_matrix_uniform[0],1,false,ViewMatrix,0);
		GLES31.glUniformMatrix4fv(projection_matrix_uniform[0],1,false,perspectiveProjectionMatrix,0);

		GLES31.glUniform1i(phong_uniform,light_model_toggle);

		if(lighting == true)
		{
			GLES31.glUniform1i(light_enable_uniform[0],1);
			GLES31.glUniform4fv(light_position_uniform[0],1,light_position,0);
			GLES31.glUniform3fv(La_uniform[0],1,light_ambient,0);
			GLES31.glUniform3fv(Ld_uniform[0],1,light_diffuse,0);
			GLES31.glUniform3fv(Ls_uniform[0],1,light_specular,0);
			
			GLES31.glUniform3fv(Ks_uniform[0],1,material_specular,0);
			GLES31.glUniform3fv(Ka_uniform[0],1,material_ambient,0);
			GLES31.glUniform3fv(Kd_uniform[0],1,material_diffuse,0);
			GLES31.glUniform1f(material_shininess_uniform[0],material_shininess);
		}
		else
		{
			GLES31.glUniform1i(light_enable_uniform[0],0);				
		}

		//bind vao
		GLES31.glBindVertexArray(vao_sphere[0]);
		GLES31.glBindBuffer(GLES31.GL_ELEMENT_ARRAY_BUFFER,vbo_sphere_element[0]);

		GLES31.glDrawElements(GLES31.GL_TRIANGLES,numElements,GLES31.GL_UNSIGNED_SHORT,0);

		GLES31.glBindVertexArray(0);
		GLES31.glUseProgram(0);

		requestRender();
	}

	private void uninitialize()
	{
		if(vao_sphere[0] != 0)
		{
			GLES31.glDeleteVertexArrays(1,vao_sphere,0);
			vao_sphere[0] = 0;
		}

		if(vbo_sphere_position[0] != 0)
		{
			GLES31.glDeleteBuffers(1,vbo_sphere_position,0);
			vbo_sphere_position[0] = 0;
		}
		
		if(vbo_sphere_element[0] != 0)
		{
			GLES31.glDeleteBuffers(1,vbo_sphere_element,0);
			vbo_sphere_element[0] = 0;
		}
		
		if(vbo_sphere_normal[0] != 0)
		{
			GLES31.glDeleteBuffers(1,vbo_sphere_normal,0);
			vbo_sphere_normal[0] = 0;
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










