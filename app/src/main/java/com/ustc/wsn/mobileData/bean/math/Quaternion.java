package com.ustc.wsn.mobileData.bean.math;

import java.io.Serializable;

import java.lang.Float;

/**
 * Self-contained and lightweight Java implementation of Quaternion. The class
 * supports a variety of Quaternion operations and functions
 *
 * @author Duy Nguyen-Truong (truongduy134@gmail.com)
 */
public class Quaternion implements Serializable {
    private float x;
    private float y;
    private float z;
    private float w;

    private  final long serialVersionUID = 2L;

    public  final float EPSILON = 0.00000000001f;
    public  final String VECTOR_INVALID_LENGTH_MSG = "Input vector must be an array of size 3";
    public  final String UNDEFINED_LOG_ZERO_QUATERNION_MSG = "Logarithm of zero quaternion is undefined";
    public  final String INVALID_INTERPOLATION_PARAM = "Interpolation parameter must be between 0 and 1 inclusively";

    //////////////////////////////////////////////////////////
    //
    // Constructors, setters and getters
    //
    /////////////////////////////////////////////////////////

    /**
     * Default Constructor. Constructs an identity Quaternion (0.0, 0.0, 0.0, 1.0)
     */
    public Quaternion() {
        this(0.0f, 0.0f, 0.0f, 1.0f);
    }

