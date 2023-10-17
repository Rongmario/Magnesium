package me.jellysquid.mods.sodium.compat.client.renderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public final class CompatGlStateManager {

    public static void bufferData(int p_227655_0_, ByteBuffer p_227655_1_, int p_227655_2_){
        GL15.glBufferData(p_227655_0_, p_227655_1_, p_227655_2_);
    }

    public static void bindBuffers(int p_227724_0_, int p_227724_1_){
        GL15.glBindBuffer(p_227724_0_,p_227724_1_);
    }

    public static void deleteBuffers(int p_227732_0_){
        GL15.glDeleteBuffers(p_227732_0_);
    }

    public static void useProgram(int program){
        GL20.glUseProgram(program);
    }

    public static int getUniformLocation(int program, CharSequence name){
        return GL20.glGetUniformLocation(program,name);
    }

    public static void deleteProgram(int program){
        GL20.glDeleteProgram(program);
    }

    public static int createProgram(){
        return GL20.glCreateProgram();
    }

    public static void attachShader(int p_227704_0_, int p_227704_1_){
        GL20.glAttachShader(p_227704_0_,p_227704_1_);
    }
    public static void linkProgram(int p_227729_0_){
        GL20.glLinkProgram(p_227729_0_);
    }

    public static int getProgram(int p_227691_0_, int p_227691_1_){
        return GL20.glGetProgrami(p_227691_0_, p_227691_1_);
    }
    public static int createShader(int type){
        return GL20.glCreateShader(type);
    }
    public static void compileShader(int shader){
        GL20.glCompileShader(shader);
    }

    public static int getShader(int p_227712_0_, int p_227712_1_){
        return GL20.glGetShaderi(p_227712_0_,p_227712_1_);
    }

    public static void deleteShader(int shader){
        GL20.glDeleteShader(shader);
    }

    public static int getInteger(int a){
        return GL11.glGetInteger(a);
    }

    public static void vertexAttribPointer(int p_227651_0_, int p_227651_1_, int p_227651_2_, boolean p_227651_3_, int p_227651_4_, long p_227651_5_){
        GL20.glVertexAttribPointer(p_227651_0_, p_227651_1_, p_227651_2_, p_227651_3_, p_227651_4_, p_227651_5_);
    }

    public static void enableVertexAttribArray(int p_227606_0_){
        GL20.glEnableVertexAttribArray(p_227606_0_);
    }

    public static int genBuffers(){
        return GL15.glGenBuffers();
    }

    public static void uniform1(int p_227718_0_, int p_227718_1_){
        GL20.glUniform1i(p_227718_0_,p_227718_1_);
    }

    public static void uniformMatrix4(int p_227698_0_, boolean p_227698_1_, FloatBuffer p_227698_2_){
        GL20.glUniformMatrix4(p_227698_0_,p_227698_1_,p_227698_2_);
    }
}
