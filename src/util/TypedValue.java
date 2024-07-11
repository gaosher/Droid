package util;


/**
 * 动态类型数据值的容器
 * 主要与 android.content.res.Resources 结合使用，用于保存资源值。
 */
public class TypedValue {
    /** 不包含任何数据的值 */
    public static final int TYPE_NULL = 0x00;

    /** The <var>data</var> field holds a resource identifier.
     *  data字段保存了一个资源标识符 */
    public static final int TYPE_REFERENCE = 0x01;

    /** The <var>data</var> field holds an attribute resource
     *  identifier (referencing an attribute in the current theme
     *  style, not a resource entry).
     *  data字段保存的是一个属性资源标识符，它引用了当前主题样式中的一个属性，而不是一个资源条目。*/
    public static final int TYPE_ATTRIBUTE = 0x02;

    /** The <var>string</var> field holds string data.  In addition, if
     *  <var>data</var> is non-zero then it is the string block
     *  index of the string and <var>assetCookie</var> is the set of
     *  assets the string came from.
     *  data字段保存一个字符串
     *  此外，如果data字段的值非零，则它表示字符串所在的字符串块索引，而assetCookie则表示字符串所属的资源集合*/
    public static final int TYPE_STRING = 0x03;

    /** The <var>data</var> field holds an IEEE 754 floating point number. */
    public static final int TYPE_FLOAT = 0x04;

    /** The <var>data</var> field holds a complex number encoding a dimension value.
     * 这段注释说明了data字段保存的是一个复杂数，用于编码一个尺寸值。 */
    public static final int TYPE_DIMENSION = 0x05;

    /** The <var>data</var> field holds a complex number encoding a fraction of a container.
     * 这段注释说明了data字段保存的是一个复杂数，用于编码一个容器的一部分的分数值*/
    public static final int TYPE_FRACTION = 0x06;

    /** Identifies the start of plain integer values.
     * Any type value from this to {@link #TYPE_LAST_INT} means the <var>data</var> field holds a generic integer value.
     *  这段注释说明了在整数值的开始处标识了一个范围。从这个值到 TYPE_LAST_INT 之间的任何类型值表示data字段保存的是一个通用的整数值。*/
    public static final int TYPE_FIRST_INT = 0x10;

    /** The <var>data</var> field holds a number that was originally specified in decimal.
     * data字段保存的是一个以十进制方式原始指定的数字*/
    public static final int TYPE_INT_DEC = 0x10;

    /** The <var>data</var> field holds a number that was originally specified in hexadecimal (0xn).
     *  data字段保存的是一个以十六进制方式原始指定的数字*/
    public static final int TYPE_INT_HEX = 0x11;

    /** The <var>data</var> field holds 0 or 1 that was originally specified as "false" or "true".
     * data字段保存的是一个以原始方式指定为 "false" 或 "true" 的值，它被表示为 0 或 1*/
    public static final int TYPE_INT_BOOLEAN = 0x12;


    /** Identifies the end of plain integer values.
     * 这个值标识了整数值的结束*/
    public static final int TYPE_LAST_INT = 0x1f;

    /* ------------------------------------------------------------------ */

    /** Complex data: bit location of unit information.
     * Complex data: 单位信息的bit位置 */
    public static final int COMPLEX_UNIT_SHIFT = 0;

    /** Complex data: mask to extract unit information (after shifting by{@link #COMPLEX_UNIT_SHIFT}).
     * This gives us 16 possible types, as defined below.
     * Complex data: 它是一个掩码，用于提取单位信息（在进行了 {@link #COMPLEX_UNIT_SHIFT} 移位后）
     * 可以得到 16 种可能的类型，如下所定义*/
    public static final int COMPLEX_UNIT_MASK = 0xf;

    /** {@link #TYPE_DIMENSION} complex unit: Value is raw pixels.
     * TYPE_DIMENSION complex unit: 原始像素值 */
    public static final int COMPLEX_UNIT_PX = 0;