    /**
     * Constructs and initializes a Quaternion with 4 input parameters
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @param w the scalar component
     */
    public Quaternion(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    /**
     * Copy constructor
     *
     * @param another the Quaternion to be copied
     */
    public Quaternion(final Quaternion another) {
        this.x = another.x;
        this.y = another.y;
        this.z = another.z;
        this.w = another.w;
    }

    /**
     * Gets x-coordinate of this quaternion
     *
     * @return The x-coordinate of this quaternion
     */
    public float getX() {
        return x;
    }

    /**
     * Gets y-coordinate of this quaternion
     *
     * @return The y-coordinate of this quaternion
     */
    public float getY() {
        return y;
    }

    /**
     * Gets z-coordinate of this quaternion
     *
     * @return The z-coordinate of this quaternion
     */
    public float getZ() {
        return z;
    }

    /**
     * Gets w-component (scalar component) of this quaternion
     *
     * @return The w-component of this quaternion
     */
    public float getW() {
        return w;
    }

    /**
     * Gets the vector component (x, y, z) of this quaternion
     *
     * @return An array of size 3 representing the vector (x, y, z)
     */
    public float[] getVectorPart() {
        float[] vector = new float[]{this.x, this.y, this.z};
        return vector;
    }

    /**
     * Get scalar component (w-component) of this quaternion
     *
     * @return The scalar component (w-component) of this quaternion
     * @see #getW()
     */
    public float getScalarPart() {
        return w;
    }

    /**
     * Gets the angle (in degree) in the angle-axis representation of the rotation
     * that this Quaternion represents
     *
     * @return The angle (in degree) of the rotation
     */
    public float getAngle() {
        return radianToDegree(this.getAngleRad());
    }

    /**
     * Gets the angle (in radian) in the angle-axis representation of the rotation
     * that this Quaternion represents
     *
     * @return The angle (in radian) of the rotation
     */
    public float getAngleRad() {
        Quaternion unitQ = new Quaternion(this);
        unitQ.normalize();

        float x = unitQ.getX();
        float y = unitQ.getY();
        float z = unitQ.getZ();
        float vNorm = (float) Math.sqrt(x * x + y * y + z * z);

        return 2 * (float) Math.atan2(vNorm, unitQ.getW());
    }

    /**
     * Gets the vector axis in the angle-axis representation of the rotation that
     * this Quaternion represents
     *
     * @return A unit vector for the rotation axis, or a zero vector in
     * degenerate case
     */
    public float[] getRotationAxis() {
        float angleRad = this.getAngleRad();
        float norm = this.norm();

        float[] axis = new float[]{0.0f, 0.0f, 0.0f};
        if ((float) Math.abs(angleRad) > EPSILON) {
            float sinTerm = (float) Math.sin(angleRad / 2.0);
            axis[0] = this.x / (norm * sinTerm);
            axis[1] = this.y / (norm * sinTerm);
            axis[2] = this.z / (norm * sinTerm);
        }

        return axis;
    }

    //////////////////////////////////////////////////////////
    //
    // Truth methods for Quaternion
    //
    /////////////////////////////////////////////////////////

    /**
     * Checks if this Quaternion is an identity quaternion (0.0, 0.0, 0.0, 1.0)
     *
     * @return {@code true} if this Quaternion is an identity quaternion, or
     * {@code false} otherwise
     */
    public boolean isIdentity() {
        return (float) Math.abs(this.squaredNorm() - 1.0) < EPSILON && (float) Math.abs(this.w - 1.0) < EPSILON;
    }

    /**
     * Checks if this Quaternion is a unit quaternion
     *
     * @return {@code true} if this Quaternion is a unit quaternion, or
     * {@code false} otherwise
     */
    public boolean isUnit() {
        return (float) Math.abs(this.norm() - 1.0) < EPSILON;
    }

    /**
     * Checks if this Quaternion equals to the input Quaternion within the
     * specified tolerance threshold
     *
     * @param another   Another Quaternion for comparison
     * @param threshold A tolerance threshold value
     * @return {@code true} if two Quaternions are equal within the tolerance
     * threshold (that means corresponding components are equal within
     * the tolerance threshold); {@code false} otherwise
     */
    public boolean equals(final Quaternion another, float threshold) {
        if (another == null) {
            return false;
        }

        return (float) Math.abs(another.getX() - this.x) < threshold && (float) Math.abs(another.getY() - this.y) < threshold && (float) Math.abs(another.getZ() - this.z) < threshold && (float) Math.abs(another.getW() - this.w) < threshold;
    }

    //////////////////////////////////////////////////////////
    //
    // Quaternion normalization
    //
    /////////////////////////////////////////////////////////

    /**
     * Computes the norm of this quaternion
     *
     * @return the norm of this quaternion
     */
    public final float norm() {
        return (float) Math.sqrt(x * x + y * y + z * z + w * w);
    }

    /**
     * Computes the square of the norm of this quaternion
     *
     * @return the square of the norm
     */
    public final float squaredNorm() {
        return x * x + y * y + z * z + w * w;
    }

    /**
     * Normalizes the quaternion so that it has norm 1
     */
    public final void normalize() {
        float qNorm = this.norm();
        this.x /= qNorm;
        this.y /= qNorm;
        this.z /= qNorm;
        this.w /= qNorm;
    }

    /**
     * Normalizes the quaternion so that it has norm 1
     *
     * @see #normalize()
     */
    public final void toUnit() {
        this.normalize();
    }

    //////////////////////////////////////////////////////////
    //
    // Quaternion Arithmetics
    //
    /////////////////////////////////////////////////////////

    /**
     * Gets the conjugate of this quaternion
     *
     * @return the conjugate quaternion
     */
    public final Quaternion conjugate() {
        return new Quaternion(-this.x, -this.y, -this.z, this.w);
    }

    /**
     * Gets the conjugate of this quaternion and assigns it to this object
     */
    public final void conjugateEq() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
    }

    /**
     * Performs addition of two quaternions
     *
     * @param another The other quaternion involving in the addition
     * @return The quaternion which is the sum result
     */
    public final Quaternion add(final Quaternion another) {
        Quaternion result = new Quaternion(this);
        result.addEq(another);
        return result;
    }

    /**
     * Performs addition of two quaternions and assigns the result to this object
     *
     * @param another The other quaternion involving in the addition
     */
    public final void addEq(final Quaternion another) {
        this.x += another.x;
        this.y += another.y;
        this.z += another.z;
        this.w += another.w;
    }

