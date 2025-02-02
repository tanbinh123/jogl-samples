/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_420;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import glm.glm;
import glm.mat._4.Mat4;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glf.Vertex_v2fv2f;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import glm.vec._2.Vec2;
import java.nio.IntBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_420_atomic_counter extends Test {

    public static void main(String[] args) {
        Gl_420_atomic_counter gl_420_atomic_counter = new Gl_420_atomic_counter();
    }

    public Gl_420_atomic_counter() {
        super("gl-420-atomic-counter", Profile.CORE, 4, 2, new Vec2(Math.PI * 0.0f));
    }

    private final String SHADERS_SOURCE = "atomic-counter";
    private final String SHADERS_ROOT = "src/data/gl_420";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * Vertex_v2fv2f.SIZE;
    private float[] vertexData = {
        -1.0f, -1.0f,/**/ 0.0f, 1.0f,
        +3.0f, -1.0f,/**/ 1.0f, 1.0f,
        +3.0f, +3.0f,/**/ 1.0f, 0.0f,
        -1.0f, +3.0f,/**/ 0.0f, 0.0f};

    private int elementCount = 3;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 3, //2, 3, 1
    };

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int TRANSFORM = 2;
        public static final int ATOMIC_COUNTER = 3;
        public static final int MAX = 4;
    }

    private IntBuffer pipelineName = GLBuffers.newDirectIntBuffer(1), vertexArrayName = GLBuffers.newDirectIntBuffer(1),
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX);
    private int programName;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        if (validated) {
            validated = initBuffer(gl4);
        }
        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT, null,
                    SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT, null,
                    SHADERS_SOURCE, "frag", null, true);

            shaderProgram.init(gl4);
            programName = shaderProgram.program();

            gl4.glProgramParameteri(programName, GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glGenProgramPipelines(1, pipelineName);
            gl4.glBindProgramPipeline(pipelineName.get(0));
            gl4.glUseProgramStages(pipelineName.get(0), GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        boolean validated = true;

        int[] maxVertexAtomicCounterBuffers = {0};
        int[] maxControlAtomicCounterBuffers = {0};
        int[] maxEvaluationAtomicCounterBuffers = {0};
        int[] maxGeometryAtomicCounterBuffers = {0};
        int[] maxFragmentAtomicCounterBuffers = {0};
        int[] maxCombinedAtomicCounterBuffers = {0};

        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);

        gl4.glGetIntegerv(GL_MAX_VERTEX_ATOMIC_COUNTER_BUFFERS, maxVertexAtomicCounterBuffers, 0);
        gl4.glGetIntegerv(GL_MAX_TESS_CONTROL_ATOMIC_COUNTER_BUFFERS, maxControlAtomicCounterBuffers, 0);
        gl4.glGetIntegerv(GL_MAX_TESS_EVALUATION_ATOMIC_COUNTER_BUFFERS, maxEvaluationAtomicCounterBuffers, 0);
        gl4.glGetIntegerv(GL_MAX_GEOMETRY_ATOMIC_COUNTER_BUFFERS, maxGeometryAtomicCounterBuffers, 0);
        gl4.glGetIntegerv(GL_MAX_FRAGMENT_ATOMIC_COUNTER_BUFFERS, maxFragmentAtomicCounterBuffers, 0);
        gl4.glGetIntegerv(GL_MAX_COMBINED_ATOMIC_COUNTER_BUFFERS, maxCombinedAtomicCounterBuffers, 0);

        gl4.glGenBuffers(Buffer.MAX, bufferName);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
        gl4.glBufferData(GL_UNIFORM_BUFFER, Mat4.SIZE, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, bufferName.get(Buffer.ATOMIC_COUNTER));
        gl4.glBufferData(GL_ATOMIC_COUNTER_BUFFER, Integer.BYTES, null, GL_DYNAMIC_COPY);
        gl4.glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, 0);

        BufferUtils.destroyDirectBuffer(elementBuffer);
        BufferUtils.destroyDirectBuffer(vertexBuffer);

        return validated;
    }

    private boolean initVertexArray(GL4 gl4) {

        boolean validated = true;

        gl4.glGenVertexArrays(1, vertexArrayName);
        gl4.glBindVertexArray(vertexArrayName.get(0));
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vertex_v2fv2f.SIZE, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, Vertex_v2fv2f.SIZE, Vec2.SIZE);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);
        }
        gl4.glBindVertexArray(0);

        return validated;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        // Setup blending
        gl4.glEnable(GL_BLEND);
        gl4.glBlendEquation(GL_FUNC_ADD);
        gl4.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
            ByteBuffer pointer = gl4.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, Mat4.SIZE,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspective_((float) Math.PI * 0.1f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            Mat4 model = new Mat4(1.0f);
            Mat4 mvp = projection.mul(viewMat4()).mul(model);

            mvp.toDbb(pointer);

            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        {
            gl4.glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, bufferName.get(Buffer.ATOMIC_COUNTER));
            ByteBuffer pointer = gl4.glMapBufferRange(
                    GL_ATOMIC_COUNTER_BUFFER, 0, Integer.BYTES,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            pointer.putInt(0, 0);

            gl4.glUnmapBuffer(GL_ATOMIC_COUNTER_BUFFER);
        }

        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);
        gl4.glClearBufferfv(GL_COLOR, 0, clearColor.put(0, 0).put(1, 0).put(2, 0).put(3, 1));

        gl4.glBindProgramPipeline(pipelineName.get(0));
        gl4.glBindVertexArray(vertexArrayName.get(0));
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        gl4.glBindBufferBase(GL_ATOMIC_COUNTER_BUFFER, 0, bufferName.get(Buffer.ATOMIC_COUNTER));
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName.get(Buffer.TRANSFORM));

        gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteBuffers(Buffer.MAX, bufferName);
        gl4.glDeleteProgram(programName);
        gl4.glDeleteVertexArrays(1, vertexArrayName);
        gl4.glDeleteProgramPipelines(1, pipelineName);

        BufferUtils.destroyDirectBuffer(bufferName);
        BufferUtils.destroyDirectBuffer(vertexArrayName);
        BufferUtils.destroyDirectBuffer(pipelineName);

        return true;
    }
}