    /** {@link #TYPE_DIMENSION} complex unit: Value is Device Independent Pixels.
     * TYPE_DIMENSION complex unit: dip/dp */
    public static final int COMPLEX_UNIT_DIP = 1;

    /** {@link #TYPE_DIMENSION} complex unit: Value is a scaled pixel.
     * TYPE_DIMENSION complex unit: sp */
    public static final int COMPLEX_UNIT_SP = 2;

    /** {@link #TYPE_DIMENSION} complex unit: Value is in points.
     * todo: 什么东西 */
    public static final int COMPLEX_UNIT_PT = 3;
    /** {@link #TYPE_DIMENSION} complex unit: Value is in inches.
     * TYPE_DIMENSION complex unit: 英寸 */
    public static final int COMPLEX_UNIT_IN = 4;

    /** {@link #TYPE_DIMENSION} complex unit: Value is in millimeters.
     * TYPE_DIMENSION complex unit: 毫米 */
    public static final int COMPLEX_UNIT_MM = 5;

    /** {@link #TYPE_FRACTION} complex unit: A basic fraction of the overall size.
     * {@link #TYPE_FRACTION} complex unit: 整体尺寸的基本分数 todo:什么东西*/
    public static final int COMPLEX_UNIT_FRACTION = 0;

    /** {@link #TYPE_FRACTION} complex unit: A fraction of the parent size.
     * {@link #TYPE_FRACTION} complex unit: parent size的基本分数 todo:什么东西 */
    public static final int COMPLEX_UNIT_FRACTION_PARENT = 1;

    /** Complex data: where the radix information is, telling where the decimal place appears in the mantissa.
     * Complex data: 表示基数信息的位置，指示尾数中小数点出现的位置。 */
    public static final int COMPLEX_RADIX_SHIFT = 4;

    /** Complex data: mask to extract radix information(after shifting by {@link #COMPLEX_RADIX_SHIFT}).
     * This give us 4 possible fixed point representations as defined below.
     * Complex data: 它是一个掩码，用于提取基数信息（在进行了 {@link #COMPLEX_RADIX_SHIFT} 移位后）
     * 这样可以得到 4 种可能的定点表示，如下所定义 todo：什么东西*/
    public static final int COMPLEX_RADIX_MASK = 0x3;

    /** Complex data: 尾数是一个integer -- i.e., 0xnnnnnn.0 */
    public static final int COMPLEX_RADIX_23p0 = 0;

    /** Complex data: the mantissa magnitude is 16 bits -- i.e, 0xnnnn.nn */
    public static final int COMPLEX_RADIX_16p7 = 1;
    /** Complex data: the mantissa magnitude is 8 bits -- i.e, 0xnn.nnnn */
    public static final int COMPLEX_RADIX_8p15 = 2;
    /** Complex data: the mantissa magnitude is 0 bits -- i.e, 0x0.nnnnnn */
    public static final int COMPLEX_RADIX_0p23 = 3;

    /** Complex data: bit location of mantissa information. */
    public static final int COMPLEX_MANTISSA_SHIFT = 8;
    /** Complex data: mask to extract mantissa information (after shifting by
     *  {@link #COMPLEX_MANTISSA_SHIFT}). This gives us 23 bits of precision;
     *  the top bit is the sign. */
    public static final int COMPLEX_MANTISSA_MASK = 0xffffff;

    /* ------------------------------------------------------------ */

    /**
     * {@link #TYPE_NULL} data indicating the value was not specified.
     */
    public static final int DATA_NULL_UNDEFINED = 0;
    /**
     * {@link #TYPE_NULL} data indicating the value was explicitly set to null.
     */
    public static final int DATA_NULL_EMPTY = 1;

    /* ------------------------------------------------------------ */

    /**
     * If {@link #density} is equal to this value then the density should be reated as the system's default density value: {todo: DisplayMetrics#DENSITY_DEFAULT}.
     */
    public static final int DENSITY_DEFAULT = 0;

    /**
     * If {@link #density} is equal to this value, then there is no density associated with the resource and it should not be scaled.
     */
    public static final int DENSITY_NONE = 0xffff; //todo：有用吗？