    /**
     * Performs multiplication of this quaternion with the input quaternion,
     * that is {@code this * another}
     *
     * @param another The other quaternion involving in the multiplication
     * @return The quaternion which is the multiplication result
     */
    public final Quaternion multiply(final Quaternion another) {
        Quaternion result = new Quaternion(this);
        result.multiplyEq(another);
        return result;
    }

    /**
     * Performs scalar multiplication of this quaternion and the input number
     *
     * @param scalar A constant factor
     * @return The quaternion which is the multiplication result
     */
    public final Quaternion multiply(float scalar) {
        Quaternion result = new Quaternion(this);
        result.multiplyEq(scalar);
        return result;
    }

    /**
     * Performs multiplication of this quaternion with the input quaternion,
     * that is {@code this * another}. Assigns the result to this object
     *
     * @param another The other quaternion involving in the multiplication
     */
    public final void multiplyEq(final Quaternion another) {
        float newW = another.w * this.w - another.x * this.x - another.y * this.y - another.z * this.z;
        float newX = another.w * this.x + another.x * this.w - another.y * this.z + another.z * this.y;
        float newY = another.w * this.y + another.x * this.z + another.y * this.w - another.z * this.x;
        float newZ = another.w * this.z - another.x * this.y + another.y * this.x + another.z * this.w;
        this.w = newW;
        this.x = newX;
        this.y = newY;
        this.z = newZ;
    }

    /**
     * Performs scalar multiplication of this quaternion and the input number.
     * Assigns the result to this object
     *
     * @param scalar A constant factor
     */
    public final void multiplyEq(float scalar) {
        this.x *= scalar;
        this.y *= scalar;
        this.z *= scalar;
        this.w *= scalar;
    }

    /**
     * Gets the inverse (reciprocal) of this quaternion
     *
     * @return The inverse quaternion
     */
    public final Quaternion inverse() {
        Quaternion result = new Quaternion(this);
        result.invert();
        return result;
    }

    /**
     * Inverts this quaternion
     *
     * @see #invert()
     */
    public final void inverseEq() {
        this.invert();
    }

    /**
     * Inverts this quaternion
     */
    public final void invert() {
        float sqNorm = this.squaredNorm();
        this.conjugateEq();
        this.multiplyEq(1.0f / sqNorm);
    }

    /**
     * Performs division of this quaternion with the input quaternion, that is
     * {@code this / another}
     *
     * @param another The other quaternion involving in the division
     * @return The quaternion which is the division result
     */
    public final Quaternion divide(final Quaternion another) {
        Quaternion result = new Quaternion(this);
        result.divideEq(another);
        return result;
    }

    /**
     * Performs division of this quaternion with the input quaternion, that is
     * {@code this / another}. Assigns the result to this object
     *
     * @param another The other quaternion involving in the division
     */
    public final void divideEq(final Quaternion another) {
        this.multiplyEq(another.inverse());
    }

    //////////////////////////////////////////////////////////
    //
    // Quaternion Transcendental Functions
    //
    /////////////////////////////////////////////////////////

    /**
     * Gets the exponential of this Quaternion
     *
     * @return The exponential Quaternion
     */
    public final Quaternion exp() {
        float[] vectorPart = this.getVectorPart();
        float vNorm = vectorNorm(vectorPart);

        if (vNorm < EPSILON) {
            return new Quaternion(0.0f, 0.0f, 0.0f, (float) Math.exp(this.w));
        }

        float scalar = (float) Math.sin(vNorm) / vNorm;
        for (int i = 0; i < vectorPart.length; ++i) {
            vectorPart[i] *= scalar;
        }

        Quaternion result = new Quaternion(vectorPart[0], vectorPart[1], vectorPart[2], (float) Math.cos(vNorm));
        result.multiplyEq((float) Math.exp(this.w));
        return result;
    }

