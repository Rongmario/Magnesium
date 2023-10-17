package me.jellysquid.mods.sodium.client.util.math;

import me.jellysquid.mods.sodium.client.util.Norm3b;
import me.jellysquid.mods.sodium.compat.util.math.Direction;
import me.jellysquid.mods.sodium.compat.util.math.Matrix3f;
import me.jellysquid.mods.sodium.compat.util.math.Matrix4f;

public class MatrixUtil {
    public static int computeNormal(Matrix3f normalMatrix, Direction facing) {
        return ((Matrix3fExtended) normalMatrix).computeNormal(facing);
    }

    public static Matrix4fExtended getExtendedMatrix(Matrix4f matrix) {
        return (Matrix4fExtended) matrix;
    }

    public static Matrix3fExtended getExtendedMatrix(Matrix3f matrix) {
        return (Matrix3fExtended) matrix;
    }

    public static int transformPackedNormal(int norm, Matrix3f matrix) {
        Matrix3fExtended mat = MatrixUtil.getExtendedMatrix(matrix);

        float normX1 = Norm3b.unpackX(norm);
        float normY1 = Norm3b.unpackY(norm);
        float normZ1 = Norm3b.unpackZ(norm);

        float normX2 = mat.transformVecX(normX1, normY1, normZ1);
        float normY2 = mat.transformVecY(normX1, normY1, normZ1);
        float normZ2 = mat.transformVecZ(normX1, normY1, normZ1);

        return Norm3b.pack(normX2, normY2, normZ2);
    }
}