    /* ------------------------------------------------------------ */

    /** The type held by this value, as defined by the constants here.
     * This tells you how to interpret the other fields in the object.
     * 这个值所持有的类型，由这里的常量定义。这告诉您如何解释对象中的其他字段 */
    public int type;

    /** If the value holds a string, this is it. */
    public CharSequence string;

    /** Basic data in the value, interpreted according to {@link #type} */
    public int data;

    /** Additional information about where the value came from; only set for strings. */
    public int assetCookie;

    /** If Value came from a resource, this holds the corresponding resource id. */
    // @AnyRes
    public int resourceId;

    /**
     * If the Value came from a resource, this holds the corresponding pixel density.
     * */
    public int density;

    /**
     * If the Value came from a style resource or a layout resource (set in an XML layout), this
     * holds the corresponding style or layout resource id against which the attribute was resolved.
     * 如果值来自样式资源或布局资源（在 XML 布局中设置），则该字段保存与该属性解析关联的相应样式或布局资源的 ID。
     */
    public int sourceResourceId;

    /* ------------------------------------------------------------ */

    /** Return the data for this value as a float.  Only use for values whose type is {@link #TYPE_FLOAT}. */
    public final float getFloat() {
        return Float.intBitsToFloat(data);
    }
    /** 1.0f / (1 << 8) = 1.0f / (0b100000000) */
    private static final float MANTISSA_MULT = 1.0f / (1<<TypedValue.COMPLEX_MANTISSA_SHIFT);
    private static final float[] RADIX_MULTS = new float[] {
            1.0f * MANTISSA_MULT,
            1.0f/(1<<7) * MANTISSA_MULT,
            1.0f/(1<<15) * MANTISSA_MULT,
            1.0f/(1<<23) * MANTISSA_MULT
    };


    /**
     * Retrieve the base value from a complex data integer.
     * This uses the {@link #COMPLEX_MANTISSA_MASK} and {@link #COMPLEX_RADIX_MASK} fields of the data
     * to compute a floating point representation of the number they describe.
     * The units are ignored.
     * 从complex data integer中检索基本值。
     * 这个方法使用数据的 COMPLEX_MANTISSA_MASK 和 COMPLEX_RADIX_MASK字段来计算它们描述的数字的浮点表示
     * 单位被忽略
     * @param complex A complex data value.
     * @return A floating point value corresponding to the complex data.
     */
    public static float complexToFloat(int complex)
    {
        return (complex & (TypedValue.COMPLEX_MANTISSA_MASK << TypedValue.COMPLEX_MANTISSA_SHIFT))
                * RADIX_MULTS[(complex >> TypedValue.COMPLEX_RADIX_SHIFT) & TypedValue.COMPLEX_RADIX_MASK];
    }

    /**
     * Converts a complex data value holding a dimension to its final floating point value.
     * The given <var>data</var> must be structured as a {@link #TYPE_DIMENSION}.
     * @param data A complex data value holding a unit, magnitude, and mantissa.
     * @param metrics Current display metrics to use in the conversion -- supplies display density and scaling information.
     *
     * @return The complex floating point value multiplied by the appropriate metrics depending on its unit.
     */
    public static float complexToDimension(int data, DisplayMetrics metrics)
    {
        return applyDimension(
                (data>>COMPLEX_UNIT_SHIFT)&COMPLEX_UNIT_MASK,
                complexToFloat(data),
                metrics);
    }

    /**
     * Converts a complex data value holding a dimension to its final value as an integer pixel offset.
     * This is the same as {@link #complexToDimension}, except the raw floating point value is truncated to an integer (pixel) value.
     * The given <var>data</var> must be structured as a {@link #TYPE_DIMENSION}.
     * @param data A complex data value holding a unit, magnitude, and mantissa.
     * @param metrics Current display metrics to use in the conversion -- supplies display density and scaling information.
     * @return The number of pixels specified by the data and its desired multiplier and units.
     */
    public static int complexToDimensionPixelOffset(int data, DisplayMetrics metrics) {
        return (int)applyDimension(
                (data>>COMPLEX_UNIT_SHIFT)&COMPLEX_UNIT_MASK,
                complexToFloat(data),
                metrics);
    }