    /**
     * Gets the natural logarithm of this Quaternion
     *
     * @return The logarithm Quaternion
     * @throws ArithmeticException if the Quaternion has norm approaching 0, that
     *                             is the norm is less than {@link Quaternion#EPSILON}
     */
    public final Quaternion log() throws ArithmeticException {
        float qNorm = this.norm();
        if (qNorm < EPSILON) {
            throw new ArithmeticException(UNDEFINED_LOG_ZERO_QUATERNION_MSG);
        }

        float[] vectorPart = this.getVectorPart();
        float vNorm = vectorNorm(vectorPart);
        float[] resultVector = new float[]{0.0f, 0.0f, 0.0f};
        if (!(vNorm < EPSILON)) {
            float factor = (float) Math.acos(this.w / qNorm) / vNorm;
            for (int i = 0; i < vectorPart.length; ++i) {
                resultVector[i] = vectorPart[i] * factor;
            }
        }

        return new Quaternion(resultVector[0], resultVector[1], resultVector[2], (float) Math.log(qNorm));
    }

    //////////////////////////////////////////////////////////
    //
    // Quaternion functions related to Rotation
    //
    /////////////////////////////////////////////////////////

    /**
     * Returns the rotation matrix represented by the normalized version of
     * this quaternion
     *
     * @return A 3 x 3 rotation matrix
     */
    public final float[][] getRotationMatrix() {
        float sqNorm = this.squaredNorm();
        float[][] mat = new float[][]{{sqNorm - 2 * (y * y + z * z), 2 * (x * y - z * w), 2 * (x * z + y * w)}, {2 * (x * y + z * w), sqNorm - 2 * (x * x + z * z), 2 * (y * z - x * w)}, {2 * (x * z - y * w), 2 * (y * z + x * w), sqNorm - 2 * (x * x + y * y)},};
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                mat[i][j] /= sqNorm;
            }
        }
        return mat;
    }
    /**
     * Rotates a 3D vector by the rotation represented by this quaternion
     *
     * @param vector An array of size 3 representing a 3D vector
     * @return The image of the input vector after the rotation
     * @throws IllegalArgumentException if input vector is not an array of size 3
     */
    public final float[] rotate(final float[] vector) throws IllegalArgumentException {
        if (vector.length != 3) {
            throw new IllegalArgumentException("Input must be an array of size 3");
        }

        float[][] rotationMat = this.getRotationMatrix();
        float[] imageVector = new float[3];

        for (int r = 0; r < 3; r++) {
            imageVector[r] = 0.0f;
            for (int c = 0; c < 3; c++) {
                imageVector[r] += rotationMat[r][c] * vector[c];
            }
        }

        return imageVector;
    }

    //////////////////////////////////////////////////////////
    //
    // Public  methods to create Quaternion
    //
    /////////////////////////////////////////////////////////

    /**
     * Gets an identity Quaternion (0.0, 0.0, 0.0, 1.0)
     *
     * @return An identity Quaternion
     */
    public  Quaternion getIdentity() {
        return new Quaternion();
    }

    /**
     * Gets the unit Quaternion of a rotation which is given by the input axis,
     * and angle (in degrees)
     *
     * @param axis       An array of size 3 representing the vector (x, y, z)
     * @param angleInDeg The angle (in degrees) of the rotation
     * @return The unit Quaternion of the rotation given by the input axis-angle
     * representation. If the norm of input axis vector is less than
     * {@link Quaternion#EPSILON}, an identity Quaternion is returned
     * @throws IllegalArgumentException if input vector is not an array of size 3
     */
    public  Quaternion fromAxisAngle(final float[] axis, float angleInDeg) throws IllegalArgumentException {
        if (axis.length != 3) {
            throw new IllegalArgumentException(VECTOR_INVALID_LENGTH_MSG);
        }

        float angleInRad = degreeToRadian(angleInDeg);
        return fromAxisAngleRad(axis, angleInRad);
    }

    /**
     * Gets the unit Quaternion of a rotation which is given by the input axis,
     * and angle (in radians)
     *
     * @param axis       An array of size 3 representing the vector (x, y, z)
     * @param angleInRad The angle (in radians) of the rotation
     * @return The unit Quaternion of the rotation given by the input axis-angle
     * representation. If the norm of input axis vector is less than
     * {@link Quaternion#EPSILON}, an identity Quaternion is returned
     * @throws IllegalArgumentException if input vector is not an array of size 3
     */
    public  Quaternion fromAxisAngleRad(final float[] axis, float angleInRad) throws IllegalArgumentException {
        if (axis.length != 3) {
            throw new IllegalArgumentException(VECTOR_INVALID_LENGTH_MSG);
        }

        // Normalize the input vector
        float vNorm = vectorNorm(axis);
        if (vNorm < EPSILON) {
            return new Quaternion();    // Identity Quaternion
        }

        axis[0] /= vNorm;
        axis[1] /= vNorm;
        axis[2] /= vNorm;

        float halfAngle = angleInRad / 2.0f;
        float sinTerm = (float) Math.sin(halfAngle);
        float x = axis[0] * sinTerm;
        float y = axis[1] * sinTerm;
        float z = axis[2] * sinTerm;
        float w = (float) Math.cos(halfAngle);
        return new Quaternion(x, y, z, w);
    }

    /**
     * Gets the unit Quaternion of a rotation specified by Euler . The order of
     * rotation is applying yaw, then pitch, then roll (that is z -&gt; y -&gt; x)
     *
     * @param roll  The roll angle (in radians)
     * @param pitch The pitch angle (in radians)
     * @param yaw   The yaw angle (in radians)
     * @return The unit Quaternion of a rotation specified by {@code roll},
     * {@code pitch}, {@code yaw} angles
     */
    public  Quaternion fromEulerAngles(float roll, float pitch, float yaw) {
        float cosHalfRoll = (float) Math.cos(roll * 0.5);
        float cosHalfPitch = (float) Math.cos(pitch * 0.5);
        float cosHalfYaw = (float) Math.cos(yaw * 0.5);
        float sinHalfRoll = (float) Math.sin(roll * 0.5);
        float sinHalfPitch = (float) Math.sin(pitch * 0.5);
        float sinHalfYaw = (float) Math.sin(yaw * 0.5);

        float w = cosHalfYaw * cosHalfPitch * cosHalfRoll + sinHalfYaw * sinHalfPitch * sinHalfRoll;
        float x = cosHalfYaw * cosHalfPitch * sinHalfRoll - sinHalfYaw * sinHalfPitch * cosHalfRoll;
        float y = cosHalfYaw * sinHalfPitch * cosHalfRoll + sinHalfYaw * cosHalfPitch * sinHalfRoll;
        float z = sinHalfYaw * cosHalfPitch * cosHalfRoll - cosHalfYaw * sinHalfPitch * sinHalfRoll;

        return new Quaternion(x, y, z, w);
    }

    public Quaternion fromDcm(float[] mData) {
        float[] data = new float[4];
        float tr = mData[0] + mData[4] + mData[8];
        if (tr > 0.0f) {
            float s = (float)Math.sqrt(tr + 1.0f);
            data[0] = s * 0.5f;
            s = 0.5f / s;
            data[1] = (mData[7] - mData[5]) * s;
            data[2] = (mData[2] - mData[6]) * s;
            data[3] = (mData[3] - mData[1]) * s;
        } else {
            int dcm_i = 0;
            for (int i = 1; i < 3; i++) {
                if (mData[i*3+i] > mData[dcm_i*3+dcm_i]) {
                    dcm_i = i;
                }
            }
            int dcm_j = (dcm_i + 1) % 3;
            int dcm_k = (dcm_i + 2) % 3;
            float s = (float)Math.sqrt((mData[dcm_i*3+dcm_i] - mData[dcm_j*3+dcm_j] -
                    mData[dcm_k*3+dcm_k]) + 1.0f);
            data[dcm_i + 1] = s * 0.5f;
            s = 0.5f / s;
            data[dcm_j + 1] = (mData[dcm_i*3+dcm_j] + mData[dcm_j*3+dcm_i]) * s;
            data[dcm_k + 1] = (mData[dcm_k*3+dcm_i] + mData[dcm_i*3+dcm_k]) * s;
            data[0] = (mData[dcm_k*3+dcm_j] - mData[dcm_j*3+dcm_k]) * s;
        }

        return new Quaternion(data[1],data[2],data[3],data[0]);
    }
    //////////////////////////////////////////////////////////
    //
    // Public  methods for interpolation
    //
    /////////////////////////////////////////////////////////

    /**
     * Interpolates linearly between {@code from} and {@code to} Quaternion. When
     * {@code t = 1}, the {@code to} Quaternion is returned. When {@code t = 0},
     * the {@code from} Quaternion is returned
     *
     * @param from The first Quaternion
     * @param to   The second Quaternion
     * @param t    Value indicating how far to interpolate between the two
     *             Quaternions
     * @return The resulting Quaternion for linear interpolation
     * @throws IllegalArgumentException if {@code t} is not between 0 and 1
     *                                  inclusively
     */
    public  Quaternion lerp(Quaternion from, Quaternion to, float t) throws IllegalArgumentException {
        if (t < -EPSILON || t > 1.0 + EPSILON) {
            throw new IllegalArgumentException(INVALID_INTERPOLATION_PARAM);
        }

        return from.multiply(1 - t).add(to.multiply(t));
    }

    public Quaternion derivative(float[] W) {
        float[] v = new float[]{0.0f, W[0], W[1], W[2]};
        Quaternion q = new Quaternion(
                0.5f * (w * W[0] - z * W[1] + y * W[2]),
                0.5f * (z * W[0] + w * W[1] - x * W[2]),
                -0.5f * (y * W[0] - x * W[1] - w * W[2]),
                -0.5f * (x * W[0] + y * W[1] + z * W[2]));
        return q;
    }

    public Quaternion update(float[] W,float dt) {
        Quaternion q = derivative(W);
        return this.add(q.multiply(dt));
    }
    //////////////////////////////////////////////////////////
    //
    // Private  methods
    //
    /////////////////////////////////////////////////////////

    private  float degreeToRadian(float degree) {
        return (float) Math.PI * degree / 180;
    }

    private  float radianToDegree(float radian) {
        return radian / (float) Math.PI * 180;
    }

    private  float vectorNorm(final float[] vector) {
        float result = 0.0f;
        for (int i = 0; i < vector.length; ++i) {
            result += vector[i] * vector[i];
        }
        return (float) Math.sqrt(result);
    }

    //////////////////////////////////////////////////////////
    //
    // Overridden methods inherited from Object
    //
    /////////////////////////////////////////////////////////

    /**
     * Gets a string representation of this Quaternion for display purposes
     *
     * @return A string contains information about this Quaternion
     */
    @Override
    public String toString() {
        return String.format("Quaternion(%f, %f, %f, %f)", this.x, this.y, this.z, this.w);
    }

    @Override
    public boolean equals(Object another) {
        // Self comparison
        if (this == another) {
            return true;
        }

        if (!(another instanceof Quaternion)) {
            return false;
        }

        Quaternion anotherQ = (Quaternion) another;
        if (Float.compare(anotherQ.w, this.w) != 0 || Float.compare(anotherQ.x, this.x) != 0 || Float.compare(anotherQ.y, this.y) != 0 || Float.compare(anotherQ.z, this.z) != 0) {
            return false;
        }

        return true;
    }

}