    /**
     * Converts a complex data value holding a dimension to its final value
     * as an integer pixel size.  This is the same as
     * {@link #complexToDimension}, except the raw floating point value is
     * converted to an integer (pixel) value for use as a size.  A size
     * conversion involves rounding the base value, and ensuring that a
     * non-zero base value is at least one pixel in size.
     * The given <var>data</var> must be structured as a
     * {@link #TYPE_DIMENSION}.
     *
     * @param data A complex data value holding a unit, magnitude, and
     *             mantissa.
     * @param metrics Current display metrics to use in the conversion --
     *                supplies display density and scaling information.
     *
     * @return The number of pixels specified by the data and its desired
     * multiplier and units.
     */
    public static int complexToDimensionPixelSize(int data,
                                                  DisplayMetrics metrics)
    {
        final float value = complexToFloat(data);
        final float f = applyDimension(
                (data>>COMPLEX_UNIT_SHIFT)&COMPLEX_UNIT_MASK,
                value,
                metrics);
        final int res = (int) ((f >= 0) ? (f + 0.5f) : (f - 0.5f));
        if (res != 0) return res;
        if (value == 0) return 0;
        if (value > 0) return 1;
        return -1;
    }

    /**
     * Return the complex unit type for this value.
     * For example, a dimen type with value 12sp will return {@link #COMPLEX_UNIT_SP}.
     * Only use for values whose type is {@link #TYPE_DIMENSION}.
     * @return The complex unit type.
     * 返回此值的 complex 单位类型
     * 例如，具有值为 12sp 的尺寸类型将返回 {@link #COMPLEX_UNIT_SP}
     * 仅用于类型为 {@link #TYPE_DIMENSION} 的值
     */
    public int getComplexUnit() {
        return COMPLEX_UNIT_MASK & (data>>TypedValue.COMPLEX_UNIT_SHIFT);
    }

    /**
     * Converts an unpacked complex data value holding a dimension to its final floating point value.
     * The two parameters <var>unit</var> and <var>value</var> are as in {@link #TYPE_DIMENSION}.
     * 将一个未打包的复杂数据值转换为其最终的浮点数值，该值保存了一个尺寸。
     * 两个参数 <var>unit</var> 和 <var>value</var> 如同 {@link #TYPE_DIMENSION} 中描述的一样。
     * @param unit The unit to convert from.
     *             要转换的单位
     * @param value The value to apply the unit to.
     * @param metrics Current display metrics to use in the conversion -- supplies display density and scaling information.
     * @return The complex floating point value multiplied by the appropriate metrics depending on its unit.
     */
    public static float applyDimension(int unit, float value, DisplayMetrics metrics) {
        switch (unit) {
            case COMPLEX_UNIT_PX:
                return value;
            case COMPLEX_UNIT_DIP:
                return value * metrics.density;
            case COMPLEX_UNIT_SP:
                return value * metrics.scaledDensity;
            case COMPLEX_UNIT_PT:
                return value * metrics.xdpi * (1.0f/72);
            case COMPLEX_UNIT_IN:
                return value * metrics.xdpi;
            case COMPLEX_UNIT_MM:
                return value * metrics.xdpi * (1.0f/25.4f);
        }
        return 0;
    }

    /**
     * Return the data for this value as a dimension.  Only use for values
     * whose type is {@link #TYPE_DIMENSION}.
     *
     * @param metrics Current display metrics to use in the conversion --
     *                supplies display density and scaling information.
     *
     * @return The complex floating point value multiplied by the appropriate
     * metrics depending on its unit.
     */
    public float getDimension(DisplayMetrics metrics)
    {
        return complexToDimension(data, metrics);
    }

    /**
     * Converts a complex data value holding a fraction to its final floating
     * point value. The given <var>data</var> must be structured as a
     * {@link #TYPE_FRACTION}.
     *
     * @param data A complex data value holding a unit, magnitude, and
     *             mantissa.
     * @param base The base value of this fraction.  In other words, a
     *             standard fraction is multiplied by this value.
     * @param pbase The parent base value of this fraction.  In other
     *             words, a parent fraction (nn%p) is multiplied by this
     *             value.
     *
     * @return The complex floating point value multiplied by the appropriate
     * base value depending on its unit.
     */
    public static float complexToFraction(int data, float base, float pbase)
    {
        switch ((data>>COMPLEX_UNIT_SHIFT)&COMPLEX_UNIT_MASK) {
            case COMPLEX_UNIT_FRACTION:
                return complexToFloat(data) * base;
            case COMPLEX_UNIT_FRACTION_PARENT:
                return complexToFloat(data) * pbase;
        }
        return 0;
    }

    /**
     * Return the data for this value as a fraction.
     * Only use for values whose type is {@link #TYPE_FRACTION}.
     *
     * @param base The base value of this fraction.  In other words, a standard fraction is multiplied by this value.
     * @param pbase The parent base value of this fraction.
     *              In other words, a parent fraction (nn%p) is multiplied by this value.
     *
     * @return The complex floating point value multiplied by the appropriate base value depending on its unit.
     */
    public float getFraction(float base, float pbase) {
        return complexToFraction(data, base, pbase);
    }

    /**
     * Regardless of the actual type of the value, try to convert it to a string value.
     * For example, a color type will be converted to a string of the form #aarrggbb.
     * @return CharSequence The coerced string value. If the value is null or the type is not known, null is returned.
     */
    public final CharSequence coerceToString() {
        int t = type;
        if (t == TYPE_STRING) {
            return string;
        }
        return coerceToString(t, data);
    }

    private static final String[] DIMENSION_UNIT_STRS = new String[] {
            "px", "dip", "sp", "pt", "in", "mm"
    };
    private static final String[] FRACTION_UNIT_STRS = new String[] {
            "%", "%p"
    };

    /**
     * Perform type conversion as per {@link #coerceToString()} on an explicitly supplied type and data.
     *
     * @param type The data type identifier.
     * @param data The data value.
     *
     * @return String The coerced string value.  If the value is
     *         null or the type is not known, null is returned.
     */
    public static final String coerceToString(int type, int data)
    {
        switch (type) {
            case TYPE_NULL:
                return null;
            case TYPE_REFERENCE:
                return "@" + data;
            case TYPE_ATTRIBUTE:
                return "?" + data;
            case TYPE_FLOAT:
                return Float.toString(Float.intBitsToFloat(data));
            case TYPE_DIMENSION:
                return Float.toString(complexToFloat(data)) + DIMENSION_UNIT_STRS[
                        (data>>COMPLEX_UNIT_SHIFT)&COMPLEX_UNIT_MASK];
            case TYPE_FRACTION:
                return Float.toString(complexToFloat(data)*100) + FRACTION_UNIT_STRS[
                        (data>>COMPLEX_UNIT_SHIFT)&COMPLEX_UNIT_MASK];
            case TYPE_INT_HEX:
                return "0x" + Integer.toHexString(data);
            case TYPE_INT_BOOLEAN:
                return data != 0 ? "true" : "false";
        }

        if (type >= TYPE_FIRST_INT && type <= TYPE_LAST_INT) {
            return Integer.toString(data);
        }
        return null;
    }

    public void setTo(TypedValue other)
    {
        type = other.type;
        string = other.string;
        data = other.data;
        assetCookie = other.assetCookie;
        resourceId = other.resourceId;
        density = other.density;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("TypedValue{t=0x").append(Integer.toHexString(type));
        sb.append("/d=0x").append(Integer.toHexString(data));
        if (type == TYPE_STRING) {
            sb.append(" \"").append(string != null ? string : "<null>").append("\"");
        }
        if (assetCookie != 0) {
            sb.append(" a=").append(assetCookie);
        }
        if (resourceId != 0) {
            sb.append(" r=0x").append(Integer.toHexString(resourceId));
        }
        sb.append("}");
        return sb.toString();
    }
};

